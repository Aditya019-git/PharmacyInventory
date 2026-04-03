<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <%
        Object medicineCount = request.getAttribute("medicineCount");
        Object supplierCount = request.getAttribute("supplierCount");
        Object lowStockCount = request.getAttribute("lowStockCount");
        Object nearExpiryCount = request.getAttribute("nearExpiryCount");
        Object todaySales = request.getAttribute("todaySales");
        String currentUser = (String) session.getAttribute("loggedInUserName");
        String role = String.valueOf(session.getAttribute("loggedInUserRole"));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (currentUser == null) {
            currentUser = (String) session.getAttribute("loggedInUser");
        }
    %>

    <header class="top-nav">
        <div class="brand">MedFlow Inventory</div>
        <nav>
            <a class="active" href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
            <a href="<%= request.getContextPath() %>/medicines">Medicines</a>
            <a href="<%= request.getContextPath() %>/suppliers">Suppliers</a>
            <a href="<%= request.getContextPath() %>/inventory">Inventory</a>
            <a href="<%= request.getContextPath() %>/expiry-alerts">Expiry Alerts</a>
            <% if (isAdmin) { %>
                <a href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="welcome-card">
            <h1>Clinical Operations Console</h1>
            <p>Welcome, <strong><%= currentUser %></strong>. Monitor essentials and move quickly.</p>
        </section>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <section class="stat-grid">
            <article class="stat-card">
                <h3>Total Medicines</h3>
                <p><%= medicineCount == null ? 0 : medicineCount %></p>
            </article>
            <article class="stat-card">
                <h3>Total Suppliers</h3>
                <p><%= supplierCount == null ? 0 : supplierCount %></p>
            </article>
            <article class="stat-card warning">
                <h3>Low Stock Medicines</h3>
                <p><%= lowStockCount == null ? 0 : lowStockCount %></p>
            </article>
            <article class="stat-card danger">
                <h3>Near Expiry (60 Days)</h3>
                <p><%= nearExpiryCount == null ? 0 : nearExpiryCount %></p>
            </article>
            <article class="stat-card revenue">
                <h3>Today's Sales</h3>
                <p>INR <%= todaySales == null ? "0.00" : String.format("%.2f", ((Number) todaySales).doubleValue()) %></p>
            </article>
        </section>
    </main>
</body>
</html>
