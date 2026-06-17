-- V37: Add custom, state-specific, sequential customer and employee IDs to the users table.
ALTER TABLE users ADD COLUMN customer_id VARCHAR(50) UNIQUE;
ALTER TABLE users ADD COLUMN employee_id VARCHAR(50) UNIQUE;
