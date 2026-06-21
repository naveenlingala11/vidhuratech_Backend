-- V46: Add parent_answer_id, depth, and mentioned_user_ids to mentor_answers

ALTER TABLE mentor_answers
    ADD COLUMN IF NOT EXISTS parent_answer_id BIGINT,
    ADD COLUMN IF NOT EXISTS depth INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS mentioned_user_ids TEXT,
    ADD CONSTRAINT fk_answer_parent FOREIGN KEY (parent_answer_id) REFERENCES mentor_answers(id) ON DELETE CASCADE;
