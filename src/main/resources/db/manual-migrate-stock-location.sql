-- Optional manual migration for stock location tables (Hibernate ddl-auto=update usually suffices).

-- CREATE TABLE IF NOT EXISTS stock_location (
--   id BIGSERIAL PRIMARY KEY,
--   stock_location_code VARCHAR(255) NOT NULL UNIQUE
-- );

-- CREATE TABLE IF NOT EXISTS stocked_material (
--   id BIGSERIAL PRIMARY KEY,
--   stock_location_id BIGINT NOT NULL,
--   material_id BIGINT NOT NULL,
--   quantity INTEGER NOT NULL DEFAULT 0,
--   CONSTRAINT fk_stocked_material_location
--     FOREIGN KEY (stock_location_id) REFERENCES stock_location(id) ON DELETE CASCADE,
--   CONSTRAINT fk_stocked_material_material
--     FOREIGN KEY (material_id) REFERENCES material(id)
-- );

-- If upgrading from the earlier many-to-many join table:
-- DROP TABLE IF EXISTS stocked_material_material;
-- ALTER TABLE stocked_material ADD COLUMN IF NOT EXISTS material_id BIGINT;
-- (backfill material_id from join table before dropping, if you had data)
