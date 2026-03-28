-- One-time PostgreSQL migration:
-- Older Hibernate @Lob byte[] mapped image_data to type "oid" (large object reference).
-- The entity now uses @JdbcTypeCode(VARBINARY), which expects "bytea".
--
-- Run against your Work Order Central DB (see application-pg.properties), e.g.:
--   psql -U postgres -d work_order_central -f manual-migrate-quality-info-step-image-bytea.sql
--
-- Existing binary data stored as large objects is copied into bytea via lo_get().
-- If the table is empty or you can drop image data, you may instead:
--   ALTER TABLE quality_info_step DROP COLUMN IF EXISTS image_data;
--   ALTER TABLE quality_info_step ADD COLUMN image_data bytea;

ALTER TABLE quality_info_step
    ALTER COLUMN image_data TYPE bytea
    USING (
        CASE
            WHEN image_data IS NULL THEN NULL
            ELSE lo_get(image_data)
        END
    );

-- Optional: reclaim orphaned large objects (run only after verifying images still load correctly)
-- SELECT lo_unlink(loid) FROM pg_largeobject_metadata;  -- do NOT run blindly; use vacuumlo or targeted cleanup
