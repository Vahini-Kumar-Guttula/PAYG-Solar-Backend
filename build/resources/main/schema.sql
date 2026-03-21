-- =====================================================
-- PAYG Solar System Database Schema
-- Database: PostgreSQL 14+
-- =====================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS device_assignments CASCADE;
DROP TABLE IF EXISTS devices CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- =====================================================
-- CUSTOMERS TABLE
-- =====================================================
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Indexes for customers table
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_phone ON customers(phone_number);
CREATE INDEX idx_customer_status ON customers(status);
CREATE INDEX idx_customer_created_at ON customers(created_at);

-- =====================================================
-- DEVICES TABLE
-- =====================================================
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(100) NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_device_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    CONSTRAINT chk_device_cost CHECK (total_cost > 0)
);

-- Indexes for devices table
CREATE INDEX idx_device_serial ON devices(serial_number);
CREATE INDEX idx_device_status ON devices(status);
CREATE INDEX idx_device_model ON devices(model);
CREATE INDEX idx_device_created_at ON devices(created_at);

-- =====================================================
-- DEVICE_ASSIGNMENTS TABLE
-- =====================================================
CREATE TABLE device_assignments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0,
    remaining_balance DECIMAL(10, 2) NOT NULL,
    payment_plan VARCHAR(20) NOT NULL,
    installment_amount DECIMAL(10, 2) NOT NULL,
    assignment_date DATE NOT NULL,
    next_payment_due_date DATE,
    last_payment_date DATE,
    missed_payments INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_assignment_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT chk_assignment_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'DEFAULTED', 'CANCELLED')),
    CONSTRAINT chk_payment_plan CHECK (payment_plan IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT chk_assignment_amounts CHECK (amount_paid >= 0 AND remaining_balance >= 0 AND total_cost > 0),
    CONSTRAINT chk_installment_amount CHECK (installment_amount > 0)
);

-- Indexes for device_assignments table
CREATE INDEX idx_assignment_customer ON device_assignments(customer_id);
CREATE INDEX idx_assignment_device ON device_assignments(device_id);
CREATE INDEX idx_assignment_status ON device_assignments(status);
CREATE INDEX idx_assignment_next_payment ON device_assignments(next_payment_due_date);
CREATE INDEX idx_assignment_created_at ON device_assignments(created_at);
CREATE INDEX idx_assignment_customer_device ON device_assignments(customer_id, device_id);

