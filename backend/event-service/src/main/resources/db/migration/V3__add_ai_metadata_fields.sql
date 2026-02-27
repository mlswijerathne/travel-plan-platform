-- TASK-6.1: Add AI-enhanced metadata fields
ALTER TABLE events 
ADD COLUMN vibes JSONB DEFAULT '[]',
ADD COLUMN is_authentic_cultural BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_events_vibes ON events USING GIN (vibes);
CREATE INDEX idx_events_cultural ON events (is_authentic_cultural);
