-- V36: Create mentor_reviews table for student reviews/ratings
CREATE TABLE IF NOT EXISTS mentor_reviews (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    rating INT NOT NULL DEFAULT 5,
    review_text TEXT,
    session_type VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_mentor FOREIGN KEY (mentor_id) REFERENCES users(id),
    CONSTRAINT fk_review_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT uq_mentor_student_review UNIQUE (mentor_id, student_id)
);
