USE pharmacy_inventory;

SET SQL_SAFE_UPDATES = 0;

DELETE FROM users WHERE role = 'CASHIER' OR username = 'cashier';

ALTER TABLE users
    MODIFY COLUMN role ENUM('ADMIN', 'PHARMACIST') NOT NULL;
