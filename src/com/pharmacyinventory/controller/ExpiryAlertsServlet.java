package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.InventoryDao;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/expiry-alerts")
public class ExpiryAlertsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final InventoryDao inventoryDao = new InventoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        int days = 60;
        String daysRaw = request.getParameter("days");
        if (daysRaw != null && !daysRaw.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(daysRaw.trim());
                if (parsed >= 7 && parsed <= 180) {
                    days = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            request.setAttribute("days", days);
            request.setAttribute("expiringBatches", inventoryDao.findExpiringWithinDays(days));
            request.setAttribute("expiredBatches", inventoryDao.findExpiredBatches());
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load expiry alerts right now.");
        }

        request.getRequestDispatcher("/views/expiry-alerts.jsp").forward(request, response);
    }
}
