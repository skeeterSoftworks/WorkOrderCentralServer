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
 * Migrates legacy {@code product_material(product_id, material_id)} join rows to
 * {@code product_material(id, product_id, material_id, quantity_per_product_unit)}.
 */
@Slf4j
@Component
public class ProductMaterialSchemaPatch implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public ProductMaterialSchemaPatch(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgresql()) {
            return;
        }
        try {
            migrateProductMaterialTable();
        } catch (Exception e) {
            log.warn("Could not patch product_material schema: {}", e.getMessage());
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

    private void migrateProductMaterialTable() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM information_schema.tables
                        WHERE table_schema = current_schema() AND table_name = 'product_material'
                        """,
                Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer idColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM information_schema.columns
                        WHERE table_schema = current_schema()
                          AND table_name = 'product_material'
                          AND column_name = 'id'
                        """,
                Integer.class);
        if (idColumnCount != null && idColumnCount > 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE product_material ADD COLUMN IF NOT EXISTS quantity_per_product_unit DOUBLE PRECISION");
            jdbcTemplate.execute("""
                    UPDATE product_material pm
                    SET quantity_per_product_unit = CASE
                        WHEN m.products_per_unit IS NOT NULL AND m.products_per_unit > 0
                        THEN 1.0 / m.products_per_unit
                        ELSE 1.0
                    END
                    FROM material m
                    WHERE pm.material_id = m.id
                      AND (pm.quantity_per_product_unit IS NULL OR pm.quantity_per_product_unit <= 0)
                    """);
            jdbcTemplate.execute(
                    "UPDATE product_material SET quantity_per_product_unit = 1.0 WHERE quantity_per_product_unit IS NULL OR quantity_per_product_unit <= 0");
            jdbcTemplate.execute(
                    "ALTER TABLE product_material ADD COLUMN IF NOT EXISTS unit_of_measure VARCHAR(16)");
            jdbcTemplate.execute(
                    "UPDATE product_material SET unit_of_measure = 'PCS' WHERE unit_of_measure IS NULL OR TRIM(unit_of_measure) = ''");
            return;
        }

        jdbcTemplate.execute("""
                CREATE TABLE product_material_migrated (
                    id BIGSERIAL PRIMARY KEY,
                    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                    material_id BIGINT NOT NULL REFERENCES material(id) ON DELETE CASCADE,
                    quantity_per_product_unit DOUBLE PRECISION NOT NULL DEFAULT 1,
                    unit_of_measure VARCHAR(16) NOT NULL DEFAULT 'PCS',
                    UNIQUE (product_id, material_id)
                )
                """);
        jdbcTemplate.execute("""
                INSERT INTO product_material_migrated (product_id, material_id, quantity_per_product_unit)
                SELECT pm.product_id,
                       pm.material_id,
                       CASE
                           WHEN m.products_per_unit IS NOT NULL AND m.products_per_unit > 0
                           THEN 1.0 / m.products_per_unit
                           ELSE 1.0
                       END
                FROM product_material pm
                JOIN material m ON m.id = pm.material_id
                """);
        jdbcTemplate.execute("DROP TABLE product_material");
        jdbcTemplate.execute("ALTER TABLE product_material_migrated RENAME TO product_material");
        log.info("Migrated product_material to per-batch quantity model");
    }
}
