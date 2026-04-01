USE pharmacy_inventory;

INSERT INTO users (full_name, username, password_hash, role, is_active)
VALUES
    ('System Administrator', 'admin', SHA2('admin@123', 256), 'ADMIN', 1),
    ('Default Pharmacist', 'pharmacist', SHA2('pharma@123', 256), 'PHARMACIST', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO suppliers (supplier_code, name, phone, email, gst_no, address_line)
VALUES
    ('SUP-001', 'Wellness Pharma Dist.', '9876543210', 'sales@wellnesspharma.com', '29ABCDE1234F1Z5', 'MG Road, Bengaluru'),
    ('SUP-002', 'LifeCare MedSupply', '9988776655', 'support@lifecaremed.com', '27ABCDE9876K1Z2', 'Andheri East, Mumbai')
ON DUPLICATE KEY UPDATE supplier_code = VALUES(supplier_code);

INSERT INTO medicines (medicine_code, name, category, brand, unit, reorder_level)
VALUES
    ('MED-PCM-500', 'Paracetamol 500', 'Tablet', 'MediCure', 'Strip', 20),
    ('MED-AMX-250', 'Amoxicillin 250', 'Capsule', 'HealPlus', 'Strip', 15),
    ('MED-CTZ-10', 'Cetirizine 10', 'Tablet', 'AllerFree', 'Strip', 20)
ON DUPLICATE KEY UPDATE medicine_code = VALUES(medicine_code);
