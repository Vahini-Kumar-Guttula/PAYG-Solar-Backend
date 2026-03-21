-- MySQL Database Schema for PAYG Solar System
-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS device_assignments;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS customers;

-- Create customers table
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_email (email),
    INDEX idx_customer_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create devices table
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_serial (serial_number),
    INDEX idx_device_status (status),
    CONSTRAINT chk_device_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create device_assignments table
CREATE TABLE device_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) DEFAULT 0.00,
    payment_frequency VARCHAR(20) NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    last_payment_date DATE,
    next_payment_due_date DATE,
    missed_payments_count INT DEFAULT 0,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    INDEX idx_assignment_customer (customer_id),
    INDEX idx_assignment_device (device_id),
    INDEX idx_assignment_due_date (next_payment_due_date),
    CONSTRAINT fk_assignment_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_frequency CHECK (payment_frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT chk_total_cost_positive CHECK (total_cost > 0),
    CONSTRAINT chk_payment_amount_positive CHECK (payment_amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    retry_count INT DEFAULT 0,
    INDEX idx_payment_assignment (assignment_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_status (status),
    CONSTRAINT fk_payment_assignment FOREIGN KEY (assignment_id) REFERENCES device_assignments(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_payment_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing
INSERT INTO customers (name, email, phone, address) VALUES
('John Doe', 'john.doe@example.com', '+254712345678', '123 Main St, Nairobi, Kenya'),
('Jane Smith', 'jane.smith@example.com', '+254723456789', '456 Oak Ave, Mombasa, Kenya'),
('Bob Johnson', 'bob.johnson@example.com', '+254734567890', '789 Pine Rd, Kisumu, Kenya');

INSERT INTO devices (serial_number, model, status) VALUES
('SN-2024-001', 'SolarMax 100W', 'INACTIVE'),
('SN-2024-002', 'SolarMax 200W', 'INACTIVE'),
('SN-2024-003', 'SolarMax 300W', 'INACTIVE'),
('SN-2024-004', 'SolarMax 100W', 'INACTIVE'),
('SN-2024-005', 'SolarMax 200W', 'INACTIVE');

-- Assign devices to customers
INSERT INTO device_assignments (customer_id, device_id, total_cost, amount_paid, payment_frequency, payment_amount, last_payment_date, next_payment_due_date, missed_payments_count) VALUES
(1, 1, 50000.00, 5000.00, 'WEEKLY', 500.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 0),
(2, 2, 75000.00, 7500.00, 'WEEKLY', 750.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 0),
(3, 3, 100000.00, 0.00, 'MONTHLY', 5000.00, NULL, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 0);

-- Update device status to ACTIVE for assigned devices
UPDATE devices SET status = 'ACTIVE' WHERE id IN (1, 2, 3);

-- Insert sample payments
INSERT INTO payments (assignment_id, amount, payment_date, status, transaction_id) VALUES
(1, 500.00, NOW(), 'COMPLETED', 'TXN-2024-001'),
(1, 500.00, DATE_SUB(NOW(), INTERVAL 7 DAY), 'COMPLETED', 'TXN-2024-002'),
(2, 750.00, NOW(), 'COMPLETED', 'TXN-2024-003'),
(2, 750.00, DATE_SUB(NOW(), INTERVAL 7 DAY), 'COMPLETED', 'TXN-2024-004');

-- Useful queries for monitoring

-- Query 1: Get all active assignments with customer and device details
-- SELECT 
--     c.name AS customer_name,
--     c.email,
--     d.serial_number,
--     d.model,
--     d.status AS device_status,
--     da.total_cost,
--     da.amount_paid,
--     da.payment_frequency,
--     da.next_payment_due_date,
--     da.missed_payments_count
-- FROM device_assignments da
-- JOIN customers c ON da.customer_id = c.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE da.completed_at IS NULL;

-- Query 2: Get overdue assignments (payment due date passed)
-- SELECT 
--     c.name AS customer_name,
--     c.phone,
--     d.serial_number,
--     da.next_payment_due_date,
--     DATEDIFF(CURDATE(), da.next_payment_due_date) AS days_overdue,
--     da.missed_payments_count
-- FROM device_assignments da
-- JOIN customers c ON da.customer_id = c.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE da.next_payment_due_date < CURDATE()
--   AND da.completed_at IS NULL;

-- Query 3: Get payment history for a customer
-- SELECT 
--     c.name AS customer_name,
--     d.serial_number,
--     p.amount,
--     p.payment_date,
--     p.status,
--     p.transaction_id
-- FROM payments p
-- JOIN device_assignments da ON p.assignment_id = da.id
-- JOIN customers c ON da.customer_id = c.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE c.id = 1
-- ORDER BY p.payment_date DESC;

-- Query 4: Get total revenue and outstanding balance
-- SELECT 
--     SUM(amount_paid) AS total_revenue,
--     SUM(total_cost - amount_paid) AS outstanding_balance,
--     COUNT(*) AS active_assignments
-- FROM device_assignments
-- WHERE completed_at IS NULL;
