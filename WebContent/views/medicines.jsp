<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.pharmacyinventory.model.Medicine" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session == null || session.getAttribute("loggedInUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("loggedInUserRole")));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Medicines</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <%
        Medicine editMedicine = (Medicine) request.getAttribute("editMedicine");
        List<Medicine> medicines = (List<Medicine>) request.getAttribute("medicines");
        if (medicines == null) {
            medicines = Collections.emptyList();
        }
        String success = request.getParameter("success");
        String error = (String) request.getAttribute("error");
    %>

    <header class="top-nav">
        <div class="brand">MedFlow Inventory</div>
        <nav>
            <a href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
            <a class="active" href="<%= request.getContextPath() %>/medicines">Medicines</a>
            <a href="<%= request.getContextPath() %>/suppliers">Suppliers</a>
            <a href="<%= request.getContextPath() %>/inventory">Inventory</a>
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
                <h1><%= editMedicine == null ? "Add New Medicine" : "Edit Medicine" %></h1>
                <p class="muted">Maintain medicine master data for stock and billing workflows.</p>
            </div>

            <form class="grid-form" action="<%= request.getContextPath() %>/medicines" method="post">
                <input type="hidden" name="id" value="<%= editMedicine == null ? "" : editMedicine.getId() %>">

                <label for="medicineCode">Medicine Code</label>
                <input id="medicineCode" name="medicineCode" type="text" required
                       value="<%= editMedicine == null ? "" : editMedicine.getMedicineCode() %>"
                       <%= editMedicine == null ? "" : "readonly" %>>

                <label for="name">Name</label>
                <input id="name" name="name" type="text" required
                       value="<%= editMedicine == null ? "" : editMedicine.getName() %>">

                <label for="category">Category</label>
                <input id="category" name="category" type="text"
                       value="<%= editMedicine == null ? "" : editMedicine.getCategory() %>">

                <label for="brand">Brand</label>
                <input id="brand" name="brand" type="text"
                       value="<%= editMedicine == null ? "" : editMedicine.getBrand() %>">

                <label for="unit">Unit</label>
                <input id="unit" name="unit" type="text" required
                       value="<%= editMedicine == null ? "" : editMedicine.getUnit() %>">

                <label for="reorderLevel">Reorder Level</label>
                <input id="reorderLevel" name="reorderLevel" type="number" min="0" required
                       value="<%= editMedicine == null ? 10 : editMedicine.getReorderLevel() %>">

                <div class="action-row">
                    <button type="submit"><%= editMedicine == null ? "Add Medicine" : "Update Medicine" %></button>
                    <% if (editMedicine != null) { %>
                        <a class="btn ghost" href="<%= request.getContextPath() %>/medicines">Cancel Edit</a>
                    <% } %>
                </div>
            </form>
        </section>

        <% if (success != null && !success.isBlank()) { %>
            <div class="alert success"><%= success %></div>
        <% } %>
        <% if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <section class="panel">
            <h2>Medicine List</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Category</th>
                            <th>Brand</th>
                            <th>Unit</th>
                            <th>Reorder</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (medicines.isEmpty()) { %>
                            <tr>
                                <td colspan="7" class="empty-cell">No medicine records yet.</td>
                            </tr>
                        <% } %>
                        <% for (Medicine medicine : medicines) { %>
                            <tr>
                                <td><%= medicine.getMedicineCode() %></td>
                                <td><%= medicine.getName() %></td>
                                <td><%= medicine.getCategory() == null ? "-" : medicine.getCategory() %></td>
                                <td><%= medicine.getBrand() == null ? "-" : medicine.getBrand() %></td>
                                <td><%= medicine.getUnit() %></td>
                                <td><%= medicine.getReorderLevel() %></td>
                                <td class="actions">
                                    <a class="link-btn" href="<%= request.getContextPath() %>/medicines?action=edit&id=<%= medicine.getId() %>">Edit</a>
                                    <a class="link-btn danger" href="<%= request.getContextPath() %>/medicines?action=delete&id=<%= medicine.getId() %>"
                                       onclick="return confirm('Delete this medicine?');">Delete</a>
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
