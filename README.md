# Pharmacy Inventory Web App

This repository contains a Java web application for pharmacy operations automation using:

- Core Java
- JSP + Servlets
- JDBC
- MySQL
- HTML + CSS
- Apache Tomcat

## Day 1 Status

Day 1 foundation is complete:

- Project structure scaffolded
- Requirements and scope frozen
- UI wireframe notes drafted
- Base web configuration added

## Day 2 Status

Day 2 database layer is complete:

- Structured MySQL schema created with constraints and indexes
- Seed data created for users, suppliers, and medicines
- ER diagram documented
- JDBC model and DAO mappings added for medicines and suppliers

## Day 3 Status

Day 3 authentication and management UI is complete:

- Login and logout with session-based access control
- Password validation using SHA-256 hash check
- Dashboard metrics wired to database
- Medicine CRUD flow (Servlet + DAO + JSP)
- Supplier CRUD flow (Servlet + DAO + JSP)
- Medical-themed responsive UI for login and management screens

## Day 4 Status

Day 4 inventory and alert workflows are complete:

- Batch-wise inventory stock entry with expiry and pricing
- Manual stock in/out adjustment with transaction logging
- Stock transaction history view in UI
- Expiry alerts page with configurable upcoming window (30/60/90/120 days)
- Navigation updated across dashboard and management screens
- Admin-only pharmacist user creation and pharmacist user listing

## Day 5 Status

Day 5 billing and invoice workflows are complete:

- Cart-based billing screen with medicine selection and quantity controls
- FIFO stock deduction using earliest non-expired batches first
- Automatic write to `sales`, `sale_items`, and `stock_transactions` on checkout
- Validation for insufficient/non-expired stock before bill completion
- Printable invoice page with totals, payment mode, and itemized batch lines
- Billing menu integrated across all primary modules

## Day 6 Status

Day 6 reporting workflows are complete:

- Reports dashboard with date-range and payment-mode sales filters
- Sales summary KPIs (total bills, total items, total revenue)
- Low-stock report with reorder shortage visibility
- Expiry risk report combining near-expiry and expired batches
- Invoice deep-link from sales report rows
- Reports navigation integrated across all major modules

## Project Structure

```text
src/com/pharmacyinventory/
  controller/
  dao/
  model/
  service/
  util/
WebContent/
  assets/css/
  views/
  WEB-INF/web.xml
docs/
  Day1-Requirements.md
  Day1-Wireframes.md
  Day2-Database.md
  Day2-ERD.md
database/
  day2_schema.sql
  day2_seed.sql
  day2_drop_cashier_role.sql
```

## Next

Day 7: Final validation pass, deployment checklist, and demo/package readiness.
