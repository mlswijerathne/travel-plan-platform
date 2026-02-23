-- Migration to add missing columns to events table and create event_registrations table

-- 1. Update events table to match the technical plan
ALTER TABLE events 
    ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'OTHER',
    ADD COLUMN IF NOT EXISTS total_capacity INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS available_seats INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS ticket_price DECIMAL(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'LKR',
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS tags VARCHAR(500),
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;

-- Rename columns to match the plan if they exist with different names
DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='events' AND column_name='event_type') THEN
        ALTER TABLE events RENAME COLUMN event_type TO category_old;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='events' AND column_name='start_date') THEN
        ALTER TABLE events RENAME COLUMN start_date TO start_date_time;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='events' AND column_name='end_date') THEN
        ALTER TABLE events RENAME COLUMN end_date TO end_date_time;
    END IF;
     IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='events' AND column_name='created_by') THEN
        ALTER TABLE events RENAME COLUMN created_by TO organizer_id;
    END IF;
END $$;

-- 2. Create event_registrations table
CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    tourist_id VARCHAR(255) NOT NULL,
    ticket_number VARCHAR(100) UNIQUE NOT NULL,
    num_tickets INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    notes TEXT,
    cancellation_reason TEXT,
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_event FOREIGN KEY(event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- 3. Add Indexes for registration
CREATE INDEX IF NOT EXISTS idx_registration_event ON event_registrations(event_id);
CREATE INDEX IF NOT EXISTS idx_registration_tourist ON event_registrations(tourist_id);
CREATE INDEX IF NOT EXISTS idx_registration_number ON event_registrations(ticket_number);

-- 4. Update events indexes
CREATE INDEX IF NOT EXISTS idx_events_organizer ON events(organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_search_params ON events(category, status, start_date_time);
