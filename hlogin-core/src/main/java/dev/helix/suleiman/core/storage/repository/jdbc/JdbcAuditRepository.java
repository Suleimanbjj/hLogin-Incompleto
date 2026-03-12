/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.storage.repository.jdbc;

import dev.helix.suleiman.core.model.AuditEntry;
import dev.helix.suleiman.core.storage.repository.AuditRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;

public final class JdbcAuditRepository implements AuditRepository {
    private final DataSource dataSource;
    private final String auditTable;

    public JdbcAuditRepository(DataSource dataSource, String tablePrefix) {
        this.dataSource = dataSource;
        this.auditTable = tablePrefix + "audit";
    }

    @Override
    public void append(AuditEntry auditEntry) {
        String sql = "INSERT INTO " + auditTable + " (subject_uuid,subject_name,actor_uuid,action,ip_address,details,created_at) "
            + "VALUES (?,?,?,?,?,?,?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, toString(auditEntry.subjectUniqueId()));
            statement.setString(2, auditEntry.subjectName());
            statement.setString(3, toString(auditEntry.actorUniqueId()));
            statement.setString(4, auditEntry.action());
            statement.setString(5, auditEntry.ipAddress());
            statement.setString(6, auditEntry.details());
            statement.setLong(7, auditEntry.createdAt().toEpochMilli());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to append audit entry", ex);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private String toString(UUID uniqueId) {
        return uniqueId == null ? null : uniqueId.toString();
    }
}
