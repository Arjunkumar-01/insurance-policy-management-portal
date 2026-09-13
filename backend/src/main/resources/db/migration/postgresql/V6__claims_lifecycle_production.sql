ALTER TABLE claims
    ADD COLUMN IF NOT EXISTS claim_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS submitted_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS reviewed_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS settled_date TIMESTAMP;

UPDATE claims
SET claim_amount = 1.00
WHERE claim_amount IS NULL;

UPDATE claims
SET submitted_date = COALESCE(created_at, now())
WHERE submitted_date IS NULL;

ALTER TABLE claims
    ALTER COLUMN claim_amount SET NOT NULL,
    ALTER COLUMN submitted_date SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_claims_amount_positive'
    ) THEN
        ALTER TABLE claims
            ADD CONSTRAINT chk_claims_amount_positive CHECK (claim_amount > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_claims_status'
    ) THEN
        ALTER TABLE claims
            ADD CONSTRAINT chk_claims_status
                CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'SETTLED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_claims_submitted_by_customer_id ON claims(submitted_by_customer_id);
CREATE INDEX IF NOT EXISTS idx_claims_policy_id ON claims(policy_id);
CREATE INDEX IF NOT EXISTS idx_claims_status ON claims(status);
CREATE INDEX IF NOT EXISTS idx_claims_submitted_date ON claims(submitted_date);

