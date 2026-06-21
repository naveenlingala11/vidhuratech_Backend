-- V40: Add media attachments and likes to Q&A/Discussion Forum
ALTER TABLE mentor_questions 
    ADD COLUMN IF NOT EXISTS media_url VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS media_type VARCHAR(50) DEFAULT 'NONE';

CREATE TABLE IF NOT EXISTS mentor_question_likes (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_question_like_user UNIQUE (question_id, user_id),
    CONSTRAINT fk_like_question FOREIGN KEY (question_id) REFERENCES mentor_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
