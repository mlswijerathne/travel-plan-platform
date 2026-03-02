-- EPIC-7: Geospatial Discovery
-- Ensure latitude/longitude have non-null indexes for TASK-7.1 route corridor and TASK-7.2 regional pulse queries.
-- The columns exist from V1; this migration adds optimised partial indexes for published events with coordinates.

CREATE INDEX idx_events_geo_published
    ON events (latitude, longitude)
    WHERE status = 'PUBLISHED'
      AND latitude IS NOT NULL
      AND longitude IS NOT NULL;
