package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.MedicineDao;
import com.pharmacyinventory.dao.SupplierDao;
import com.pharmacyinventory.util.AuthUtil;
import com.pharmacyinventory.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MedicineDao medicineDao = new MedicineDao();
    private final SupplierDao supplierDao = new SupplierDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        try {
            request.setAttribute("medicineCount", medicineDao.countAll());
            request.setAttribute("supplierCount", supplierDao.countAll());
            request.setAttribute("lowStockCount", medicineDao.countLowStock());
            request.setAttribute("nearExpiryCount", medicineDao.countNearExpiry(60));
            request.setAttribute("todaySales", getTodaySalesAmount());
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load dashboard metrics right now.");
        }

        request.getRequestDispatcher("/views/dashboard.jsp").forward(request, response);
    }

    private double getTodaySalesAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        }
        return 0;
    }
}
