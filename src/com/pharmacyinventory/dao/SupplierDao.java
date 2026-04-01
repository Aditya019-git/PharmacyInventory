package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.Supplier;
import com.pharmacyinventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDao {

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT * FROM suppliers ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                suppliers.add(mapRow(resultSet));
            }
        }
        return suppliers;
    }

    public Optional<Supplier> findById(long id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id = ?";

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

    public Optional<Supplier> findByCode(String supplierCode) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, supplierCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean create(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (supplier_code, name, phone, email, gst_no, address_line) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, supplier.getSupplierCode());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getGstNo());
            statement.setString(6, supplier.getAddressLine());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, phone = ?, email = ?, gst_no = ?, address_line = ? "
                + "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getPhone());
            statement.setString(3, supplier.getEmail());
            statement.setString(4, supplier.getGstNo());
            statement.setString(5, supplier.getAddressLine());
            statement.setLong(6, supplier.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Supplier mapRow(ResultSet resultSet) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(resultSet.getLong("id"));
        supplier.setSupplierCode(resultSet.getString("supplier_code"));
        supplier.setName(resultSet.getString("name"));
        supplier.setPhone(resultSet.getString("phone"));
        supplier.setEmail(resultSet.getString("email"));
        supplier.setGstNo(resultSet.getString("gst_no"));
        supplier.setAddressLine(resultSet.getString("address_line"));
        supplier.setCreatedAt(resultSet.getTimestamp("created_at"));
        supplier.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return supplier;
    }
}
