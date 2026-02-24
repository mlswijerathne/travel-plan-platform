-- Saga orchestration table for multi-provider booking transactions
CREATE TABLE saga_orchestration (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    saga_state VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    current_step INT NOT NULL DEFAULT 0,
    total_steps INT NOT NULL DEFAULT 0,
    completed_steps TEXT,
    failure_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    timeout_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_saga_booking_id ON saga_orchestration(booking_id);
CREATE INDEX idx_saga_state ON saga_orchestration(saga_state);

-- Add refund tracking to bookings
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS refund_amount DECIMAL(12, 2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS refund_policy VARCHAR(50);
