<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.pharmacyinventory.model.User" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String role = String.valueOf(session.getAttribute("loggedInUserRole"));
    if (!"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    List<User> pharmacists = (List<User>) request.getAttribute("pharmacists");
    if (pharmacists == null) pharmacists = Collections.emptyList();
    String querySuccess = request.getParameter("success");
    String queryError = request.getParameter("error");
    String attrError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pharmacist Management</title>
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
            <a class="active" href="<%= request.getContextPath() %>/pharmacists">Pharmacists</a>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel split">
            <div>
                <h1>Create Pharmacist Account</h1>
                <p class="muted">Admin can onboard new pharmacist users for system access.</p>

                <form class="grid-form" action="<%= request.getContextPath() %>/pharmacists" method="post">
                    <label for="fullName">Full Name</label>
                    <input id="fullName" name="fullName" type="text" required>

                    <label for="username">Username</label>
                    <input id="username" name="username" type="text" required>

                    <label for="password">Password</label>
                    <input id="password" name="password" type="password" minlength="6" required>

                    <div class="action-row">
                        <button type="submit">Create Pharmacist</button>
                    </div>
                </form>
            </div>
            <div>
                <h2>Access Rules</h2>
                <p class="muted">New accounts are created with role <strong>PHARMACIST</strong> and active status.</p>
                <ul class="simple-list">
                    <li>Pharmacist can access medicines, suppliers, inventory, and expiry modules.</li>
                    <li>Only admin can create pharmacist accounts.</li>
                    <li>Password is securely stored as SHA-256 hash.</li>
                </ul>
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
            <h2>Pharmacist Users</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Username</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Created At</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (pharmacists.isEmpty()) { %>
                            <tr><td colspan="5" class="empty-cell">No pharmacist users found.</td></tr>
                        <% } %>
                        <% for (User pharmacist : pharmacists) { %>
                            <tr>
                                <td><%= pharmacist.getFullName() %></td>
                                <td><%= pharmacist.getUsername() %></td>
                                <td><%= pharmacist.getRole() %></td>
                                <td><%= pharmacist.isActive() ? "Active" : "Inactive" %></td>
                                <td><%= pharmacist.getCreatedAt() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
