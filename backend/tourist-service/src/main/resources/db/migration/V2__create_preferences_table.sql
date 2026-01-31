-- Create tourist preferences table
CREATE TABLE tourist_preferences (
    id BIGSERIAL PRIMARY KEY,
    tourist_id BIGINT NOT NULL REFERENCES tourists(id) ON DELETE CASCADE,
    preferred_budget VARCHAR(50),
    travel_style VARCHAR(50),
    dietary_restrictions TEXT[],
    interests TEXT[],
    preferred_languages TEXT[],
    accessibility_needs TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tourist_preferences_uk_tourist UNIQUE (tourist_id)
);

CREATE INDEX idx_tourist_preferences_tourist_id ON tourist_preferences(tourist_id);
