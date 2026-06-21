-- V42: Add edit tracking columns to mentor_questions and mentor_answers

ALTER TABLE mentor_questions
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_edited BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS edit_count INT DEFAULT 0;

ALTER TABLE mentor_answers
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_edited BOOLEAN DEFAULT FALSE;
