-- V1__init_schema.sql
CREATE TABLE IF NOT EXISTS packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_days INT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    max_participants INT,
    destinations TEXT[],
    inclusions TEXT[],
    exclusions TEXT[],
    images TEXT[],
    is_featured BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_packages_is_active ON packages(is_active);
CREATE INDEX IF NOT EXISTS idx_packages_is_featured ON packages(is_featured);

CREATE TABLE IF NOT EXISTS package_items (
    id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    provider_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_package
        FOREIGN KEY (package_id) 
        REFERENCES packages(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_package_items_package_id ON package_items(package_id);
