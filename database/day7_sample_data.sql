USE pharmacy_inventory;

-- Additional pharmacist users (idempotent by username)
INSERT INTO users (full_name, username, password_hash, role, is_active)
VALUES
    ('Rahul Verma', 'pharma_rahul', SHA2('rahul@123', 256), 'PHARMACIST', 1),
    ('Sneha Patel', 'pharma_sneha', SHA2('sneha@123', 256), 'PHARMACIST', 1),
    ('Amit Joshi', 'pharma_amit', SHA2('amit@123', 256), 'PHARMACIST', 1)
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    role = 'PHARMACIST',
    is_active = 1;

-- Additional suppliers
INSERT INTO suppliers (supplier_code, name, phone, email, gst_no, address_line)
VALUES
    ('SUP-003', 'CureLink Distributors', '9871112233', 'ops@curelink.in', '24ABCDE4567H1Z9', 'Navrangpura, Ahmedabad'),
    ('SUP-004', 'MediRoute Wholesale', '9822003344', 'sales@mediroute.in', '19ABCDE7788J1Z4', 'Shivaji Nagar, Pune'),
    ('SUP-005', 'HealthBridge Agencies', '9899001122', 'hello@healthbridge.in', '07ABCDE2233L1Z6', 'Karol Bagh, Delhi'),
    ('SUP-006', 'ZenCare Pharma Trade', '9776655443', 'contact@zencare.in', '33ABCDE1122M1Z3', 'MVP Colony, Visakhapatnam')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    phone = VALUES(phone),
    email = VALUES(email),
    gst_no = VALUES(gst_no),
    address_line = VALUES(address_line);

-- Additional medicines
INSERT INTO medicines (medicine_code, name, category, brand, unit, reorder_level)
VALUES
    ('MED-AZM-500', 'Azithromycin 500', 'Tablet', 'BioMedix', 'Strip', 15),
    ('MED-DIC-50', 'Diclofenac 50', 'Tablet', 'PainRelief', 'Strip', 20),
    ('MED-ORS-01', 'ORS Sachet', 'Sachet', 'HydraPlus', 'Packet', 30),
    ('MED-PAN-40', 'Pantoprazole 40', 'Tablet', 'GastroSafe', 'Strip', 20),
    ('MED-MTF-500', 'Metformin 500', 'Tablet', 'DiaCare', 'Strip', 25),
    ('MED-AML-5', 'Amlodipine 5', 'Tablet', 'CardioZen', 'Strip', 20),
    ('MED-VITC-500', 'Vitamin C 500', 'Tablet', 'NutriLife', 'Strip', 18),
    ('MED-IBU-400', 'Ibuprofen 400', 'Tablet', 'PainRelief', 'Strip', 20),
    ('MED-LEV-500', 'Levofloxacin 500', 'Tablet', 'BioMedix', 'Strip', 12),
    ('MED-INS-GLA', 'Insulin Glargine', 'Injection', 'GlucoGuard', 'Vial', 10)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category = VALUES(category),
    brand = VALUES(brand),
    unit = VALUES(unit),
    reorder_level = VALUES(reorder_level);

-- Seed medicine batches (only if specific batch does not exist)
INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-PCM-2501', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 210 DAY), 180, 8.50, 12.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-003'
WHERE m.medicine_code = 'MED-PCM-500'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-PCM-2501'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-AMX-2502', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 160 DAY), 120, 24.00, 34.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-004'
WHERE m.medicine_code = 'MED-AMX-250'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-AMX-2502'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-CTZ-2503', DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_ADD(CURDATE(), INTERVAL 95 DAY), 140, 5.20, 8.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-005'
WHERE m.medicine_code = 'MED-CTZ-10'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-CTZ-2503'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-AZM-2504', DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), 85, 62.00, 89.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-006'
WHERE m.medicine_code = 'MED-AZM-500'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-AZM-2504'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-DIC-2505', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 60, 9.00, 14.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-003'
WHERE m.medicine_code = 'MED-DIC-50'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-DIC-2505'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-ORS-2506', DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 300 DAY), 220, 12.00, 18.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-004'
WHERE m.medicine_code = 'MED-ORS-01'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-ORS-2506'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-PAN-2507', DATE_SUB(CURDATE(), INTERVAL 75 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY), 130, 18.00, 28.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-005'
WHERE m.medicine_code = 'MED-PAN-40'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-PAN-2507'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-MTF-2508', DATE_SUB(CURDATE(), INTERVAL 80 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 110, 16.00, 24.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-006'
WHERE m.medicine_code = 'MED-MTF-500'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-MTF-2508'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-AML-2509', DATE_SUB(CURDATE(), INTERVAL 95 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 40, 22.00, 32.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-003'
WHERE m.medicine_code = 'MED-AML-5'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-AML-2509'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-VITC-2510', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 280 DAY), 150, 10.00, 16.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-004'
WHERE m.medicine_code = 'MED-VITC-500'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-VITC-2510'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-IBU-2511', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 240 DAY), 125, 14.00, 21.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-005'
WHERE m.medicine_code = 'MED-IBU-400'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-IBU-2511'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-LEV-2512', DATE_SUB(CURDATE(), INTERVAL 55 DAY), DATE_ADD(CURDATE(), INTERVAL 75 DAY), 70, 75.00, 105.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-006'
WHERE m.medicine_code = 'MED-LEV-500'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-LEV-2512'
  );

INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-INS-2513', DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_ADD(CURDATE(), INTERVAL 330 DAY), 55, 420.00, 560.00
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-004'
WHERE m.medicine_code = 'MED-INS-GLA'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-INS-2513'
  );

-- One batch intentionally expired for expiry-report testing
INSERT INTO medicine_batches (medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price)
SELECT m.id, s.id, 'BCH-DIC-OLD1', DATE_SUB(CURDATE(), INTERVAL 240 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 18, 8.50, 13.50
FROM medicines m
LEFT JOIN suppliers s ON s.supplier_code = 'SUP-003'
WHERE m.medicine_code = 'MED-DIC-50'
  AND NOT EXISTS (
      SELECT 1 FROM medicine_batches b WHERE b.medicine_id = m.id AND b.batch_no = 'BCH-DIC-OLD1'
  );

-- Create opening transactions for any batch missing an OPENING stock transaction
INSERT INTO stock_transactions (batch_id, txn_type, qty, ref_type, ref_id, note, txn_date, created_by)
SELECT b.id, 'PURCHASE_IN', b.qty_in_stock, 'OPENING', NULL, 'Sample data opening stock', NOW(),
       (SELECT id FROM users WHERE username = 'admin' LIMIT 1)
FROM medicine_batches b
LEFT JOIN stock_transactions st ON st.batch_id = b.id AND st.ref_type = 'OPENING'
WHERE st.id IS NULL
  AND b.qty_in_stock > 0;
