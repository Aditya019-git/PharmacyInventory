package com.pharmacyinventory.dao;

import com.pharmacyinventory.model.User;
import com.pharmacyinventory.util.DBConnection;
import com.pharmacyinventory.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {

    public Optional<User> authenticate(String username, String rawPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        String hash = PasswordUtil.sha256(rawPassword);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapRow(resultSet);
                    if (hash.equals(user.getPasswordHash())) {
                        return Optional.of(user);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(resultSet.getString("role"));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return user;
    }
}
