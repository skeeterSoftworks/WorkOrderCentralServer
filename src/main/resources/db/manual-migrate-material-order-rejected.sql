-- Run once on PostgreSQL when REJECTED was added to material orders.
-- Hibernate ddl-auto=update adds columns but does not widen enum check constraints.

ALTER TABLE material_order ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6);
ALTER TABLE material_order ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP(6);

ALTER TABLE material_order DROP CONSTRAINT IF EXISTS material_order_status_check;

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
);
