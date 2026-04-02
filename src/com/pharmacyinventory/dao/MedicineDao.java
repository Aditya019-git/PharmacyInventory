package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.Medicine;
import com.pharmacyinventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineDao {

    public List<Medicine> findAll() throws SQLException {
        String sql = "SELECT * FROM medicines ORDER BY name";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicines.add(mapRow(resultSet));
            }
        }
        return medicines;
    }

    public Optional<Medicine> findById(long id) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Medicine> findByCode(String medicineCode) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, medicineCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean create(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines (medicine_code, name, category, brand, unit, reorder_level) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, medicine.getMedicineCode());
            statement.setString(2, medicine.getName());
            statement.setString(3, medicine.getCategory());
            statement.setString(4, medicine.getBrand());
            statement.setString(5, medicine.getUnit());
            statement.setInt(6, medicine.getReorderLevel());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, category = ?, brand = ?, unit = ?, reorder_level = ? "
                + "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, medicine.getName());
            statement.setString(2, medicine.getCategory());
            statement.setString(3, medicine.getBrand());
            statement.setString(4, medicine.getUnit());
            statement.setInt(5, medicine.getReorderLevel());
            statement.setLong(6, medicine.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM medicines WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public long countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicines";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }
        return 0;
    }

    public long countLowStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT m.id "
                + "FROM medicines m "
                + "LEFT JOIN medicine_batches b ON b.medicine_id = m.id "
                + "GROUP BY m.id, m.reorder_level "
                + "HAVING COALESCE(SUM(b.qty_in_stock), 0) < m.reorder_level"
                + ") low_stock";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }
        return 0;
    }

    public long countNearExpiry(int withinDays) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicine_batches "
                + "WHERE exp_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, withinDays);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
        return 0;
    }

    private Medicine mapRow(ResultSet resultSet) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setId(resultSet.getLong("id"));
        medicine.setMedicineCode(resultSet.getString("medicine_code"));
        medicine.setName(resultSet.getString("name"));
        medicine.setCategory(resultSet.getString("category"));
        medicine.setBrand(resultSet.getString("brand"));
        medicine.setUnit(resultSet.getString("unit"));
        medicine.setReorderLevel(resultSet.getInt("reorder_level"));
        medicine.setCreatedAt(resultSet.getTimestamp("created_at"));
        medicine.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return medicine;
    }
}
