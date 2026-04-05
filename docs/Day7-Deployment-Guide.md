# Day 7 Deployment Guide

## 1. Prerequisites

- Java 17
- MySQL 8.x running on `localhost:3306`
- Apache Tomcat 10/11
- MySQL JDBC jar in `WebContent/WEB-INF/lib`

## 2. Database Setup

Run from project root:

```powershell
cmd /c "mysql -u root -proot < database\day2_schema.sql"
cmd /c "mysql -u root -proot < database\day2_seed.sql"
```

If migrating from old role setup:

```powershell
cmd /c "mysql -u root -proot -D pharmacy_inventory < database\day2_drop_cashier_role.sql"
```

## 3. Eclipse Project Configuration

- Java Build Path uses `JavaSE-17`.
- Project Facets:
  - Java: 17
  - Dynamic Web Module: 5.0 or 6.0
- Deployment Assembly:
  - `/src -> /WEB-INF/classes`
  - `/WebContent -> /`
- Targeted runtime: your active Tomcat server.

## 4. Server Run

- Clean project (`Project -> Clean...`).
- In Servers view: Stop -> Clean -> Publish -> Start.
- App URL:
  - `http://localhost:8080/PharmacyInventory/login`
  - Replace port if your Tomcat uses a different one.

## 5. Default Credentials

- Admin: `admin / admin@123`
- Pharmacist: `pharmacist / pharma@123`

## 6. Post-Deploy Smoke Tests

- Login and dashboard loads.
- Medicines CRUD, Suppliers CRUD.
- Inventory add batch and stock adjustments.
- Expiry alerts page opens.
- Billing checkout creates invoice.
- Reports page loads with filters.
- Admin can create pharmacist users.
