CREATE TABLE IF NOT EXISTS training_content (
                                                id BIGSERIAL PRIMARY KEY,
                                                batch_id BIGINT,
                                                trainer_email VARCHAR(255),
    type VARCHAR(30) CHECK (type IN ('PRACTICE', 'MATERIAL', 'NOTE')),
    title VARCHAR(255),
    description VARCHAR(2000),
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    file_data BYTEA,
    created_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_training_content_trainer_email
    ON training_content (trainer_email);

CREATE INDEX IF NOT EXISTS idx_training_content_batch_id
    ON training_content (batch_id);

CREATE INDEX IF NOT EXISTS idx_training_content_type
    ON training_content (type);