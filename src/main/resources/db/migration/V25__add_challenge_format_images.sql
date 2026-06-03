ALTER TABLE pseudo_code_challenge
    ADD COLUMN IF NOT EXISTS constraints_image_url TEXT,
    ADD COLUMN IF NOT EXISTS input_format_image_url TEXT,
    ADD COLUMN IF NOT EXISTS output_format_image_url TEXT;