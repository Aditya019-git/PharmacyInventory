package com.pharmacyinventory.dao;

import com.pharmacyinventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuditDao {

    public void logAction(String entityName, long entityId, String action, Long changedBy) throws SQLException {
        String sql = "INSERT INTO audit_logs (entity_name, entity_id, action, old_data, new_data, changed_by, changed_at) "
                + "VALUES (?, ?, ?, NULL, NULL, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entityName);
            statement.setLong(2, entityId);
            statement.setString(3, action);
            if (changedBy == null || changedBy <= 0) {
                statement.setNull(4, java.sql.Types.BIGINT);
            } else {
                statement.setLong(4, changedBy);
            }
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        }
    }
}
