-- Keep in sync with MaterialOrderSchemaPatch.removeAcknowledgedStatus.
-- Migrates legacy ORDER_ACKNOWLEDGED rows and refreshes the status check constraint.

UPDATE material_order SET status = 'ORDER_SENT' WHERE status = 'ORDER_ACKNOWLEDGED';

ALTER TABLE material_order_line ADD COLUMN IF NOT EXISTS offered_price_per_unit NUMERIC(19, 4);

ALTER TABLE material_order DROP CONSTRAINT IF EXISTS material_order_status_check;

ALTER TABLE material_order ADD CONSTRAINT material_order_status_check CHECK (
    status IN (
        'ORDER_CREATED',
        'ORDER_SENT',
        'ORDER_ACCEPTED',
        'IN_TRANSPORT',
        'RECEIVED_IN_STOCK',
        'VALIDATED',
        'REJECTED'
    )
);
