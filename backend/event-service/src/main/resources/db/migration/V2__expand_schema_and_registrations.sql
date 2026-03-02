-- Recreating missing V2 migration for schema expansion
ALTER TABLE events RENAME COLUMN event_type TO category;
ALTER TABLE events RENAME COLUMN start_date TO start_date_time;
ALTER TABLE events RENAME COLUMN end_date TO end_date_time;
ALTER TABLE events RENAME COLUMN created_by TO organizer_id;
ALTER TABLE events DROP COLUMN images;
ALTER TABLE events DROP COLUMN is_featured;
ALTER TABLE events DROP COLUMN is_active;

ALTER TABLE events ADD COLUMN total_capacity INT NOT NULL DEFAULT 0;
ALTER TABLE events ADD COLUMN available_seats INT NOT NULL DEFAULT 0;
ALTER TABLE events ADD COLUMN ticket_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE events ADD COLUMN currency VARCHAR(10) DEFAULT 'LKR';
ALTER TABLE events ADD COLUMN status VARCHAR(50) DEFAULT 'DRAFT';
ALTER TABLE events ADD COLUMN image_url VARCHAR(500);
ALTER TABLE events ADD COLUMN tags VARCHAR(500);
ALTER TABLE events ADD COLUMN version INT DEFAULT 0;

CREATE TABLE event_registrations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    tourist_id VARCHAR(255) NOT NULL,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    num_tickets INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    notes TEXT,
    cancellation_reason TEXT,
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_registration_event ON event_registrations(event_id);
CREATE INDEX idx_registration_tourist ON event_registrations(tourist_id);
