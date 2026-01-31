CREATE TABLE tour_guides (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    bio TEXT,
    languages TEXT[],
    specializations TEXT[],
    experience_years INT DEFAULT 0,
    hourly_rate DECIMAL(10, 2),
    daily_rate DECIMAL(10, 2),
    average_rating DECIMAL(3, 2) DEFAULT 0,
    review_count INT DEFAULT 0,
    profile_image_url TEXT,
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tour_guides_user_id ON tour_guides(user_id);
CREATE INDEX idx_tour_guides_languages ON tour_guides USING GIN(languages);
