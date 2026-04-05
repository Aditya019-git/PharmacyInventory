package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.ReportDao;
import com.pharmacyinventory.model.ExpiryReportRow;
import com.pharmacyinventory.model.SalesReportRow;
import com.pharmacyinventory.model.SalesReportSummary;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@WebServlet("/reports")
public class ReportsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Set<String> PAYMENT_MODES = Set.of("ALL", "CASH", "CARD", "UPI", "MIXED");

    private final ReportDao reportDao = new ReportDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate defaultFrom = today.minusDays(29);
        LocalDate fromDate = parseDateOrDefault(request.getParameter("fromDate"), defaultFrom);
        LocalDate toDate = parseDateOrDefault(request.getParameter("toDate"), today);
        if (fromDate.isAfter(toDate)) {
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        String paymentMode = normalizePaymentMode(request.getParameter("paymentMode"));
        int expiryDays = parseExpiryDays(request.getParameter("expiryDays"));

        try {
            List<SalesReportRow> salesRows = reportDao.findSalesReportRows(Date.valueOf(fromDate), Date.valueOf(toDate), paymentMode);
            SalesReportSummary summary = reportDao.summarizeSales(salesRows);
            List<ExpiryReportRow> expiryRows = reportDao.findExpiryRows(expiryDays);

            long expiredCount = expiryRows.stream().filter(row -> "EXPIRED".equalsIgnoreCase(row.getStatus())).count();
            long nearCount = expiryRows.size() - expiredCount;

            request.setAttribute("salesRows", salesRows);
            request.setAttribute("salesSummary", summary);
            request.setAttribute("lowStockRows", reportDao.findLowStockRows());
            request.setAttribute("expiryRows", expiryRows);
            request.setAttribute("expiredCount", expiredCount);
            request.setAttribute("nearExpiryCount", nearCount);
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load report data right now.");
        }

        request.setAttribute("fromDate", fromDate.toString());
        request.setAttribute("toDate", toDate.toString());
        request.setAttribute("paymentMode", paymentMode);
        request.setAttribute("expiryDays", expiryDays);
        request.getRequestDispatcher("/views/reports.jsp").forward(request, response);
    }

    private LocalDate parseDateOrDefault(String raw, LocalDate fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private int parseExpiryDays(String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value < 7 || value > 180) {
                return 60;
            }
            return value;
        } catch (Exception e) {
            return 60;
        }
    }

    private String normalizePaymentMode(String raw) {
        String mode = raw == null ? "ALL" : raw.trim().toUpperCase();
        if (!PAYMENT_MODES.contains(mode)) {
            return "ALL";
        }
        return mode;
    }
}
