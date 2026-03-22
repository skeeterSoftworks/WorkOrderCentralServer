-- If product_order.product_id had a UNIQUE constraint from an old @OneToOne mapping,
-- drop it so multiple order lines can reference the same product.
-- Inspect constraints first: \d product_order
-- Typical Hibernate / PostgreSQL names (adjust if yours differ):

ALTER TABLE product_order DROP CONSTRAINT IF EXISTS uk_product_order_product_id;
ALTER TABLE product_order DROP CONSTRAINT IF EXISTS product_order_product_id_key;