-- =====================================================
-- PAYMENTS TABLE
-- =====================================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP NOT NULL,
    external_reference VARCHAR(100) UNIQUE,
    description VARCHAR(500),
    gateway_response VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_assignment FOREIGN KEY (assignment_id) REFERENCES device_assignments(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('MOBILE_MONEY', 'BANK_TRANSFER', 'CASH', 'CARD')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

-- Indexes for payments table
CREATE INDEX idx_payment_customer ON payments(customer_id);
CREATE INDEX idx_payment_assignment ON payments(assignment_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_date ON payments(payment_date);
CREATE INDEX idx_payment_reference ON payments(external_reference);
CREATE INDEX idx_payment_created_at ON payments(created_at);

-- =====================================================
-- TRIGGERS FOR UPDATED_AT TIMESTAMPS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for customers table
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for devices table
CREATE TRIGGER update_devices_updated_at
    BEFORE UPDATE ON devices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for device_assignments table
CREATE TRIGGER update_device_assignments_updated_at
    BEFORE UPDATE ON device_assignments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone_number, address, city, country, status) VALUES
('John', 'Doe', 'john.doe@example.com', '+254712345678', '123 Main St', 'Nairobi', 'Kenya', 'ACTIVE'),
('Jane', 'Smith', 'jane.smith@example.com', '+254723456789', '456 Oak Ave', 'Mombasa', 'Kenya', 'ACTIVE'),
('Bob', 'Johnson', 'bob.johnson@example.com', '+254734567890', '789 Pine Rd', 'Kisumu', 'Kenya', 'ACTIVE');

-- Insert sample devices
INSERT INTO devices (serial_number, model, manufacturer, total_cost, status, description) VALUES
('SUN-2024-001', 'Solar Home System 50W', 'Sun King', 15000.00, 'INACTIVE', '50W solar panel with battery and LED lights'),
('SUN-2024-002', 'Solar Home System 100W', 'Sun King', 25000.00, 'INACTIVE', '100W solar panel with battery, LED lights, and phone charging'),
('SUN-2024-003', 'Solar Home System 50W', 'Sun King', 15000.00, 'INACTIVE', '50W solar panel with battery and LED lights'),
('SUN-2024-004', 'Solar Home System 200W', 'Sun King', 45000.00, 'INACTIVE', '200W solar panel with battery, LED lights, TV, and phone charging');

-- Insert sample device assignments
INSERT INTO device_assignments (
    customer_id, device_id, total_cost, amount_paid, remaining_balance,
    payment_plan, installment_amount, assignment_date, next_payment_due_date,
    last_payment_date, missed_payments, status
) VALUES
(1, 1, 15000.00, 5000.00, 10000.00, 'WEEKLY', 500.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '7 days', CURRENT_DATE - INTERVAL '7 days', 0, 'ACTIVE'),
(2, 2, 25000.00, 10000.00, 15000.00, 'MONTHLY', 2500.00, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '30 days', CURRENT_DATE - INTERVAL '30 days', 0, 'ACTIVE');

-- Update device status to ACTIVE for assigned devices
UPDATE devices SET status = 'ACTIVE' WHERE id IN (1, 2);

-- Insert sample payments
INSERT INTO payments (
    customer_id, assignment_id, amount, payment_method, status,
    payment_date, external_reference, description
) VALUES
(1, 1, 500.00, 'MOBILE_MONEY', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '7 days', 'PAY-1234567890-ABC123', 'Weekly payment'),
(1, 1, 500.00, 'MOBILE_MONEY', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '14 days', 'PAY-1234567891-ABC124', 'Weekly payment'),
(2, 2, 2500.00, 'MOBILE_MONEY', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '30 days', 'PAY-1234567892-ABC125', 'Monthly payment');

-- =====================================================
-- USEFUL QUERIES
-- =====================================================

-- Get all active assignments with customer and device details
-- SELECT 
--     da.id as assignment_id,
--     c.first_name || ' ' || c.last_name as customer_name,
--     c.email,
--     d.serial_number,
--     d.model,
--     da.total_cost,
--     da.amount_paid,
--     da.remaining_balance,
--     da.payment_plan,
--     da.next_payment_due_date,
--     da.missed_payments,
--     d.status as device_status
-- FROM device_assignments da
-- JOIN customers c ON da.customer_id = c.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE da.status = 'ACTIVE';

-- Get payment history for a customer
-- SELECT 
--     p.id,
--     p.amount,
--     p.payment_method,
--     p.status,
--     p.payment_date,
--     p.external_reference,
--     d.serial_number,
--     d.model
-- FROM payments p
-- JOIN device_assignments da ON p.assignment_id = da.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE p.customer_id = 1
-- ORDER BY p.payment_date DESC;

-- Get overdue assignments (7 days overdue)
-- SELECT 
--     da.id,
--     c.first_name || ' ' || c.last_name as customer_name,
--     c.phone_number,
--     d.serial_number,
--     da.next_payment_due_date,
--     CURRENT_DATE - da.next_payment_due_date as days_overdue,
--     da.remaining_balance
-- FROM device_assignments da
-- JOIN customers c ON da.customer_id = c.id
-- JOIN devices d ON da.device_id = d.id
-- WHERE da.status = 'ACTIVE'
--   AND da.next_payment_due_date < CURRENT_DATE - INTERVAL '7 days';

-- Get total revenue
-- SELECT 
--     SUM(amount) as total_revenue,
--     COUNT(*) as total_payments
-- FROM payments
-- WHERE status = 'COMPLETED';

-- Get customer payment statistics
-- SELECT 
--     c.id,
--     c.first_name || ' ' || c.last_name as customer_name,
--     COUNT(p.id) as total_payments,
--     SUM(p.amount) as total_paid,
--     SUM(da.remaining_balance) as total_remaining
-- FROM customers c
-- LEFT JOIN device_assignments da ON c.id = da.customer_id
-- LEFT JOIN payments p ON c.id = p.customer_id AND p.status = 'COMPLETED'
-- GROUP BY c.id, c.first_name, c.last_name;

-- =====================================================
-- GRANTS (Adjust based on your user setup)
-- =====================================================

-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO payg_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO payg_user;

-- =====================================================
-- END OF SCHEMA
-- =====================================================
