ALTER TABLE mentor_profiles
ADD COLUMN price_per_week DECIMAL(10, 2) DEFAULT 0.00,
ADD COLUMN price_per_month DECIMAL(10, 2) DEFAULT 0.00,
ADD COLUMN availability_days VARCHAR(500) DEFAULT 'monday,tuesday,wednesday,thursday,friday',
ADD COLUMN availability_slots VARCHAR(500) DEFAULT 'evening',
ADD COLUMN allow_daily_sessions BOOLEAN DEFAULT FALSE;

-- Create table for mentor-student associations
CREATE TABLE mentor_student_relations (
    id SERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    progress INTEGER DEFAULT 0,
    target_milestone VARCHAR(255) DEFAULT '',
    last_meeting_date VARCHAR(100) DEFAULT '',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ms_relation_mentor FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_relation_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_ms_relation UNIQUE (mentor_id, student_id)
);

-- Create table for mentor sessions
CREATE TABLE mentor_sessions (
    id SERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    session_date VARCHAR(100) NOT NULL,
    session_time VARCHAR(100) NOT NULL,
    session_type VARCHAR(100) DEFAULT 'Mock Interview',
    meeting_link VARCHAR(1000) NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ms_session_mentor FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_session_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create table for mentor goals
CREATE TABLE mentor_goals (
    id SERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN DEFAULT FALSE,
    due_date VARCHAR(100) DEFAULT 'Today',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ms_goal_mentor FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed some student users if they don't exist
INSERT INTO users (name, email, phone, password, role, active, deleted, notifications_enabled, profile_image_url, created_at)
VALUES 
('Rahul Sharma', 'student.rahul@test.com', '9988776601', '$2a$10$P/RAYcSrETx5jyR6Bdl7g.XHgN9SEPuqRcm./5Ofg0wjmOhcJYFEq', 'STUDENT', true, false, true, '', CURRENT_TIMESTAMP),
('Sneha Reddy', 'student.sneha@test.com', '9988776602', '$2a$10$P/RAYcSrETx5jyR6Bdl7g.XHgN9SEPuqRcm./5Ofg0wjmOhcJYFEq', 'STUDENT', true, false, true, '', CURRENT_TIMESTAMP),
('Kiran Kumar', 'student.kiran@test.com', '9988776603', '$2a$10$P/RAYcSrETx5jyR6Bdl7g.XHgN9SEPuqRcm./5Ofg0wjmOhcJYFEq', 'STUDENT', true, false, true, '', CURRENT_TIMESTAMP),
('Akhil G', 'student.akhil@test.com', '9988776604', '$2a$10$P/RAYcSrETx5jyR6Bdl7g.XHgN9SEPuqRcm./5Ofg0wjmOhcJYFEq', 'STUDENT', true, false, true, '', CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Seed relations for our main mentor 'naveen.lingala@vidhuratech.com'
INSERT INTO mentor_student_relations (mentor_id, student_id, progress, target_milestone, last_meeting_date, status)
SELECT 
    m.id, 
    s.id, 
    CASE WHEN s.email = 'student.rahul@test.com' THEN 78 WHEN s.email = 'student.sneha@test.com' THEN 91 WHEN s.email = 'student.kiran@test.com' THEN 82 ELSE 65 END,
    CASE WHEN s.email = 'student.rahul@test.com' THEN 'Spring Security Setup' WHEN s.email = 'student.sneha@test.com' THEN 'Mock Technical Interview' WHEN s.email = 'student.kiran@test.com' THEN 'AWS Deployment Pipeline' ELSE 'Angular Form Validations' END,
    CASE WHEN s.email = 'student.rahul@test.com' THEN '2 days ago' WHEN s.email = 'student.sneha@test.com' THEN 'Yesterday' WHEN s.email = 'student.kiran@test.com' THEN '4 days ago' ELSE '1 week ago' END,
    'ACTIVE'
FROM users m, users s
WHERE m.email = 'naveen.lingala@vidhuratech.com' AND s.email IN ('student.rahul@test.com', 'student.sneha@test.com', 'student.kiran@test.com', 'student.akhil@test.com')
ON CONFLICT DO NOTHING;

-- Seed sessions for 'naveen.lingala@vidhuratech.com'
INSERT INTO mentor_sessions (mentor_id, student_id, session_date, session_time, session_type, meeting_link, status)
SELECT 
    m.id, 
    s.id,
    CASE WHEN s.email = 'student.rahul@test.com' THEN 'Today' WHEN s.email = 'student.sneha@test.com' THEN 'Tomorrow' ELSE 'June 22, 2026' END,
    CASE WHEN s.email = 'student.rahul@test.com' THEN '4:00 PM' WHEN s.email = 'student.sneha@test.com' THEN '11:00 AM' ELSE '6:30 PM' END,
    CASE WHEN s.email = 'student.rahul@test.com' THEN 'Resume Improvement' WHEN s.email = 'student.sneha@test.com' THEN 'Technical Mock Drill' ELSE 'Architecture Review' END,
    CASE WHEN s.email = 'student.rahul@test.com' THEN 'https://zoom.us/j/99988877761' WHEN s.email = 'student.sneha@test.com' THEN 'https://zoom.us/j/99988877762' ELSE 'https://zoom.us/j/99988877763' END,
    'SCHEDULED'
FROM users m, users s
WHERE m.email = 'naveen.lingala@vidhuratech.com' AND s.email IN ('student.rahul@test.com', 'student.sneha@test.com', 'student.kiran@test.com');

-- Seed goals for 'naveen.lingala@vidhuratech.com'
INSERT INTO mentor_goals (mentor_id, title, description, completed, due_date)
SELECT 
    id, 
    'Mock Interview Feedback', 
    'Fill out the evaluation scorecard for Sneha''s Java assessment', 
    false, 
    'Today'
FROM users WHERE email = 'naveen.lingala@vidhuratech.com'
UNION ALL
SELECT 
    id, 
    'Review Angular Code', 
    'Inspect the github PR for Akhil''s reactive forms task', 
    false, 
    'Tomorrow'
FROM users WHERE email = 'naveen.lingala@vidhuratech.com'
UNION ALL
SELECT 
    id, 
    'Submit Monthly Report', 
    'Report hours dedicated to student cohorts this month', 
    true, 
    'Completed'
FROM users WHERE email = 'naveen.lingala@vidhuratech.com';
