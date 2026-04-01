CREATE DATABASE IF NOT EXISTS pharmacy_inventory
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacy_inventory;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'PHARMACIST') NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(120),
    gst_no VARCHAR(30),
    address_line VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80),
    brand VARCHAR(80),
    unit VARCHAR(20) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicine_batches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_id BIGINT NOT NULL,
    supplier_id BIGINT,
    batch_no VARCHAR(60) NOT NULL,
    mfg_date DATE,
    exp_date DATE NOT NULL,
    qty_in_stock INT NOT NULL DEFAULT 0,
    cost_price DECIMAL(10, 2) NOT NULL,
    sell_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    CONSTRAINT fk_batch_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT uq_medicine_batch UNIQUE (medicine_id, batch_no),
    CONSTRAINT chk_batch_qty CHECK (qty_in_stock >= 0)
);

CREATE TABLE IF NOT EXISTS purchases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    invoice_no VARCHAR(60) NOT NULL,
    purchase_date DATE NOT NULL,
    sub_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uq_supplier_invoice UNIQUE (supplier_id, invoice_no)
);

CREATE TABLE IF NOT EXISTS purchase_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    qty INT NOT NULL,
    rate DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_item_batch FOREIGN KEY (batch_id) REFERENCES medicine_batches(id),
    CONSTRAINT chk_purchase_item_qty CHECK (qty > 0)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_no VARCHAR(60) NOT NULL UNIQUE,
    customer_name VARCHAR(120),
    sale_date DATETIME NOT NULL,
    sub_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_mode ENUM('CASH', 'CARD', 'UPI', 'MIXED') NOT NULL DEFAULT 'CASH',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS sale_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    qty INT NOT NULL,
    rate DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    line_total DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_item_batch FOREIGN KEY (batch_id) REFERENCES medicine_batches(id),
    CONSTRAINT chk_sale_item_qty CHECK (qty > 0)
);

CREATE TABLE IF NOT EXISTS stock_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_id BIGINT NOT NULL,
    txn_type ENUM(
        'PURCHASE_IN',
        'SALE_OUT',
        'ADJUSTMENT_IN',
        'ADJUSTMENT_OUT',
        'RETURN_IN',
        'RETURN_OUT',
        'EXPIRED_OUT'
    ) NOT NULL,
    qty INT NOT NULL,
    ref_type ENUM('PURCHASE', 'SALE', 'ADJUSTMENT', 'RETURN', 'EXPIRED', 'OPENING') NOT NULL,
    ref_id BIGINT,
    note VARCHAR(255),
    txn_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_stock_txn_batch FOREIGN KEY (batch_id) REFERENCES medicine_batches(id),
    CONSTRAINT fk_stock_txn_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_stock_txn_qty CHECK (qty > 0)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_name VARCHAR(60) NOT NULL,
    entity_id BIGINT NOT NULL,
    action ENUM('INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT') NOT NULL,
    old_data JSON,
    new_data JSON,
    changed_by BIGINT,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_medicines_name ON medicines(name);
CREATE INDEX idx_batches_exp_date ON medicine_batches(exp_date);
CREATE INDEX idx_batches_qty ON medicine_batches(qty_in_stock);
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_stock_txn_date ON stock_transactions(txn_date);
