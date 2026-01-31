CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_days INT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    max_participants INT,
    destinations TEXT[],
    inclusions TEXT[],
    exclusions TEXT[],
    images TEXT[],
    is_featured BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE package_items (
    id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL REFERENCES packages(id) ON DELETE CASCADE,
    day_number INT NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    provider_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_packages_is_active ON packages(is_active);
CREATE INDEX idx_packages_is_featured ON packages(is_featured);
CREATE INDEX idx_package_items_package_id ON package_items(package_id);
