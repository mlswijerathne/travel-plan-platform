-- Create hotels table
CREATE TABLE hotels (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    star_rating INT CHECK (star_rating >= 1 AND star_rating <= 5),
    average_rating DECIMAL(3, 2) DEFAULT 0,
    review_count INT DEFAULT 0,
    amenities TEXT[],
    check_in_time TIME DEFAULT '14:00',
    check_out_time TIME DEFAULT '11:00',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hotels_owner_id ON hotels(owner_id);
CREATE INDEX idx_hotels_city ON hotels(city);
CREATE INDEX idx_hotels_location ON hotels(latitude, longitude);

-- Create rooms table
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price_per_night DECIMAL(10, 2) NOT NULL,
    max_occupancy INT NOT NULL DEFAULT 2,
    amenities TEXT[],
    total_rooms INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rooms_hotel_id ON rooms(hotel_id);
