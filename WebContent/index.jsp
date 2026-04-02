<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MedFlow Inventory</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="medical-bg">
    <main class="page-wrap">
        <section class="panel">
            <h1>MedFlow Inventory</h1>
            <p>Clinical pharmacy operations for medicines, suppliers, and billing readiness.</p>
            <a class="btn" href="<%= request.getContextPath() %>/login">Open Login</a>
        </section>
    </main>
</body>
</html>
