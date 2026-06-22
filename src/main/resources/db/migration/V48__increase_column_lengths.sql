-- V48: Increase column lengths to TEXT for image URLs, meeting/apply links, and skills to prevent SQLState 22001 (value too long) errors
ALTER TABLE users ALTER COLUMN profile_image_url TYPE TEXT;
ALTER TABLE mentor_profiles ALTER COLUMN verification_document_url TYPE TEXT;
ALTER TABLE mentor_profiles ALTER COLUMN skills TYPE TEXT;
ALTER TABLE mentor_sessions ALTER COLUMN meeting_link TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN apply_link TYPE TEXT;
ALTER TABLE public_challenge_discussions ALTER COLUMN author_profile_image_url TYPE TEXT;
ALTER TABLE training_content ALTER COLUMN file_url TYPE TEXT;
ALTER TABLE invoice ALTER COLUMN payment_screenshot_url TYPE TEXT;
ALTER TABLE mentor_questions ALTER COLUMN media_url TYPE TEXT;
