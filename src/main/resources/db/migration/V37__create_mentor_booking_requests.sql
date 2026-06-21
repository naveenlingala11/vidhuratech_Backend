-- V37: Create mentor_booking_requests table for in-app booking flow
CREATE TABLE IF NOT EXISTS mentor_booking_requests (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    student_phone VARCHAR(50),
    student_email VARCHAR(255),
    topic VARCHAR(255),
    message TEXT,
    preferred_plan VARCHAR(20) DEFAULT 'HOURLY',
    status VARCHAR(20) DEFAULT 'PENDING',
    mentor_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_mentor FOREIGN KEY (mentor_id) REFERENCES users(id),
    CONSTRAINT fk_booking_student FOREIGN KEY (student_id) REFERENCES users(id)
);
