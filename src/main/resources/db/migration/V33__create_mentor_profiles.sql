CREATE TABLE mentor_profiles (
    user_id BIGINT PRIMARY KEY,
    current_company VARCHAR(255),
    job_role VARCHAR(255),
    years_of_experience DOUBLE PRECISION DEFAULT 0.0,
    biography TEXT,
    skills VARCHAR(1000),
    languages VARCHAR(255),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    rating DOUBLE PRECISION DEFAULT 5.0,
    reviews_count INT DEFAULT 0,
    price_per_hour DECIMAL(10, 2) DEFAULT 0.00,
    featured BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
