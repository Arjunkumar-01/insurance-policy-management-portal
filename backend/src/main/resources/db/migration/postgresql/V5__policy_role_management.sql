ALTER TABLE policies
    ADD COLUMN IF NOT EXISTS coverage_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS renewal_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS cancellation_status VARCHAR(30);

UPDATE policies p
SET coverage_amount = pr.coverage_amount
FROM products pr
WHERE p.product_id = pr.id
  AND p.coverage_amount IS NULL;

UPDATE policies
SET coverage_amount = 0
WHERE coverage_amount IS NULL;

UPDATE policies
SET renewal_status = 'NOT_DUE'
WHERE renewal_status IS NULL;

UPDATE policies
SET cancellation_status = CASE
    WHEN status = 'CANCELLED' THEN 'APPROVED'
    WHEN status = 'CANCEL_REQUESTED' THEN 'REQUESTED'
    ELSE 'NONE'
END
WHERE cancellation_status IS NULL;

ALTER TABLE policies
    ALTER COLUMN coverage_amount SET NOT NULL,
    ALTER COLUMN renewal_status SET NOT NULL,
    ALTER COLUMN cancellation_status SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_policies_customer_id ON policies(customer_id);
CREATE INDEX IF NOT EXISTS idx_policies_product_id ON policies(product_id);
CREATE INDEX IF NOT EXISTS idx_policies_status ON policies(status);
CREATE INDEX IF NOT EXISTS idx_policies_end_date ON policies(end_date);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_policies_renewal_status'
    ) THEN
        ALTER TABLE policies
            ADD CONSTRAINT chk_policies_renewal_status
                CHECK (renewal_status IN ('NOT_DUE','ELIGIBLE','RENEWAL_REQUESTED','RENEWED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_policies_cancellation_status'
    ) THEN
        ALTER TABLE policies
            ADD CONSTRAINT chk_policies_cancellation_status
                CHECK (cancellation_status IN ('NONE','REQUESTED','APPROVED','REJECTED'));
    END IF;
END $$;

