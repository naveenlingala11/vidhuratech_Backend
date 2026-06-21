-- V43: Add answer voting system, pinned posts, and view counter

CREATE TABLE IF NOT EXISTS mentor_answer_votes (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vote_answer FOREIGN KEY (answer_id) REFERENCES mentor_answers(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_answer_user_vote UNIQUE (answer_id, user_id)
);

ALTER TABLE mentor_questions
    ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS pin_order INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS views_count INT DEFAULT 0;
