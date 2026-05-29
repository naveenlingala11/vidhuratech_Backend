ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS featured_on_home BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS featured_rank INTEGER NOT NULL DEFAULT 100,
    ADD COLUMN IF NOT EXISTS auto_monthly_batch_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS monthly_batch_duration_months INTEGER NOT NULL DEFAULT 3;

ALTER TABLE batches
    ADD COLUMN IF NOT EXISTS auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS batch_month VARCHAR(7);

CREATE TABLE IF NOT EXISTS course_trainer_assignments (
                                                          id BIGSERIAL PRIMARY KEY,
                                                          course_id BIGINT NOT NULL,
                                                          trainer_id BIGINT NOT NULL,
                                                          active BOOLEAN NOT NULL DEFAULT TRUE,
                                                          assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                          assigned_by_user_id BIGINT,

                                                          CONSTRAINT fk_course_trainer_assignment_course
                                                          FOREIGN KEY (course_id) REFERENCES courses(id),

    CONSTRAINT fk_course_trainer_assignment_trainer
    FOREIGN KEY (trainer_id) REFERENCES users(id)
    );

CREATE INDEX IF NOT EXISTS idx_course_trainer_assignment_course
    ON course_trainer_assignments(course_id);

CREATE INDEX IF NOT EXISTS idx_course_trainer_assignment_trainer
    ON course_trainer_assignments(trainer_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_course_trainer_assignment_active
    ON course_trainer_assignments(course_id, trainer_id)
    WHERE active = true;

CREATE UNIQUE INDEX IF NOT EXISTS ux_auto_batch_course_month
    ON batches(course_id, batch_month)
    WHERE auto_generated = true;