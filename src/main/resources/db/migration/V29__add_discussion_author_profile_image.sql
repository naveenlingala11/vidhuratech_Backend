ALTER TABLE public_challenge_discussions
    ADD COLUMN IF NOT EXISTS author_profile_image_url VARCHAR(1000);