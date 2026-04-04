package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.BillableMedicine;
import com.pharmacyinventory.model.BillingCartItem;
import com.pharmacyinventory.model.SaleInvoiceItemView;
import com.pharmacyinventory.model.SaleInvoiceView;
import com.pharmacyinventory.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillingDao {
    private static final DateTimeFormatter BILL_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public List<BillableMedicine> findBillableMedicines() throws SQLException {
        String sql = "SELECT m.id, m.medicine_code, m.name, "
                + "COALESCE(SUM(CASE WHEN b.exp_date >= CURDATE() THEN b.qty_in_stock ELSE 0 END), 0) AS available_qty, "
                + "MIN(CASE WHEN b.exp_date >= CURDATE() AND b.qty_in_stock > 0 THEN b.sell_price END) AS unit_price "
                + "FROM medicines m "
                + "LEFT JOIN medicine_batches b ON b.medicine_id = m.id "
                + "GROUP BY m.id, m.medicine_code, m.name "
                + "ORDER BY m.name";
        List<BillableMedicine> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                BillableMedicine row = mapBillable(resultSet);
                if (row.getAvailableQty() > 0 && row.getUnitPrice() != null) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public Optional<BillableMedicine> findBillableMedicineById(long medicineId) throws SQLException {
        String sql = "SELECT m.id, m.medicine_code, m.name, "
                + "COALESCE(SUM(CASE WHEN b.exp_date >= CURDATE() THEN b.qty_in_stock ELSE 0 END), 0) AS available_qty, "
                + "MIN(CASE WHEN b.exp_date >= CURDATE() AND b.qty_in_stock > 0 THEN b.sell_price END) AS unit_price "
                + "FROM medicines m "
                + "LEFT JOIN medicine_batches b ON b.medicine_id = m.id "
                + "WHERE m.id = ? "
                + "GROUP BY m.id, m.medicine_code, m.name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, medicineId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BillableMedicine medicine = mapBillable(resultSet);
                    return Optional.of(medicine);
                }
            }
        }
        return Optional.empty();
    }

    public long createSaleWithFifo(String customerName, String paymentMode, BigDecimal discountPercent,
                                   BigDecimal taxPercent, List<BillingCartItem> cartItems, long userId)
            throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        String insertSaleSql = "INSERT INTO sales "
                + "(bill_no, customer_name, sale_date, sub_total, tax_amount, discount_amount, total_amount, payment_mode, created_by) "
                + "VALUES (?, ?, ?, 0, 0, 0, 0, ?, ?)";
        String findBatchSql = "SELECT id, qty_in_stock, sell_price "
                + "FROM medicine_batches "
                + "WHERE medicine_id = ? AND exp_date >= CURDATE() AND qty_in_stock > 0 "
                + "ORDER BY exp_date ASC, id ASC FOR UPDATE";
        String updateBatchSql = "UPDATE medicine_batches SET qty_in_stock = qty_in_stock - ? "
                + "WHERE id = ? AND qty_in_stock >= ?";
        String insertSaleItemSql = "INSERT INTO sale_items "
                + "(sale_id, batch_id, qty, rate, discount, tax, line_total) VALUES (?, ?, ?, ?, 0, 0, ?)";
        String insertStockTxnSql = "INSERT INTO stock_transactions "
                + "(batch_id, txn_type, qty, ref_type, ref_id, note, txn_date, created_by) "
                + "VALUES (?, 'SALE_OUT', ?, 'SALE', ?, ?, ?, ?)";
        String updateSaleTotalsSql = "UPDATE sales SET sub_total = ?, tax_amount = ?, discount_amount = ?, total_amount = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long saleId;
                String billNo = generateBillNo();
                Timestamp now = new Timestamp(System.currentTimeMillis());

                try (PreparedStatement statement = connection.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, billNo);
                    statement.setString(2, customerName);
                    statement.setTimestamp(3, now);
                    statement.setString(4, paymentMode);
                    statement.setLong(5, userId);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Unable to create sale header.");
                        }
                        saleId = keys.getLong(1);
                    }
                }

                BigDecimal subTotal = BigDecimal.ZERO;
                for (BillingCartItem item : cartItems) {
                    int remainingQty = item.getQty();
                    try (PreparedStatement findStatement = connection.prepareStatement(findBatchSql)) {
                        findStatement.setLong(1, item.getMedicineId());
                        try (ResultSet batchRs = findStatement.executeQuery()) {
                            while (batchRs.next() && remainingQty > 0) {
                                long batchId = batchRs.getLong("id");
                                int availableQty = batchRs.getInt("qty_in_stock");
                                BigDecimal sellPrice = batchRs.getBigDecimal("sell_price");

                                int usedQty = Math.min(remainingQty, availableQty);
                                if (usedQty <= 0) {
                                    continue;
                                }

                                try (PreparedStatement updateBatch = connection.prepareStatement(updateBatchSql)) {
                                    updateBatch.setInt(1, usedQty);
                                    updateBatch.setLong(2, batchId);
                                    updateBatch.setInt(3, usedQty);
                                    int updated = updateBatch.executeUpdate();
                                    if (updated != 1) {
                                        throw new SQLException("Stock changed during checkout. Retry billing.");
                                    }
                                }

                                BigDecimal lineTotal = sellPrice.multiply(BigDecimal.valueOf(usedQty))
                                        .setScale(2, RoundingMode.HALF_UP);
                                subTotal = subTotal.add(lineTotal);

                                try (PreparedStatement saleItemStmt = connection.prepareStatement(insertSaleItemSql)) {
                                    saleItemStmt.setLong(1, saleId);
                                    saleItemStmt.setLong(2, batchId);
                                    saleItemStmt.setInt(3, usedQty);
                                    saleItemStmt.setBigDecimal(4, sellPrice);
                                    saleItemStmt.setBigDecimal(5, lineTotal);
                                    saleItemStmt.executeUpdate();
                                }

                                try (PreparedStatement txnStmt = connection.prepareStatement(insertStockTxnSql)) {
                                    txnStmt.setLong(1, batchId);
                                    txnStmt.setInt(2, usedQty);
                                    txnStmt.setLong(3, saleId);
                                    txnStmt.setString(4, "Sale bill " + billNo);
                                    txnStmt.setTimestamp(5, now);
                                    txnStmt.setLong(6, userId);
                                    txnStmt.executeUpdate();
                                }

                                remainingQty -= usedQty;
                            }
                        }
                    }

                    if (remainingQty > 0) {
                        throw new SQLException("Insufficient non-expired stock for medicine: " + item.getMedicineName());
                    }
                }

                BigDecimal discountAmount = percentageOf(subTotal, discountPercent);
                BigDecimal taxableAmount = subTotal.subtract(discountAmount);
                BigDecimal taxAmount = percentageOf(taxableAmount, taxPercent);
                BigDecimal total = taxableAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

                try (PreparedStatement updateTotals = connection.prepareStatement(updateSaleTotalsSql)) {
                    updateTotals.setBigDecimal(1, subTotal.setScale(2, RoundingMode.HALF_UP));
                    updateTotals.setBigDecimal(2, taxAmount);
                    updateTotals.setBigDecimal(3, discountAmount);
                    updateTotals.setBigDecimal(4, total);
                    updateTotals.setLong(5, saleId);
                    updateTotals.executeUpdate();
                }

                connection.commit();
                return saleId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Optional<SaleInvoiceView> findInvoiceBySaleId(long saleId) throws SQLException {
        String saleSql = "SELECT s.id, s.bill_no, s.customer_name, s.sale_date, s.payment_mode, s.sub_total, "
                + "s.discount_amount, s.tax_amount, s.total_amount, u.username "
                + "FROM sales s LEFT JOIN users u ON u.id = s.created_by WHERE s.id = ?";
        String itemSql = "SELECT m.medicine_code, m.name AS medicine_name, b.batch_no, si.qty, si.rate, si.line_total "
                + "FROM sale_items si "
                + "INNER JOIN medicine_batches b ON b.id = si.batch_id "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "WHERE si.sale_id = ? ORDER BY si.id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement saleStmt = connection.prepareStatement(saleSql)) {
            saleStmt.setLong(1, saleId);
            try (ResultSet saleRs = saleStmt.executeQuery()) {
                if (!saleRs.next()) {
                    return Optional.empty();
                }

                SaleInvoiceView invoice = new SaleInvoiceView();
                invoice.setSaleId(saleRs.getLong("id"));
                invoice.setBillNo(saleRs.getString("bill_no"));
                invoice.setCustomerName(saleRs.getString("customer_name"));
                invoice.setSaleDate(saleRs.getTimestamp("sale_date"));
                invoice.setPaymentMode(saleRs.getString("payment_mode"));
                invoice.setSubTotal(saleRs.getBigDecimal("sub_total"));
                invoice.setDiscountAmount(saleRs.getBigDecimal("discount_amount"));
                invoice.setTaxAmount(saleRs.getBigDecimal("tax_amount"));
                invoice.setTotalAmount(saleRs.getBigDecimal("total_amount"));
                invoice.setCreatedByUser(saleRs.getString("username"));

                try (PreparedStatement itemStmt = connection.prepareStatement(itemSql)) {
                    itemStmt.setLong(1, saleId);
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        while (itemRs.next()) {
                            SaleInvoiceItemView item = new SaleInvoiceItemView();
                            item.setMedicineCode(itemRs.getString("medicine_code"));
                            item.setMedicineName(itemRs.getString("medicine_name"));
                            item.setBatchNo(itemRs.getString("batch_no"));
                            item.setQty(itemRs.getInt("qty"));
                            item.setRate(itemRs.getBigDecimal("rate"));
                            item.setLineTotal(itemRs.getBigDecimal("line_total"));
                            invoice.getItems().add(item);
                        }
                    }
                }
                return Optional.of(invoice);
            }
        }
    }

    private BillableMedicine mapBillable(ResultSet resultSet) throws SQLException {
        BillableMedicine row = new BillableMedicine();
        row.setMedicineId(resultSet.getLong("id"));
        row.setMedicineCode(resultSet.getString("medicine_code"));
        row.setMedicineName(resultSet.getString("name"));
        row.setAvailableQty(resultSet.getInt("available_qty"));
        row.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        return row;
    }

    private BigDecimal percentageOf(BigDecimal base, BigDecimal pct) {
        if (pct == null || pct.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return base.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String generateBillNo() {
        return "BILL-" + LocalDateTime.now().format(BILL_TS_FORMATTER) + "-"
                + (System.currentTimeMillis() % 1000);
    }
}
