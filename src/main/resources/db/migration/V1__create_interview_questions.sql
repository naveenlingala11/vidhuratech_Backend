CREATE TABLE IF NOT EXISTS interview_questions (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   company VARCHAR(255),
    role VARCHAR(255),
    type VARCHAR(255),
    topic VARCHAR(255),
    difficulty VARCHAR(255),
    question TEXT,
    answer TEXT
    );