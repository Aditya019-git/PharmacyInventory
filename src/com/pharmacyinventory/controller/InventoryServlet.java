package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.InventoryDao;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final InventoryDao inventoryDao = new InventoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        try {
            request.setAttribute("medicines", inventoryDao.findMedicineOptions());
            request.setAttribute("suppliers", inventoryDao.findSupplierOptions());
            request.setAttribute("batches", inventoryDao.findAllBatches());
            request.setAttribute("transactions", inventoryDao.findRecentTransactions(20));
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load inventory data right now.");
        }
        request.getRequestDispatcher("/views/inventory.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        long userId = getCurrentUserId(request.getSession(false));
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            if ("addBatch".equalsIgnoreCase(action)) {
                handleAddBatch(request, userId);
                response.sendRedirect(request.getContextPath() + "/inventory?success=" + encode("Batch added successfully."));
                return;
            }

            if ("adjustStock".equalsIgnoreCase(action)) {
                boolean adjusted = handleAdjustStock(request, userId);
                if (!adjusted) {
                    response.sendRedirect(request.getContextPath() + "/inventory?error="
                            + encode("Stock adjustment failed. Check batch and available quantity."));
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/inventory?success="
                        + encode("Stock adjusted and transaction logged."));
                return;
            }

            response.sendRedirect(request.getContextPath() + "/inventory?error=" + encode("Unknown inventory action."));
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/inventory?error=" + encode(e.getMessage()));
        } catch (SQLException e) {
            response.sendRedirect(request.getContextPath() + "/inventory?error="
                    + encode("Unable to complete inventory operation."));
        }
    }

    private void handleAddBatch(HttpServletRequest request, long userId) throws SQLException {
        long medicineId = parseLongRequired(request.getParameter("medicineId"), "Medicine is required.");
        String supplierRaw = trim(request.getParameter("supplierId"));
        Long supplierId = supplierRaw.isEmpty() ? null : parseLongRequired(supplierRaw, "Invalid supplier.");
        String batchNo = trim(request.getParameter("batchNo"));
        String mfgDateRaw = trim(request.getParameter("mfgDate"));
        String expDateRaw = trim(request.getParameter("expDate"));
        int qty = parseIntRequired(request.getParameter("qty"), "Quantity must be a valid positive number.");
        BigDecimal costPrice = parseDecimalRequired(request.getParameter("costPrice"), "Invalid cost price.");
        BigDecimal sellPrice = parseDecimalRequired(request.getParameter("sellPrice"), "Invalid sell price.");

        if (batchNo.isEmpty()) {
            throw new IllegalArgumentException("Batch number is required.");
        }
        if (expDateRaw.isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required.");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (costPrice.compareTo(BigDecimal.ZERO) < 0 || sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Prices cannot be negative.");
        }

        Date mfgDate = mfgDateRaw.isEmpty() ? null : Date.valueOf(mfgDateRaw);
        Date expDate = Date.valueOf(expDateRaw);
        if (mfgDate != null && expDate.before(mfgDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before manufacturing date.");
        }

        inventoryDao.addBatchWithOpeningStock(medicineId, supplierId, batchNo, mfgDate, expDate, qty, costPrice, sellPrice, userId);
    }

    private boolean handleAdjustStock(HttpServletRequest request, long userId) throws SQLException {
        long batchId = parseLongRequired(request.getParameter("batchId"), "Batch is required.");
        String direction = trim(request.getParameter("direction"));
        int qty = parseIntRequired(request.getParameter("adjustQty"), "Adjustment quantity is required.");
        String note = trim(request.getParameter("note"));

        if (!"IN".equalsIgnoreCase(direction) && !"OUT".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException("Select a valid adjustment direction.");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Adjustment quantity must be greater than zero.");
        }
        if (note.isEmpty()) {
            note = "Manual stock adjustment";
        }
        return inventoryDao.adjustStock(batchId, direction, qty, note, userId);
    }

    private long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return -1;
        }
        Object userId = session.getAttribute("loggedInUserId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private long parseLongRequired(String value, String message) {
        try {
            long result = Long.parseLong(trim(value));
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parseIntRequired(String value, String message) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal parseDecimalRequired(String value, String message) {
        try {
            return new BigDecimal(trim(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
