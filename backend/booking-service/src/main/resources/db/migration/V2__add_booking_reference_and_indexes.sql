-- Add booking_reference column for unique booking identifiers
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS booking_reference VARCHAR(50);
CREATE UNIQUE INDEX IF NOT EXISTS idx_bookings_reference ON bookings(booking_reference);

-- Add cancellation_reason column
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- Add provider-level indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_booking_items_provider ON booking_items(provider_type, provider_id);
CREATE INDEX IF NOT EXISTS idx_booking_items_status ON booking_items(status);
CREATE INDEX IF NOT EXISTS idx_booking_items_dates ON booking_items(start_date, end_date);
