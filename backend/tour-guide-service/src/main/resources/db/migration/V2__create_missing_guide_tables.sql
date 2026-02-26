CREATE TABLE guide_availability_overrides (
    id UUID PRIMARY KEY,
    available_date DATE,
    start_time TIME,
    end_time TIME,
    is_available BOOLEAN NOT NULL,
    guide_id BIGINT,
    CONSTRAINT fk_guide_availability FOREIGN KEY (guide_id) REFERENCES tour_guides(id) ON DELETE CASCADE
);

CREATE TABLE guide_languages (
    id UUID PRIMARY KEY,
    language VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL,
    guide_id BIGINT,
    CONSTRAINT fk_guide_language FOREIGN KEY (guide_id) REFERENCES tour_guides(id) ON DELETE CASCADE
);

CREATE TABLE guide_skills (
    id UUID PRIMARY KEY,
    skill_name VARCHAR(255) NOT NULL,
    guide_id BIGINT,
    CONSTRAINT fk_guide_skill FOREIGN KEY (guide_id) REFERENCES tour_guides(id) ON DELETE CASCADE
);

CREATE TABLE guide_specializations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    guide_id BIGINT,
    CONSTRAINT fk_guide_specialization FOREIGN KEY (guide_id) REFERENCES tour_guides(id) ON DELETE CASCADE
);
