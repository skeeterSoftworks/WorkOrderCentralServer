-- Optional: add machine photo column (PostgreSQL). Run if schema is not managed by ddl-auto.
ALTER TABLE machine ADD COLUMN IF NOT EXISTS machine_image BYTEA;
