ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30),
    ADD COLUMN IF NOT EXISTS transaction_reference VARCHAR(60),
    ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);

UPDATE payments
SET status = 'SUCCESSFUL'
WHERE status = 'SUCCESS';

UPDATE payments
SET payment_method = 'UPI'
WHERE payment_method IS NULL;

UPDATE payments
SET transaction_reference = CONCAT('TXN-', id)
WHERE transaction_reference IS NULL;

ALTER TABLE payments
    ALTER COLUMN payment_method SET NOT NULL,
    ALTER COLUMN transaction_reference SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_payments_status'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT chk_payments_status
                CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESSFUL', 'FAILED', 'REFUNDED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_payments_method'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT chk_payments_method
                CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'UPI', 'INSURANCE_WALLET'));
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_payment_reference ON payments(payment_reference);
CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_transaction_reference ON payments(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_policy_id ON payments(policy_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_payment_date ON payments(payment_date);

