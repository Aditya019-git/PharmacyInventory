<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.pharmacyinventory.model.Medicine" %>
<%@ page import="com.pharmacyinventory.model.Supplier" %>
<%@ page import="com.pharmacyinventory.model.InventoryBatchView" %>
<%@ page import="com.pharmacyinventory.model.StockTransactionView" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));

    List<Medicine> medicines = (List<Medicine>) request.getAttribute("medicines");
    List<Supplier> suppliers = (List<Supplier>) request.getAttribute("suppliers");
    List<InventoryBatchView> batches = (List<InventoryBatchView>) request.getAttribute("batches");
    List<StockTransactionView> transactions = (List<StockTransactionView>) request.getAttribute("transactions");

    if (medicines == null) medicines = Collections.emptyList();
    if (suppliers == null) suppliers = Collections.emptyList();
    if (batches == null) batches = Collections.emptyList();
    if (transactions == null) transactions = Collections.emptyList();

    String success = request.getParameter("success");
    String queryError = request.getParameter("error");
    String attrError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventory</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <header class="top-nav">
        <div class="brand">MedFlow Inventory</div>
        <nav>
            <a href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
            <a href="<%= request.getContextPath() %>/medicines">Medicines</a>
            <a href="<%= request.getContextPath() %>/suppliers">Suppliers</a>
            <a class="active" href="<%= request.getContextPath() %>/inventory">Inventory</a>
            <a href="<%= request.getContextPath() %>/expiry-alerts">Expiry Alerts</a>
            <a href="<%= request.getContextPath() %>/billing">Billing</a>
            <% if (isAdmin) { %>
                <a href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel split">
            <div>
                <h1>Add Batch Stock</h1>
                <p class="muted">Create medicine batches with expiry and opening stock in one step.</p>

                <form class="grid-form" action="<%= request.getContextPath() %>/inventory" method="post">
                    <input type="hidden" name="action" value="addBatch">

                    <label for="medicineId">Medicine</label>
                    <select id="medicineId" name="medicineId" required>
                        <option value="">Select medicine</option>
                        <% for (Medicine med : medicines) { %>
                            <option value="<%= med.getId() %>"><%= med.getMedicineCode() %> - <%= med.getName() %></option>
                        <% } %>
                    </select>

                    <label for="supplierId">Supplier (Optional)</label>
                    <select id="supplierId" name="supplierId">
                        <option value="">Select supplier</option>
                        <% for (Supplier supplier : suppliers) { %>
                            <option value="<%= supplier.getId() %>"><%= supplier.getSupplierCode() %> - <%= supplier.getName() %></option>
                        <% } %>
                    </select>

                    <label for="batchNo">Batch No</label>
                    <input id="batchNo" name="batchNo" type="text" required>

                    <label for="mfgDate">Mfg Date</label>
                    <input id="mfgDate" name="mfgDate" type="date">

                    <label for="expDate">Expiry Date</label>
                    <input id="expDate" name="expDate" type="date" required>

                    <label for="qty">Opening Qty</label>
                    <input id="qty" name="qty" type="number" min="1" required>

                    <label for="costPrice">Cost Price</label>
                    <input id="costPrice" name="costPrice" type="number" min="0" step="0.01" required>

                    <label for="sellPrice">Sell Price</label>
                    <input id="sellPrice" name="sellPrice" type="number" min="0" step="0.01" required>

                    <div class="action-row">
                        <button type="submit">Add Batch</button>
                    </div>
                </form>
            </div>

            <div>
                <h2>Stock Adjustment</h2>
                <p class="muted">Adjust damaged/returned/manual stock and keep audit trail.</p>

                <form class="grid-form" action="<%= request.getContextPath() %>/inventory" method="post">
                    <input type="hidden" name="action" value="adjustStock">

                    <label for="batchId">Batch</label>
                    <select id="batchId" name="batchId" required>
                        <option value="">Select batch</option>
                        <% for (InventoryBatchView batch : batches) { %>
                            <option value="<%= batch.getBatchId() %>">
                                <%= batch.getMedicineCode() %> / <%= batch.getBatchNo() %> (Qty: <%= batch.getQtyInStock() %>)
                            </option>
                        <% } %>
                    </select>

                    <label for="direction">Direction</label>
                    <select id="direction" name="direction" required>
                        <option value="IN">Stock In</option>
                        <option value="OUT">Stock Out</option>
                    </select>

                    <label for="adjustQty">Quantity</label>
                    <input id="adjustQty" name="adjustQty" type="number" min="1" required>

                    <label for="note">Note</label>
                    <input id="note" name="note" type="text" placeholder="Reason for adjustment">

                    <div class="action-row">
                        <button type="submit">Apply Adjustment</button>
                    </div>
                </form>
            </div>
        </section>

        <% if (success != null && !success.isBlank()) { %>
            <div class="alert success"><%= success %></div>
        <% } %>
        <% if (queryError != null && !queryError.isBlank()) { %>
            <div class="alert error"><%= queryError %></div>
        <% } %>
        <% if (attrError != null && !attrError.isBlank()) { %>
            <div class="alert error"><%= attrError %></div>
        <% } %>

        <section class="panel">
            <h2>Batch Inventory</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Batch No</th>
                            <th>Supplier</th>
                            <th>Mfg Date</th>
                            <th>Exp Date</th>
                            <th>Qty</th>
                            <th>Cost</th>
                            <th>Sell</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (batches.isEmpty()) { %>
                            <tr><td colspan="8" class="empty-cell">No batch stock records yet.</td></tr>
                        <% } %>
                        <% for (InventoryBatchView batch : batches) { %>
                            <tr class="<%= batch.isExpired() ? "row-danger" : "" %>">
                                <td><%= batch.getMedicineCode() %> - <%= batch.getMedicineName() %></td>
                                <td><%= batch.getBatchNo() %></td>
                                <td><%= batch.getSupplierName() == null ? "-" : batch.getSupplierName() %></td>
                                <td><%= batch.getMfgDate() == null ? "-" : batch.getMfgDate() %></td>
                                <td><%= batch.getExpDate() %></td>
                                <td><%= batch.getQtyInStock() %></td>
                                <td><%= batch.getCostPrice() %></td>
                                <td><%= batch.getSellPrice() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel">
            <h2>Recent Stock Transactions</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>When</th>
                            <th>Medicine</th>
                            <th>Batch</th>
                            <th>Type</th>
                            <th>Qty</th>
                            <th>Ref</th>
                            <th>By</th>
                            <th>Note</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (transactions.isEmpty()) { %>
                            <tr><td colspan="8" class="empty-cell">No stock transactions yet.</td></tr>
                        <% } %>
                        <% for (StockTransactionView txn : transactions) { %>
                            <tr>
                                <td><%= txn.getTxnDate() %></td>
                                <td><%= txn.getMedicineCode() %> - <%= txn.getMedicineName() %></td>
                                <td><%= txn.getBatchNo() %></td>
                                <td><%= txn.getTxnType() %></td>
                                <td><%= txn.getQty() %></td>
                                <td><%= txn.getRefType() %></td>
                                <td><%= txn.getUserName() == null ? "-" : txn.getUserName() %></td>
                                <td><%= txn.getNote() == null ? "-" : txn.getNote() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
