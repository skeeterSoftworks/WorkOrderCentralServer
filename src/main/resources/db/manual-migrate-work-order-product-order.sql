-- One-time migration when upgrading from work_order.purchase_order_id to work_order.product_order_id.
-- Hibernate ddl-auto=update may add product_order_id but not remove the old column or migrate data.
-- Review and run manually on PostgreSQL after backup.

-- 1) Add new column if missing
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS product_order_id BIGINT;

-- 2) Map each existing work order to the first product line of its purchase order (adjust if you need another rule)
UPDATE work_order wo
SET product_order_id = po_min.line_id
FROM (
    SELECT po.order_id AS purchase_order_id, MIN(po.id) AS line_id
    FROM product_order po
    GROUP BY po.order_id
) po_min
WHERE wo.purchase_order_id IS NOT NULL
  AND po_min.purchase_order_id = wo.purchase_order_id
  AND wo.product_order_id IS NULL;

-- 3) Drop old FK/column (names may differ — inspect \d work_order)
ALTER TABLE work_order DROP CONSTRAINT IF EXISTS work_order_purchase_order_id_key;
ALTER TABLE work_order DROP CONSTRAINT IF EXISTS fk_work_order_purchase_order;
ALTER TABLE work_order DROP COLUMN IF EXISTS purchase_order_id;

-- 4) Constraints for new model
ALTER TABLE work_order ADD CONSTRAINT uk_work_order_product_order UNIQUE (product_order_id);
ALTER TABLE work_order ADD CONSTRAINT fk_work_order_product_order FOREIGN KEY (product_order_id) REFERENCES product_order (id);
