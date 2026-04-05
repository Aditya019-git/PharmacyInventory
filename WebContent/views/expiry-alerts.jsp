<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.pharmacyinventory.model.InventoryBatchView" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));

    List<InventoryBatchView> expiringBatches = (List<InventoryBatchView>) request.getAttribute("expiringBatches");
    List<InventoryBatchView> expiredBatches = (List<InventoryBatchView>) request.getAttribute("expiredBatches");
    if (expiringBatches == null) expiringBatches = Collections.emptyList();
    if (expiredBatches == null) expiredBatches = Collections.emptyList();

    Integer days = (Integer) request.getAttribute("days");
    if (days == null) days = 60;
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Expiry Alerts</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <header class="top-nav">
        <div class="brand">MedFlow Inventory</div>
        <nav>
            <a href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
            <a href="<%= request.getContextPath() %>/medicines">Medicines</a>
            <a href="<%= request.getContextPath() %>/suppliers">Suppliers</a>
            <a href="<%= request.getContextPath() %>/inventory">Inventory</a>
            <a class="active" href="<%= request.getContextPath() %>/expiry-alerts">Expiry Alerts</a>
            <a href="<%= request.getContextPath() %>/billing">Billing</a>
            <a href="<%= request.getContextPath() %>/reports">Reports</a>
            <% if (isAdmin) { %>
                <a href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel split">
            <div>
                <h1>Expiry Alert Center</h1>
                <p class="muted">Monitor near-expiry and expired batches before they affect billing and compliance.</p>
            </div>
            <form class="inline-form" method="get" action="<%= request.getContextPath() %>/expiry-alerts">
                <label for="days">Show batches expiring within</label>
                <select id="days" name="days">
                    <option value="30" <%= days == 30 ? "selected" : "" %>>30 days</option>
                    <option value="60" <%= days == 60 ? "selected" : "" %>>60 days</option>
                    <option value="90" <%= days == 90 ? "selected" : "" %>>90 days</option>
                    <option value="120" <%= days == 120 ? "selected" : "" %>>120 days</option>
                </select>
                <button type="submit">Refresh Alerts</button>
            </form>
        </section>

        <% if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <section class="panel">
            <h2>Near Expiry Batches (Next <%= days %> Days)</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Batch</th>
                            <th>Expiry</th>
                            <th>Qty</th>
                            <th>Supplier</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (expiringBatches.isEmpty()) { %>
                            <tr><td colspan="5" class="empty-cell">No near-expiry batches in selected range.</td></tr>
                        <% } %>
                        <% for (InventoryBatchView row : expiringBatches) { %>
                            <tr class="row-warning">
                                <td><%= row.getMedicineCode() %> - <%= row.getMedicineName() %></td>
                                <td><%= row.getBatchNo() %></td>
                                <td><%= row.getExpDate() %></td>
                                <td><%= row.getQtyInStock() %></td>
                                <td><%= row.getSupplierName() == null ? "-" : row.getSupplierName() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel">
            <h2>Already Expired Batches</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Batch</th>
                            <th>Expiry</th>
                            <th>Qty</th>
                            <th>Supplier</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (expiredBatches.isEmpty()) { %>
                            <tr><td colspan="5" class="empty-cell">No expired batches found.</td></tr>
                        <% } %>
                        <% for (InventoryBatchView row : expiredBatches) { %>
                            <tr class="row-danger">
                                <td><%= row.getMedicineCode() %> - <%= row.getMedicineName() %></td>
                                <td><%= row.getBatchNo() %></td>
                                <td><%= row.getExpDate() %></td>
                                <td><%= row.getQtyInStock() %></td>
                                <td><%= row.getSupplierName() == null ? "-" : row.getSupplierName() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
