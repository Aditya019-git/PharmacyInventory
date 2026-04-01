# Day 2 Database Setup

## Files

- `database/day2_schema.sql`: Full schema (tables, constraints, indexes)
- `database/day2_seed.sql`: Starter data for users, suppliers, medicines
- `database/day2_drop_cashier_role.sql`: One-time migration to remove old `CASHIER` role from existing DB

## Run Locally (PowerShell)

```powershell
cd "C:\Users\Aditya\Desktop\Projects\PharmacyInventory"
cmd /c "mysql -u root -proot < database\day2_schema.sql"
cmd /c "mysql -u root -proot < database\day2_seed.sql"
```

## Verify

```powershell
mysql -u root -proot -D pharmacy_inventory -e "SHOW TABLES;"
mysql -u root -proot -D pharmacy_inventory -e "SELECT id, username, role FROM users;"
```

## If Your DB Already Has CASHIER Role

Run this one-time migration:

```powershell
cmd /c "mysql -u root -proot -D pharmacy_inventory < database\day2_drop_cashier_role.sql"
```

## Notes

- Passwords in seed are stored using `SHA2(..., 256)` in SQL.
- `medicine_batches` keeps stock batch-wise for FIFO and expiry workflows.
- `stock_transactions` stores all inventory movement for auditability.
