# Day 1 Requirements Freeze

## 1. Project Goal

Build a web-based pharmacy management system to automate medicine stock tracking, supplier handling, billing, and expiry monitoring.

## 2. Roles

- Admin
- Pharmacist

Role rule:
- Pharmacist handles billing and sales operations.

## 3. Functional Modules (MVP)

1. Authentication and role-based access
2. Medicine management (CRUD)
3. Supplier management (CRUD)
4. Batch-wise inventory tracking
5. Billing and invoice generation
6. Expiry alert tracking
7. Reports (daily sales, low stock, expiry list)

## 4. Core Rules

- Do not allow sale of expired medicines.
- Do not allow sale quantity greater than available stock.
- Use batch-wise stock updates for purchase and sale.
- Record each stock movement as a transaction.
- Access control hierarchy: `ADMIN > PHARMACIST`.

## 5. Non-Functional Requirements

- Responsive UI for desktop and tablet.
- Prepared statements for secure DB operations.
- Passwords stored as hashes.
- Clear error handling and user feedback messages.

## 6. Day 1 Done Criteria

- Requirements documented and accepted.
- Modules and scope locked for MVP.
- Project directory scaffold created.
- Initial web.xml and starter JSP pages available.
