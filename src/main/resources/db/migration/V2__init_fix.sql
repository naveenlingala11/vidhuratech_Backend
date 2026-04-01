ALTER TABLE IF EXISTS interview_questions
    ALTER COLUMN question TYPE TEXT USING question::text;

ALTER TABLE IF EXISTS interview_questions
    ALTER COLUMN answer TYPE TEXT USING answer::text;