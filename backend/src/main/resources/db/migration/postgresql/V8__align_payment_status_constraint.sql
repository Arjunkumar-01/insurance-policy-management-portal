DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'payments_status_check'
    ) THEN
        ALTER TABLE payments DROP CONSTRAINT payments_status_check;
    END IF;
END $$;
