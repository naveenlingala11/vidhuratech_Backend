DO $$
BEGIN
    -- Safely alter course_id if it exists and is not bigint
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'curriculum' AND column_name = 'course_id'
    ) THEN
        IF (
            SELECT data_type 
            FROM information_schema.columns 
            WHERE table_name = 'curriculum' AND column_name = 'course_id'
        ) <> 'bigint' THEN
            ALTER TABLE curriculum ALTER COLUMN course_id TYPE bigint USING (course_id::bigint);
        END IF;
    END IF;

    -- Safely alter batch_id if it exists and is not bigint
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'curriculum' AND column_name = 'batch_id'
    ) THEN
        IF (
            SELECT data_type 
            FROM information_schema.columns 
            WHERE table_name = 'curriculum' AND column_name = 'batch_id'
        ) <> 'bigint' THEN
            ALTER TABLE curriculum ALTER COLUMN batch_id TYPE bigint USING (batch_id::bigint);
        END IF;
    END IF;
END $$;
