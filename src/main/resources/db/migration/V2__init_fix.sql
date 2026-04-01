ALTER TABLE interview_questions
    ALTER COLUMN question TYPE TEXT USING question::text;

ALTER TABLE interview_questions
    ALTER COLUMN answer TYPE TEXT USING answer::text;