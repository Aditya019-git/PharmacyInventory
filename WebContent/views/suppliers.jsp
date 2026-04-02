<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.pharmacyinventory.model.Supplier" %>
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
    <title>Suppliers</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <%
        Supplier editSupplier = (Supplier) request.getAttribute("editSupplier");
        List<Supplier> suppliers = (List<Supplier>) request.getAttribute("suppliers");
        if (suppliers == null) {
            suppliers = Collections.emptyList();
        }
        String success = request.getParameter("success");
        String error = (String) request.getAttribute("error");
    %>

    <header class="top-nav">
        <div class="brand">MedFlow Inventory</div>
        <nav>
            <a href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
            <a href="<%= request.getContextPath() %>/medicines">Medicines</a>
            <a class="active" href="<%= request.getContextPath() %>/suppliers">Suppliers</a>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </nav>
    </header>

    <main class="page-wrap">
        <section class="panel split">
            <div>
                <h1><%= editSupplier == null ? "Add New Supplier" : "Edit Supplier" %></h1>
                <p class="muted">Maintain vendor details for purchase and stock inward workflows.</p>
            </div>

            <form class="grid-form" action="<%= request.getContextPath() %>/suppliers" method="post">
                <input type="hidden" name="id" value="<%= editSupplier == null ? "" : editSupplier.getId() %>">

                <label for="supplierCode">Supplier Code</label>
                <input id="supplierCode" name="supplierCode" type="text" required
                       value="<%= editSupplier == null ? "" : editSupplier.getSupplierCode() %>"
                       <%= editSupplier == null ? "" : "readonly" %>>

                <label for="name">Supplier Name</label>
                <input id="name" name="name" type="text" required
                       value="<%= editSupplier == null ? "" : editSupplier.getName() %>">

                <label for="phone">Phone</label>
                <input id="phone" name="phone" type="text"
                       value="<%= editSupplier == null ? "" : editSupplier.getPhone() %>">

                <label for="email">Email</label>
                <input id="email" name="email" type="email"
                       value="<%= editSupplier == null ? "" : editSupplier.getEmail() %>">

                <label for="gstNo">GST No</label>
                <input id="gstNo" name="gstNo" type="text"
                       value="<%= editSupplier == null ? "" : editSupplier.getGstNo() %>">

                <label for="addressLine">Address</label>
                <input id="addressLine" name="addressLine" type="text"
                       value="<%= editSupplier == null ? "" : editSupplier.getAddressLine() %>">

                <div class="action-row">
                    <button type="submit"><%= editSupplier == null ? "Add Supplier" : "Update Supplier" %></button>
                    <% if (editSupplier != null) { %>
                        <a class="btn ghost" href="<%= request.getContextPath() %>/suppliers">Cancel Edit</a>
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
            <h2>Supplier List</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Phone</th>
                            <th>Email</th>
                            <th>GST</th>
                            <th>Address</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (suppliers.isEmpty()) { %>
                            <tr>
                                <td colspan="7" class="empty-cell">No supplier records yet.</td>
                            </tr>
                        <% } %>
                        <% for (Supplier supplier : suppliers) { %>
                            <tr>
                                <td><%= supplier.getSupplierCode() %></td>
                                <td><%= supplier.getName() %></td>
                                <td><%= supplier.getPhone() == null ? "-" : supplier.getPhone() %></td>
                                <td><%= supplier.getEmail() == null ? "-" : supplier.getEmail() %></td>
                                <td><%= supplier.getGstNo() == null ? "-" : supplier.getGstNo() %></td>
                                <td><%= supplier.getAddressLine() == null ? "-" : supplier.getAddressLine() %></td>
                                <td class="actions">
                                    <a class="link-btn" href="<%= request.getContextPath() %>/suppliers?action=edit&id=<%= supplier.getId() %>">Edit</a>
                                    <a class="link-btn danger" href="<%= request.getContextPath() %>/suppliers?action=delete&id=<%= supplier.getId() %>"
                                       onclick="return confirm('Delete this supplier?');">Delete</a>
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
