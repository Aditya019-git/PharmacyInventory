<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.pharmacyinventory.model.BillableMedicine" %>
<%@ page import="com.pharmacyinventory.model.BillingCartItem" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));

    List<BillableMedicine> medicines = (List<BillableMedicine>) request.getAttribute("medicines");
    List<BillingCartItem> cartItems = (List<BillingCartItem>) request.getAttribute("cartItems");
    BigDecimal subTotal = (BigDecimal) request.getAttribute("subTotal");
    if (medicines == null) medicines = Collections.emptyList();
    if (cartItems == null) cartItems = Collections.emptyList();
    if (subTotal == null) subTotal = BigDecimal.ZERO;

    String querySuccess = request.getParameter("success");
    String queryError = request.getParameter("error");
    String attrError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Billing</title>
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
        <section class="panel split">
            <div>
                <h1>Create Bill</h1>
                <p class="muted">Add sale items and checkout with FIFO stock deduction.</p>

                <form class="grid-form" action="<%= request.getContextPath() %>/billing" method="post">
                    <label for="medicineId">Medicine</label>
                    <select id="medicineId" name="medicineId" required>
                        <option value="">Select medicine</option>
                        <% for (BillableMedicine med : medicines) { %>
                            <option value="<%= med.getMedicineId() %>">
                                <%= med.getMedicineCode() %> - <%= med.getMedicineName() %>
                                (Stock: <%= med.getAvailableQty() %>, Price: <%= med.getUnitPrice() %>)
                            </option>
                        <% } %>
                    </select>

                    <label for="qty">Quantity</label>
                    <input id="qty" name="qty" type="number" min="1" required>

                    <div class="action-row">
                        <button type="submit" name="action" value="addItem">Add To Bill</button>
                        <button type="submit" name="action" value="clearCart" class="btn ghost">Clear Cart</button>
                    </div>
                </form>
            </div>

            <div>
                <h2>Checkout</h2>
                <p class="muted">Finalize bill and generate invoice.</p>
                <form class="grid-form" action="<%= request.getContextPath() %>/billing" method="post">
                    <input type="hidden" name="action" value="checkout">

                    <label for="customerName">Customer Name</label>
                    <input id="customerName" name="customerName" type="text" placeholder="Walk-in Customer">

                    <label for="paymentMode">Payment Mode</label>
                    <select id="paymentMode" name="paymentMode" required>
                        <option value="CASH">Cash</option>
                        <option value="CARD">Card</option>
                        <option value="UPI">UPI</option>
                        <option value="MIXED">Mixed</option>
                    </select>

                    <label for="discountPercent">Discount %</label>
                    <input id="discountPercent" name="discountPercent" type="number" min="0" max="100" step="0.01" value="0">

                    <label for="taxPercent">Tax %</label>
                    <input id="taxPercent" name="taxPercent" type="number" min="0" max="100" step="0.01" value="0">

                    <div class="summary-strip">
                        <span>Subtotal</span>
                        <strong>INR <%= String.format("%.2f", subTotal.doubleValue()) %></strong>
                    </div>

                    <div class="action-row">
                        <button type="submit">Checkout & Generate Invoice</button>
                    </div>
                </form>
            </div>
        </section>

        <% if (querySuccess != null && !querySuccess.isBlank()) { %>
            <div class="alert success"><%= querySuccess %></div>
        <% } %>
        <% if (queryError != null && !queryError.isBlank()) { %>
            <div class="alert error"><%= queryError %></div>
        <% } %>
        <% if (attrError != null && !attrError.isBlank()) { %>
            <div class="alert error"><%= attrError %></div>
        <% } %>

        <section class="panel">
            <h2>Current Bill Items</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Medicine</th>
                            <th>Qty</th>
                            <th>Rate</th>
                            <th>Line Total</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (cartItems.isEmpty()) { %>
                            <tr><td colspan="5" class="empty-cell">No items added to bill yet.</td></tr>
                        <% } %>
                        <% for (BillingCartItem item : cartItems) { %>
                            <tr>
                                <td><%= item.getMedicineCode() %> - <%= item.getMedicineName() %></td>
                                <td><%= item.getQty() %></td>
                                <td><%= item.getUnitPrice() %></td>
                                <td><%= item.getLineTotal() %></td>
                                <td>
                                    <form action="<%= request.getContextPath() %>/billing" method="post" style="margin:0;">
                                        <input type="hidden" name="action" value="removeItem">
                                        <input type="hidden" name="medicineId" value="<%= item.getMedicineId() %>">
                                        <button type="submit" class="link-btn danger">Remove</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
