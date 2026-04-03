package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.InventoryBatchView;
import com.pharmacyinventory.model.Medicine;
import com.pharmacyinventory.model.StockTransactionView;
import com.pharmacyinventory.model.Supplier;
import com.pharmacyinventory.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {

    public List<Medicine> findMedicineOptions() throws SQLException {
        String sql = "SELECT id, medicine_code, name FROM medicines ORDER BY name";
        List<Medicine> medicines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Medicine medicine = new Medicine();
                medicine.setId(resultSet.getLong("id"));
                medicine.setMedicineCode(resultSet.getString("medicine_code"));
                medicine.setName(resultSet.getString("name"));
                medicines.add(medicine);
            }
        }
        return medicines;
    }

    public List<Supplier> findSupplierOptions() throws SQLException {
        String sql = "SELECT id, supplier_code, name FROM suppliers ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(resultSet.getLong("id"));
                supplier.setSupplierCode(resultSet.getString("supplier_code"));
                supplier.setName(resultSet.getString("name"));
                suppliers.add(supplier);
            }
        }
        return suppliers;
    }

    public List<InventoryBatchView> findAllBatches() throws SQLException {
        String sql = "SELECT b.id, m.medicine_code, m.name AS medicine_name, b.batch_no, b.mfg_date, b.exp_date, "
                + "b.qty_in_stock, b.cost_price, b.sell_price, s.name AS supplier_name "
                + "FROM medicine_batches b "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "LEFT JOIN suppliers s ON s.id = b.supplier_id "
                + "ORDER BY b.exp_date ASC, m.name ASC";
        List<InventoryBatchView> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(mapBatchRow(resultSet));
            }
        }
        return rows;
    }

    public List<InventoryBatchView> findExpiringWithinDays(int days) throws SQLException {
        String sql = "SELECT b.id, m.medicine_code, m.name AS medicine_name, b.batch_no, b.mfg_date, b.exp_date, "
                + "b.qty_in_stock, b.cost_price, b.sell_price, s.name AS supplier_name "
                + "FROM medicine_batches b "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "LEFT JOIN suppliers s ON s.id = b.supplier_id "
                + "WHERE b.exp_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) "
                + "ORDER BY b.exp_date ASC, m.name ASC";
        List<InventoryBatchView> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapBatchRow(resultSet));
                }
            }
        }
        return rows;
    }

    public List<InventoryBatchView> findExpiredBatches() throws SQLException {
        String sql = "SELECT b.id, m.medicine_code, m.name AS medicine_name, b.batch_no, b.mfg_date, b.exp_date, "
                + "b.qty_in_stock, b.cost_price, b.sell_price, s.name AS supplier_name "
                + "FROM medicine_batches b "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "LEFT JOIN suppliers s ON s.id = b.supplier_id "
                + "WHERE b.exp_date < CURDATE() "
                + "ORDER BY b.exp_date DESC, m.name ASC";
        List<InventoryBatchView> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                InventoryBatchView row = mapBatchRow(resultSet);
                row.setExpired(true);
                rows.add(row);
            }
        }
        return rows;
    }

    public List<StockTransactionView> findRecentTransactions(int limit) throws SQLException {
        String sql = "SELECT st.id, st.txn_type, st.qty, st.ref_type, st.note, st.txn_date, "
                + "m.medicine_code, m.name AS medicine_name, b.batch_no, u.username "
                + "FROM stock_transactions st "
                + "INNER JOIN medicine_batches b ON b.id = st.batch_id "
                + "INNER JOIN medicines m ON m.id = b.medicine_id "
                + "LEFT JOIN users u ON u.id = st.created_by "
                + "ORDER BY st.txn_date DESC "
                + "LIMIT ?";
        List<StockTransactionView> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    StockTransactionView row = new StockTransactionView();
                    row.setId(resultSet.getLong("id"));
                    row.setTxnType(resultSet.getString("txn_type"));
                    row.setQty(resultSet.getInt("qty"));
                    row.setRefType(resultSet.getString("ref_type"));
                    row.setNote(resultSet.getString("note"));
                    row.setTxnDate(resultSet.getTimestamp("txn_date"));
                    row.setMedicineCode(resultSet.getString("medicine_code"));
                    row.setMedicineName(resultSet.getString("medicine_name"));
                    row.setBatchNo(resultSet.getString("batch_no"));
                    row.setUserName(resultSet.getString("username"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public boolean addBatchWithOpeningStock(long medicineId, Long supplierId, String batchNo, Date mfgDate,
                                            Date expDate, int qty, BigDecimal costPrice, BigDecimal sellPrice,
                                            long userId) throws SQLException {
        String batchSql = "INSERT INTO medicine_batches "
                + "(medicine_id, supplier_id, batch_no, mfg_date, exp_date, qty_in_stock, cost_price, sell_price) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String stockTxnSql = "INSERT INTO stock_transactions "
                + "(batch_id, txn_type, qty, ref_type, ref_id, note, txn_date, created_by) "
                + "VALUES (?, 'PURCHASE_IN', ?, 'OPENING', NULL, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long batchId;
                try (PreparedStatement statement = connection.prepareStatement(batchSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, medicineId);
                    if (supplierId == null) {
                        statement.setNull(2, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(2, supplierId);
                    }
                    statement.setString(3, batchNo);
                    statement.setDate(4, mfgDate);
                    statement.setDate(5, expDate);
                    statement.setInt(6, qty);
                    statement.setBigDecimal(7, costPrice);
                    statement.setBigDecimal(8, sellPrice);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Unable to create batch.");
                        }
                        batchId = keys.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(stockTxnSql)) {
                    statement.setLong(1, batchId);
                    statement.setInt(2, qty);
                    statement.setString(3, "Opening stock");
                    statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    statement.setLong(5, userId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean adjustStock(long batchId, String direction, int qty, String note, long userId) throws SQLException {
        String txType = "OUT".equalsIgnoreCase(direction) ? "ADJUSTMENT_OUT" : "ADJUSTMENT_IN";
        int delta = "OUT".equalsIgnoreCase(direction) ? -qty : qty;
        String updateSql = "UPDATE medicine_batches SET qty_in_stock = qty_in_stock + ? "
                + "WHERE id = ? AND qty_in_stock + ? >= 0";
        String txnSql = "INSERT INTO stock_transactions "
                + "(batch_id, txn_type, qty, ref_type, ref_id, note, txn_date, created_by) "
                + "VALUES (?, ?, ?, 'ADJUSTMENT', NULL, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int updatedRows;
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setInt(1, delta);
                    statement.setLong(2, batchId);
                    statement.setInt(3, delta);
                    updatedRows = statement.executeUpdate();
                }

                if (updatedRows != 1) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement statement = connection.prepareStatement(txnSql)) {
                    statement.setLong(1, batchId);
                    statement.setString(2, txType);
                    statement.setInt(3, qty);
                    statement.setString(4, note);
                    statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    statement.setLong(6, userId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private InventoryBatchView mapBatchRow(ResultSet resultSet) throws SQLException {
        InventoryBatchView row = new InventoryBatchView();
        row.setBatchId(resultSet.getLong("id"));
        row.setMedicineCode(resultSet.getString("medicine_code"));
        row.setMedicineName(resultSet.getString("medicine_name"));
        row.setBatchNo(resultSet.getString("batch_no"));
        row.setMfgDate(resultSet.getDate("mfg_date"));
        row.setExpDate(resultSet.getDate("exp_date"));
        row.setQtyInStock(resultSet.getInt("qty_in_stock"));
        row.setCostPrice(resultSet.getBigDecimal("cost_price"));
        row.setSellPrice(resultSet.getBigDecimal("sell_price"));
        row.setSupplierName(resultSet.getString("supplier_name"));
        row.setExpired(row.getExpDate() != null && row.getExpDate().before(Date.valueOf(LocalDate.now())));
        return row;
    }
}
