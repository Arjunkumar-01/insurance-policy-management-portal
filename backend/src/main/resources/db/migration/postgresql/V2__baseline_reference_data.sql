-- Baseline data normalization for clean and existing environments.
-- This migration does not add business seed data; it only normalizes required defaults.

UPDATE customers
SET enabled = TRUE
WHERE enabled IS NULL;

UPDATE products
SET active = TRUE
WHERE active IS NULL;

UPDATE policies
SET status = 'PENDING'
WHERE status IS NULL;

UPDATE claims
SET status = 'SUBMITTED'
WHERE status IS NULL;

UPDATE payments
SET status = 'PENDING'
WHERE status IS NULL;

