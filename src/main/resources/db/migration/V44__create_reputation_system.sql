-- V44: Create reputation system table and user columns

CREATE TABLE IF NOT EXISTS user_reputation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    reference_type VARCHAR(30), -- 'QUESTION', 'ANSWER', 'VOTE', etc.
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reputation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS reputation_points INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS reputation_level VARCHAR(20) DEFAULT 'BEGINNER';
