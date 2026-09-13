-- Product Management migration
-- Runs before JPA startup and safely upgrades legacy products data.

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS product_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'products'
          AND column_name = 'active'
    ) THEN
        UPDATE products
        SET product_code = COALESCE(product_code, CONCAT('PRD-', LPAD(CAST(id AS TEXT), 6, '0'))),
            description = COALESCE(description, 'Default product description'),
            status = COALESCE(status, CASE WHEN active = TRUE THEN 'ACTIVE' ELSE 'INACTIVE' END);
    ELSE
        UPDATE products
        SET product_code = COALESCE(product_code, CONCAT('PRD-', LPAD(CAST(id AS TEXT), 6, '0'))),
            description = COALESCE(description, 'Default product description'),
            status = COALESCE(status, 'ACTIVE');
    END IF;
END $$;

ALTER TABLE products
    ALTER COLUMN product_code SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_products_product_code'
    ) THEN
        ALTER TABLE products ADD CONSTRAINT uk_products_product_code UNIQUE (product_code);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_products_status'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(product_category);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(product_name);
CREATE INDEX IF NOT EXISTS idx_products_code ON products(product_code);
CREATE INDEX IF NOT EXISTS idx_products_category_status ON products(product_category, status);

