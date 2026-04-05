package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.ExpiryReportRow;
import com.pharmacyinventory.model.LowStockReportRow;
import com.pharmacyinventory.model.SalesReportRow;
import com.pharmacyinventory.model.SalesReportSummary;
import com.pharmacyinventory.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public List<SalesReportRow> findSalesReportRows(Date fromDate, Date toDate, String paymentMode) throws SQLException {
        String sql = "SELECT s.id, s.bill_no, s.sale_date, s.customer_name, s.payment_mode, s.total_amount, "
                + "COALESCE(SUM(si.qty), 0) AS item_qty "
                + "FROM sales s "
                + "LEFT JOIN sale_items si ON si.sale_id = s.id "
                + "WHERE DATE(s.sale_date) BETWEEN ? AND ? "
                + "AND (? = 'ALL' OR s.payment_mode = ?) "
                + "GROUP BY s.id, s.bill_no, s.sale_date, s.customer_name, s.payment_mode, s.total_amount "
                + "ORDER BY s.sale_date DESC";

        List<SalesReportRow> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, fromDate);
            statement.setDate(2, toDate);
            statement.setString(3, paymentMode);
            statement.setString(4, paymentMode);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SalesReportRow row = new SalesReportRow();
                    row.setSaleId(resultSet.getLong("id"));
                    row.setBillNo(resultSet.getString("bill_no"));
                    row.setSaleDate(resultSet.getTimestamp("sale_date"));
                    row.setCustomerName(resultSet.getString("customer_name"));
                    row.setPaymentMode(resultSet.getString("payment_mode"));
                    row.setItemQty(resultSet.getInt("item_qty"));
                    row.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public SalesReportSummary summarizeSales(List<SalesReportRow> salesRows) {
        SalesReportSummary summary = new SalesReportSummary();
        summary.setTotalBills(salesRows.size());

        int totalItems = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (SalesReportRow row : salesRows) {
            totalItems += row.getItemQty();
            if (row.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(row.getTotalAmount());
            }
        }

        summary.setTotalItems(totalItems);
        summary.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    public List<LowStockReportRow> findLowStockRows() throws SQLException {
        String sql = "SELECT m.medicine_code, m.name, m.reorder_level, COALESCE(SUM(b.qty_in_stock), 0) AS available_qty "
                + "FROM medicines m "
                + "LEFT JOIN medicine_batches b ON b.medicine_id = m.id "
                + "GROUP BY m.id, m.medicine_code, m.name, m.reorder_level "
                + "HAVING COALESCE(SUM(b.qty_in_stock), 0) < m.reorder_level "
                + "ORDER BY (m.reorder_level - COALESCE(SUM(b.qty_in_stock), 0)) DESC, m.name ASC";

        List<LowStockReportRow> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                LowStockReportRow row = new LowStockReportRow();
                row.setMedicineCode(resultSet.getString("medicine_code"));
                row.setMedicineName(resultSet.getString("name"));
                row.setReorderLevel(resultSet.getInt("reorder_level"));
                row.setAvailableQty(resultSet.getInt("available_qty"));
                row.setShortageQty(row.getReorderLevel() - row.getAvailableQty());
                rows.add(row);
            }
        }
        return rows;
    }

    public List<ExpiryReportRow> findExpiryRows(int withinDays) throws SQLException {
        String sql = "SELECT m.medicine_code, m.name, b.batch_no, b.exp_date, b.qty_in_stock, "
                + "CASE WHEN b.exp_date < CURDATE() THEN 'EXPIRED' ELSE 'NEAR_EXPIRY' END AS expiry_status "
                + "FROM medicine_batches b "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "WHERE b.qty_in_stock > 0 "
                + "AND (b.exp_date < CURDATE() OR b.exp_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)) "
                + "ORDER BY CASE WHEN b.exp_date < CURDATE() THEN 0 ELSE 1 END, b.exp_date ASC, m.name ASC";

        List<ExpiryReportRow> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, withinDays);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ExpiryReportRow row = new ExpiryReportRow();
                    row.setMedicineCode(resultSet.getString("medicine_code"));
                    row.setMedicineName(resultSet.getString("name"));
                    row.setBatchNo(resultSet.getString("batch_no"));
                    row.setExpDate(resultSet.getDate("exp_date"));
                    row.setQtyInStock(resultSet.getInt("qty_in_stock"));
                    row.setStatus(resultSet.getString("expiry_status"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
