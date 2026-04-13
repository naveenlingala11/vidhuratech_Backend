-- V6__create_invoice_table.sql
CREATE TABLE IF NOT EXISTS invoice (
                                       id VARCHAR(255) PRIMARY KEY,
    lead_phone VARCHAR(50),
    name VARCHAR(255),
    email VARCHAR(255),
    mobile VARCHAR(50),
    student_address TEXT,
    course VARCHAR(255),
    batch VARCHAR(255),
    trainer VARCHAR(255),
    amount DOUBLE PRECISION,
    discount DOUBLE PRECISION,
    scholarship DOUBLE PRECISION,
    paid_amount DOUBLE PRECISION,
    remaining_amount DOUBLE PRECISION,
    installment_enabled BOOLEAN,
    coupon_code VARCHAR(255),
    payment_status VARCHAR(100),
    payment_method VARCHAR(100),
    notes VARCHAR(2000),
    created_at TIMESTAMP
    );