-- V41: Add is_solved column to mentor_questions table
ALTER TABLE mentor_questions 
    ADD COLUMN IF NOT EXISTS is_solved BOOLEAN DEFAULT FALSE;
