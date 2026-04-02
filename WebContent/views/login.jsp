<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session != null && session.getAttribute("loggedInUser") != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pharmacy Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <main class="auth-layout">
        <section class="auth-hero">
            <span class="pill">Pharmacy Automation</span>
            <h1>MedFlow Inventory</h1>
            <p>Track stock, manage suppliers, and run billing from one clinical dashboard.</p>
            <div class="pulse-grid"></div>
        </section>

        <section class="auth-card">
            <h2>Secure Login</h2>
            <p class="muted">Use your Admin or Pharmacist account.</p>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <div class="alert error"><%= error %></div>
            <% } %>

            <form action="<%= request.getContextPath() %>/login" method="post">
                <label for="username">Username</label>
                <input id="username" type="text" name="username" required>

                <label for="password">Password</label>
                <input id="password" type="password" name="password" required>

                <button type="submit">Login</button>
            </form>

            <div class="hint">Demo: admin / admin@123</div>
        </section>
    </main>
</body>
</html>
