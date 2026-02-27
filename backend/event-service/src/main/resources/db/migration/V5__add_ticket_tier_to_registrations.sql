-- TASK-5.2: Add ticket_tier_id to event_registrations
ALTER TABLE event_registrations
ADD COLUMN ticket_tier_id BIGINT REFERENCES ticket_tiers(id) ON DELETE SET NULL;
