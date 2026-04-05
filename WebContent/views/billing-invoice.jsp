<%@ page import="com.pharmacyinventory.model.SaleInvoiceView" %>
<%@ page import="com.pharmacyinventory.model.SaleInvoiceItemView" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));
    SaleInvoiceView invoice = (SaleInvoiceView) request.getAttribute("invoice");
    if (invoice == null) {
        response.sendRedirect(request.getContextPath() + "/billing?error=Invoice%20not%20available.");
        return;
    }
    BigDecimal subTotal = invoice.getSubTotal() == null ? BigDecimal.ZERO : invoice.getSubTotal();
    BigDecimal discount = invoice.getDiscountAmount() == null ? BigDecimal.ZERO : invoice.getDiscountAmount();
    BigDecimal tax = invoice.getTaxAmount() == null ? BigDecimal.ZERO : invoice.getTaxAmount();
    BigDecimal total = invoice.getTotalAmount() == null ? BigDecimal.ZERO : invoice.getTotalAmount();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice <%= invoice.getBillNo() %></title>
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
            <a class="active" href="<%= request.getContextPath() %>/billing">Billing</a>
            <a href="<%= request.getContextPath() %>/reports">Reports</a>
            <% if (isAdmin) { %>
                <a href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel invoice-panel">
            <div class="invoice-header">
                <div>
                    <h1>Sales Invoice</h1>
                    <p class="muted">Bill No: <strong><%= invoice.getBillNo() %></strong></p>
                </div>
                <div class="invoice-actions">
                    <a class="btn" href="<%= request.getContextPath() %>/billing">Back To Billing</a>
                    <button class="btn ghost" onclick="window.print()">Print Invoice</button>
                </div>
            </div>

            <div class="invoice-meta">
                <div><strong>Customer:</strong> <%= invoice.getCustomerName() == null ? "Walk-in Customer" : invoice.getCustomerName() %></div>
                <div><strong>Date:</strong> <%= invoice.getSaleDate() %></div>
                <div><strong>Payment:</strong> <%= invoice.getPaymentMode() %></div>
                <div><strong>Created By:</strong> <%= invoice.getCreatedByUser() == null ? "-" : invoice.getCreatedByUser() %></div>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Batch</th>
                            <th>Qty</th>
                            <th>Rate</th>
                            <th>Line Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (SaleInvoiceItemView item : invoice.getItems()) { %>
                            <tr>
                                <td><%= item.getMedicineCode() %> - <%= item.getMedicineName() %></td>
                                <td><%= item.getBatchNo() %></td>
                                <td><%= item.getQty() %></td>
                                <td><%= item.getRate() %></td>
                                <td><%= item.getLineTotal() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <div class="invoice-totals">
                <div><span>Subtotal</span><strong>INR <%= String.format("%.2f", subTotal.doubleValue()) %></strong></div>
                <div><span>Discount</span><strong>INR <%= String.format("%.2f", discount.doubleValue()) %></strong></div>
                <div><span>Tax</span><strong>INR <%= String.format("%.2f", tax.doubleValue()) %></strong></div>
                <div class="grand-total"><span>Total</span><strong>INR <%= String.format("%.2f", total.doubleValue()) %></strong></div>
            </div>
        </section>
    </main>
</body>
</html>
