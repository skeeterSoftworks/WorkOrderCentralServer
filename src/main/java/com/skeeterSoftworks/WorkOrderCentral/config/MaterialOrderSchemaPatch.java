package com.skeeterSoftworks.WorkOrderCentral.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Hibernate {@code ddl-auto=update} does not alter PostgreSQL check constraints when enum values
 * are added in Java. Recreate {@code material_order_status_check} so {@code REJECTED} is allowed.
 */
@Slf4j
@Component
public class MaterialOrderSchemaPatch implements ApplicationRunner {

    private static final String STATUS_CHECK = "material_order_status_check";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public MaterialOrderSchemaPatch(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgresql()) {
            return;
        }
        try {
            patchStatusCheckConstraint();
            ensureTimestampColumns();
            migrateMaterialOrderLines();
            ensureDeliveryNoteTable();
            ensureReceptionPerDeliveryNote();
        } catch (Exception e) {
            log.warn("Could not patch material_order schema: {}", e.getMessage());
        }
    }

    private boolean isPostgresql() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String product = meta.getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgresql");
        } catch (SQLException e) {
            return false;
        }
    }

    private void patchStatusCheckConstraint() {
        if (constraintIncludesRejected()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE material_order DROP CONSTRAINT IF EXISTS " + STATUS_CHECK);
        jdbcTemplate.execute("""
                ALTER TABLE material_order ADD CONSTRAINT material_order_status_check CHECK (
                    status IN (
                        'ORDER_CREATED',
                        'ORDER_SENT',
                        'ORDER_ACKNOWLEDGED',
                        'ORDER_ACCEPTED',
                        'IN_TRANSPORT',
                        'RECEIVED_IN_STOCK',
                        'VALIDATED',
                        'REJECTED'
                    )
                )
                """);
        log.info("Updated {} to allow REJECTED status", STATUS_CHECK);
    }

    private boolean constraintIncludesRejected() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_constraint c
                JOIN pg_class t ON c.conrelid = t.oid
                WHERE t.relname = 'material_order'
                  AND c.conname = ?
                  AND pg_get_constraintdef(c.oid) LIKE '%REJECTED%'
                """, Integer.class, STATUS_CHECK);
        return count != null && count > 0;
    }

    private void ensureTimestampColumns() {
        jdbcTemplate.execute("ALTER TABLE material_order ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6)");
        jdbcTemplate.execute("ALTER TABLE material_order ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP(6)");
    }

    private void migrateMaterialOrderLines() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS material_order_line (
                    id BIGSERIAL PRIMARY KEY,
                    material_order_id BIGINT NOT NULL REFERENCES material_order(id),
                    material_id BIGINT NOT NULL REFERENCES material(id),
                    quantity INT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                INSERT INTO material_order_line (material_order_id, material_id, quantity)
                SELECT mo.id, mo.material_id, mo.quantity
                FROM material_order mo
                WHERE mo.material_id IS NOT NULL
                  AND mo.quantity IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM material_order_line l WHERE l.material_order_id = mo.id
                  )
                """);
        jdbcTemplate.execute("""
                ALTER TABLE material_order_reception
                ADD COLUMN IF NOT EXISTS material_order_line_id BIGINT REFERENCES material_order_line(id)
                """);
        jdbcTemplate.execute("""
                UPDATE material_order_reception r
                SET material_order_line_id = (
                    SELECT l.id FROM material_order_line l
                    WHERE l.material_order_id = r.material_order_id
                    ORDER BY l.id
                    LIMIT 1
                )
                WHERE r.material_order_line_id IS NULL
                  AND EXISTS (
                      SELECT 1 FROM material_order_line l WHERE l.material_order_id = r.material_order_id
                  )
                """);
        log.info("Ensured material_order_line table and migrated legacy material_order rows");
    }

    private void ensureDeliveryNoteTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS delivery_note (
                    id BIGSERIAL PRIMARY KEY,
                    material_order_id BIGINT NOT NULL REFERENCES material_order(id),
                    material_order_line_id BIGINT NOT NULL REFERENCES material_order_line(id),
                    delivery_note_number VARCHAR(255) NOT NULL,
                    received_at TIMESTAMP(6) NOT NULL,
                    quantity INT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                INSERT INTO delivery_note (material_order_id, material_order_line_id, delivery_note_number, received_at, quantity)
                SELECT r.material_order_id, r.material_order_line_id, 'LEGACY-' || r.id, r.received_at, r.received_quantity
                FROM material_order_reception r
                WHERE r.material_order_line_id IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM delivery_note d WHERE d.material_order_line_id = r.material_order_line_id
                  )
                """);
        log.info("Ensured delivery_note table and migrated legacy receptions");
    }

    private void ensureReceptionPerDeliveryNote() {
        jdbcTemplate.execute("""
                ALTER TABLE material_order_reception
                ADD COLUMN IF NOT EXISTS delivery_note_id BIGINT REFERENCES delivery_note(id)
                """);
        jdbcTemplate.execute("""
                UPDATE material_order_reception r
                SET delivery_note_id = (
                    SELECT d.id FROM delivery_note d
                    WHERE d.material_order_line_id = r.material_order_line_id
                    ORDER BY d.received_at DESC, d.id DESC
                    LIMIT 1
                )
                WHERE r.delivery_note_id IS NULL
                  AND r.material_order_line_id IS NOT NULL
                  AND EXISTS (
                      SELECT 1 FROM delivery_note d WHERE d.material_order_line_id = r.material_order_line_id
                  )
                """);
        jdbcTemplate.execute("""
                INSERT INTO delivery_note (material_order_id, material_order_line_id, delivery_note_number, received_at, quantity)
                SELECT r.material_order_id, r.material_order_line_id, 'LEGACY-' || r.id, r.received_at, r.received_quantity
                FROM material_order_reception r
                WHERE r.material_order_line_id IS NOT NULL
                  AND r.delivery_note_id IS NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM delivery_note d WHERE d.material_order_line_id = r.material_order_line_id
                  )
                """);
        jdbcTemplate.execute("""
                UPDATE material_order_reception r
                SET delivery_note_id = (
                    SELECT d.id FROM delivery_note d
                    WHERE d.material_order_line_id = r.material_order_line_id
                    ORDER BY d.received_at DESC, d.id DESC
                    LIMIT 1
                )
                WHERE r.delivery_note_id IS NULL
                  AND r.material_order_line_id IS NOT NULL
                """);
        jdbcTemplate.execute("""
                INSERT INTO material_order_reception (
                    material_order_id, material_order_line_id, delivery_note_id, received_at, received_quantity
                )
                SELECT d.material_order_id, d.material_order_line_id, d.id, d.received_at, d.quantity
                FROM delivery_note d
                WHERE NOT EXISTS (
                    SELECT 1 FROM material_order_reception r WHERE r.delivery_note_id = d.id
                )
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_material_order_reception_delivery_note
                ON material_order_reception (delivery_note_id)
                WHERE delivery_note_id IS NOT NULL
                """);
        log.info("Linked material_order_reception rows to delivery_note batches");
    }
}
