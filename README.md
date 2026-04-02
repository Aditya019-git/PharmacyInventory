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

Day 4: Batch-wise inventory workflows and stock transaction tracking.
