<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.pharmacyinventory.model.SalesReportRow" %>
<%@ page import="com.pharmacyinventory.model.SalesReportSummary" %>
<%@ page import="com.pharmacyinventory.model.LowStockReportRow" %>
<%@ page import="com.pharmacyinventory.model.ExpiryReportRow" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));

    List<SalesReportRow> salesRows = (List<SalesReportRow>) request.getAttribute("salesRows");
    List<LowStockReportRow> lowStockRows = (List<LowStockReportRow>) request.getAttribute("lowStockRows");
    List<ExpiryReportRow> expiryRows = (List<ExpiryReportRow>) request.getAttribute("expiryRows");
    SalesReportSummary summary = (SalesReportSummary) request.getAttribute("salesSummary");

    if (salesRows == null) salesRows = Collections.emptyList();
    if (lowStockRows == null) lowStockRows = Collections.emptyList();
    if (expiryRows == null) expiryRows = Collections.emptyList();
    if (summary == null) {
        summary = new SalesReportSummary();
        summary.setTotalBills(0);
        summary.setTotalItems(0);
        summary.setTotalRevenue(BigDecimal.ZERO);
    }

    String fromDate = String.valueOf(request.getAttribute("fromDate"));
    String toDate = String.valueOf(request.getAttribute("toDate"));
    String paymentMode = String.valueOf(request.getAttribute("paymentMode"));
    Integer expiryDays = (Integer) request.getAttribute("expiryDays");
    if (expiryDays == null) expiryDays = 60;
    Long expiredCount = (Long) request.getAttribute("expiredCount");
    Long nearExpiryCount = (Long) request.getAttribute("nearExpiryCount");
    if (expiredCount == null) expiredCount = 0L;
    if (nearExpiryCount == null) nearExpiryCount = 0L;
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports</title>
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
            <a href="<%= request.getContextPath() %>/expiry-alerts">Expiry Alerts</a>
            <a href="<%= request.getContextPath() %>/billing">Billing</a>
            <a class="active" href="<%= request.getContextPath() %>/reports">Reports</a>
            <% if (isAdmin) { %>
                <a href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel">
            <h1>Operational Reports</h1>
            <p class="muted">Analyze sales trends, identify low stock, and monitor expiry risk in one view.</p>

            <form class="report-filter" action="<%= request.getContextPath() %>/reports" method="get">
                <div>
                    <label for="fromDate">From Date</label>
                    <input id="fromDate" name="fromDate" type="date" value="<%= fromDate %>">
                </div>
                <div>
                    <label for="toDate">To Date</label>
                    <input id="toDate" name="toDate" type="date" value="<%= toDate %>">
                </div>
                <div>
                    <label for="paymentMode">Payment Mode</label>
                    <select id="paymentMode" name="paymentMode">
                        <option value="ALL" <%= "ALL".equalsIgnoreCase(paymentMode) ? "selected" : "" %>>All</option>
                        <option value="CASH" <%= "CASH".equalsIgnoreCase(paymentMode) ? "selected" : "" %>>Cash</option>
                        <option value="CARD" <%= "CARD".equalsIgnoreCase(paymentMode) ? "selected" : "" %>>Card</option>
                        <option value="UPI" <%= "UPI".equalsIgnoreCase(paymentMode) ? "selected" : "" %>>UPI</option>
                        <option value="MIXED" <%= "MIXED".equalsIgnoreCase(paymentMode) ? "selected" : "" %>>Mixed</option>
                    </select>
                </div>
                <div>
                    <label for="expiryDays">Expiry Window</label>
                    <select id="expiryDays" name="expiryDays">
                        <option value="30" <%= expiryDays == 30 ? "selected" : "" %>>30 Days</option>
                        <option value="60" <%= expiryDays == 60 ? "selected" : "" %>>60 Days</option>
                        <option value="90" <%= expiryDays == 90 ? "selected" : "" %>>90 Days</option>
                        <option value="120" <%= expiryDays == 120 ? "selected" : "" %>>120 Days</option>
                    </select>
                </div>
                <div class="filter-action">
                    <button type="submit">Run Reports</button>
                </div>
            </form>
        </section>

        <% if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <section class="kpi-grid">
            <article class="kpi-card">
                <h3>Total Bills</h3>
                <p><%= summary.getTotalBills() %></p>
            </article>
            <article class="kpi-card">
                <h3>Total Items Sold</h3>
                <p><%= summary.getTotalItems() %></p>
            </article>
            <article class="kpi-card">
                <h3>Total Revenue</h3>
                <p>INR <%= String.format("%.2f", summary.getTotalRevenue().doubleValue()) %></p>
            </article>
            <article class="kpi-card warning">
                <h3>Near Expiry Batches</h3>
                <p><%= nearExpiryCount %></p>
            </article>
            <article class="kpi-card danger">
                <h3>Expired Batches</h3>
                <p><%= expiredCount %></p>
            </article>
            <article class="kpi-card danger">
                <h3>Low Stock Medicines</h3>
                <p><%= lowStockRows.size() %></p>
            </article>
        </section>

        <section class="panel">
            <h2>Sales Report</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Bill No</th>
                            <th>Customer</th>
                            <th>Payment</th>
                            <th>Items</th>
                            <th>Total</th>
                            <th>Invoice</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (salesRows.isEmpty()) { %>
                            <tr><td colspan="7" class="empty-cell">No sales found for selected filters.</td></tr>
                        <% } %>
                        <% for (SalesReportRow row : salesRows) { %>
                            <tr>
                                <td><%= row.getSaleDate() %></td>
                                <td><%= row.getBillNo() %></td>
                                <td><%= row.getCustomerName() == null ? "Walk-in Customer" : row.getCustomerName() %></td>
                                <td><%= row.getPaymentMode() %></td>
                                <td><%= row.getItemQty() %></td>
                                <td>INR <%= String.format("%.2f", row.getTotalAmount().doubleValue()) %></td>
                                <td><a class="link-btn" href="<%= request.getContextPath() %>/billing/invoice?saleId=<%= row.getSaleId() %>">View</a></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel">
            <h2>Low Stock Report</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Available Qty</th>
                            <th>Reorder Level</th>
                            <th>Shortage</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (lowStockRows.isEmpty()) { %>
                            <tr><td colspan="4" class="empty-cell">No low-stock medicines.</td></tr>
                        <% } %>
                        <% for (LowStockReportRow row : lowStockRows) { %>
                            <tr class="row-warning">
                                <td><%= row.getMedicineCode() %> - <%= row.getMedicineName() %></td>
                                <td><%= row.getAvailableQty() %></td>
                                <td><%= row.getReorderLevel() %></td>
                                <td><%= row.getShortageQty() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel">
            <h2>Expiry Risk Report</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Status</th>
                            <th>Medicine</th>
                            <th>Batch</th>
                            <th>Expiry Date</th>
                            <th>Qty</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (expiryRows.isEmpty()) { %>
                            <tr><td colspan="5" class="empty-cell">No expiry risk records.</td></tr>
                        <% } %>
                        <% for (ExpiryReportRow row : expiryRows) { %>
                            <tr class="<%= "EXPIRED".equalsIgnoreCase(row.getStatus()) ? "row-danger" : "row-warning" %>">
                                <td><%= row.getStatus() %></td>
                                <td><%= row.getMedicineCode() %> - <%= row.getMedicineName() %></td>
                                <td><%= row.getBatchNo() %></td>
                                <td><%= row.getExpDate() %></td>
                                <td><%= row.getQtyInStock() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
