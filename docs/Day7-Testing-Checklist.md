# Day 7 Testing Checklist

## 1. Authentication & Session

- [ ] Admin login works with valid credentials.
- [ ] Pharmacist login works with valid credentials.
- [ ] Invalid credentials show error and deny access.
- [ ] Logout invalidates session and redirects to login.
- [ ] Direct URL access to protected pages redirects to login.

## 2. Role Access

- [ ] Admin can open `/pharmacists`.
- [ ] Pharmacist cannot open `/pharmacists` (redirects to dashboard).
- [ ] Admin can create pharmacist users.
- [ ] New pharmacist can login after creation.

## 3. Master Data CRUD

- [ ] Medicine add/edit/delete works.
- [ ] Supplier add/edit/delete works.
- [ ] Duplicate medicine code and supplier code are blocked.

## 4. Inventory & Stock

- [ ] Add batch creates stock and opening transaction.
- [ ] Stock adjustment IN increases quantity.
- [ ] Stock adjustment OUT decreases quantity.
- [ ] OUT adjustment beyond available stock is blocked.

## 5. Expiry

- [ ] Near-expiry list updates for 30/60/90/120 day filters.
- [ ] Expired batches appear in expired section.

## 6. Billing (FIFO)

- [ ] Add items to cart works.
- [ ] Checkout creates sale and invoice.
- [ ] FIFO batch deduction uses oldest non-expired batch first.
- [ ] Insufficient stock blocks checkout with proper message.
- [ ] Invoice page loads and print button works.

## 7. Reports

- [ ] Sales report filters by date range.
- [ ] Sales report filters by payment mode.
- [ ] Revenue, bills, and item totals are accurate.
- [ ] Low-stock report shows reorder shortage.
- [ ] Expiry risk report shows near-expiry and expired batches.
- [ ] Invoice link from sales report opens correct invoice.

## 8. Audit Validation

- [ ] `LOGIN` entries appear in `audit_logs` after login.
- [ ] `LOGOUT` entries appear in `audit_logs` after logout.
- [ ] `INSERT` audit entry appears when admin creates pharmacist.
