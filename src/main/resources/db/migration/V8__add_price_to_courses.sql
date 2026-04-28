-- Step 1: Add column nullable first
ALTER TABLE courses ADD COLUMN price DOUBLE PRECISION;

-- Step 2: Backfill existing data safely
UPDATE courses
SET price =
        CASE
            WHEN LOWER(title) LIKE '%java%' THEN 3499
            WHEN LOWER(title) LIKE '%python%' THEN 2999
            ELSE 1999
            END
WHERE price IS NULL;

-- Step 3: Make it NOT NULL
ALTER TABLE courses ALTER COLUMN price SET NOT NULL;