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

import dev.helix.suleiman.api.enums.AccountMode;
import dev.helix.suleiman.api.enums.HashAlgorithm;
import dev.helix.suleiman.core.model.AccountRecord;
import dev.helix.suleiman.core.storage.repository.AccountRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

public final class JdbcAccountRepository implements AccountRepository {
    private final DataSource dataSource;
    private final String accountsTable;

    public JdbcAccountRepository(DataSource dataSource, String tablePrefix) {
        this.dataSource = dataSource;
        this.accountsTable = tablePrefix + "accounts";
    }

    @Override
    public Optional<AccountRecord> findByUniqueId(UUID uniqueId) {
        return querySingle("SELECT * FROM " + accountsTable + " WHERE unique_id = ?", uniqueId.toString());
    }

    @Override
    public Optional<AccountRecord> findByNormalizedName(String normalizedName) {
        return querySingle("SELECT * FROM " + accountsTable + " WHERE normalized_name = ?", normalizedName);
    }

    @Override
    public void insert(AccountRecord accountRecord) {
        String sql = "INSERT INTO " + accountsTable + " (unique_id,current_name,normalized_name,password_hash,hash_algorithm,"
            + "registration_ip,last_login_ip,first_login,last_login,register_date,language,total_logins,failed_attempts,"
            + "blocked_until,captcha_successes,captcha_failures,webhook_flags,admin_notes,account_mode,last_server,"
            + "last_known_version,remember_hash,remember_until) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                bindAccount(statement, accountRecord);
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to insert account", ex);
        } finally {
            close(connection);
        }
    }

    @Override
    public void update(AccountRecord accountRecord) {
        String sql = "UPDATE " + accountsTable + " SET current_name=?,normalized_name=?,password_hash=?,hash_algorithm=?,"
            + "registration_ip=?,last_login_ip=?,first_login=?,last_login=?,register_date=?,language=?,total_logins=?,"
            + "failed_attempts=?,blocked_until=?,captcha_successes=?,captcha_failures=?,webhook_flags=?,admin_notes=?,"
            + "account_mode=?,last_server=?,last_known_version=?,remember_hash=?,remember_until=? WHERE unique_id=?";
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                statement.setString(1, accountRecord.currentName());
                statement.setString(2, accountRecord.normalizedName());
                statement.setString(3, accountRecord.passwordHash());
                statement.setString(4, accountRecord.hashAlgorithm().name());
                statement.setString(5, accountRecord.registrationIp());
                statement.setString(6, accountRecord.lastLoginIp());
                setInstant(statement, 7, accountRecord.firstLogin());
                setInstant(statement, 8, accountRecord.lastLogin());
                setInstant(statement, 9, accountRecord.registerDate());
                statement.setString(10, accountRecord.language());
                statement.setLong(11, accountRecord.totalLogins());
                statement.setInt(12, accountRecord.failedAttempts());
                setInstant(statement, 13, accountRecord.blockedUntil());
                statement.setLong(14, accountRecord.captchaSuccessCount());
                statement.setLong(15, accountRecord.captchaFailureCount());
                statement.setString(16, accountRecord.webhookFlags());
                statement.setString(17, accountRecord.adminNotes());
                statement.setString(18, accountRecord.accountMode().name());
                statement.setString(19, accountRecord.lastServer());
                statement.setString(20, accountRecord.lastKnownVersion());
                statement.setString(21, accountRecord.rememberHash());
                setInstant(statement, 22, accountRecord.rememberUntil());
                statement.setString(23, accountRecord.uniqueId().toString());
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to update account", ex);
        } finally {
            close(connection);
        }
    }

    @Override
    public void updateFailedAttempts(UUID uniqueId, int failedAttempts, Instant blockedUntil) {
        executeUpdate("UPDATE " + accountsTable + " SET failed_attempts=?, blocked_until=? WHERE unique_id=?",
            new Binder() {
                @Override
                public void bind(PreparedStatement statement) throws SQLException {
                    statement.setInt(1, failedAttempts);
                    setInstant(statement, 2, blockedUntil);
                    statement.setString(3, uniqueId.toString());
                }
            });
    }

    @Override
    public void updateLoginSuccess(UUID uniqueId, String currentName, String lastLoginIp, Instant lastLogin,
                                   long totalLogins, int failedAttempts, Instant blockedUntil, String rememberHash,
                                   Instant rememberUntil, String lastServer, String lastKnownVersion) {
        executeUpdate("UPDATE " + accountsTable + " SET current_name=?, last_login_ip=?, last_login=?, total_logins=?, "
                + "failed_attempts=?, blocked_until=?, remember_hash=?, remember_until=?, last_server=?, last_known_version=? "
                + "WHERE unique_id=?", new Binder() {
            @Override
            public void bind(PreparedStatement statement) throws SQLException {
                statement.setString(1, currentName);
                statement.setString(2, lastLoginIp);
                setInstant(statement, 3, lastLogin);
                statement.setLong(4, totalLogins);
                statement.setInt(5, failedAttempts);
                setInstant(statement, 6, blockedUntil);
                statement.setString(7, rememberHash);
                setInstant(statement, 8, rememberUntil);
                statement.setString(9, truncate(lastServer, 64));
                statement.setString(10, truncate(lastKnownVersion, 255));
                statement.setString(11, uniqueId.toString());
            }
        });
    }

    @Override
    public void updateLanguage(UUID uniqueId, String language) {
        executeUpdate("UPDATE " + accountsTable + " SET language=? WHERE unique_id=?", new Binder() {
            @Override
            public void bind(PreparedStatement statement) throws SQLException {
                statement.setString(1, language);
                statement.setString(2, uniqueId.toString());
            }
        });
    }

    @Override
    public void updatePassword(UUID uniqueId, String hash, HashAlgorithm algorithm) {
        executeUpdate("UPDATE " + accountsTable + " SET password_hash=?, hash_algorithm=? WHERE unique_id=?", new Binder() {
            @Override
            public void bind(PreparedStatement statement) throws SQLException {
                statement.setString(1, hash);
                statement.setString(2, algorithm.name());
                statement.setString(3, uniqueId.toString());
            }
        });
    }

    @Override
    public void updateCaptchaStats(UUID uniqueId, long successCount, long failureCount) {
        executeUpdate("UPDATE " + accountsTable + " SET captcha_successes=?, captcha_failures=? WHERE unique_id=?", new Binder() {
            @Override
            public void bind(PreparedStatement statement) throws SQLException {
                statement.setLong(1, successCount);
                statement.setLong(2, failureCount);
                statement.setString(3, uniqueId.toString());
            }
        });
    }

    @Override
    public void delete(UUID uniqueId) {
        executeUpdate("DELETE FROM " + accountsTable + " WHERE unique_id=?", new Binder() {
            @Override
            public void bind(PreparedStatement statement) throws SQLException {
                statement.setString(1, uniqueId.toString());
            }
        });
    }

    private Optional<AccountRecord> querySingle(String sql, Object parameter) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                statement.setObject(1, parameter);
                ResultSet resultSet = statement.executeQuery();
                try {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(map(resultSet));
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to query account", ex);
        } finally {
            close(connection);
        }
    }

    private AccountRecord map(ResultSet resultSet) throws SQLException {
        return new AccountRecord(
            UUID.fromString(resultSet.getString("unique_id")),
            resultSet.getString("current_name"),
            resultSet.getString("normalized_name"),
            resultSet.getString("password_hash"),
            HashAlgorithm.valueOf(resultSet.getString("hash_algorithm")),
            resultSet.getString("registration_ip"),
            resultSet.getString("last_login_ip"),
            getInstant(resultSet, "first_login"),
            getInstant(resultSet, "last_login"),
            getInstant(resultSet, "register_date"),
            resultSet.getString("language"),
            resultSet.getLong("total_logins"),
            resultSet.getInt("failed_attempts"),
            getInstant(resultSet, "blocked_until"),
            resultSet.getLong("captcha_successes"),
            resultSet.getLong("captcha_failures"),
            resultSet.getString("webhook_flags"),
            resultSet.getString("admin_notes"),
            AccountMode.valueOf(resultSet.getString("account_mode")),
            resultSet.getString("last_server"),
            resultSet.getString("last_known_version"),
            resultSet.getString("remember_hash"),
            getInstant(resultSet, "remember_until")
        );
    }

    private void bindAccount(PreparedStatement statement, AccountRecord accountRecord) throws SQLException {
        statement.setString(1, accountRecord.uniqueId().toString());
        statement.setString(2, truncate(accountRecord.currentName(), 16));
        statement.setString(3, truncate(accountRecord.normalizedName(), 16));
        statement.setString(4, accountRecord.passwordHash());
        statement.setString(5, accountRecord.hashAlgorithm().name());
        statement.setString(6, truncate(accountRecord.registrationIp(), 64));
        statement.setString(7, truncate(accountRecord.lastLoginIp(), 64));
        setInstant(statement, 8, accountRecord.firstLogin());
        setInstant(statement, 9, accountRecord.lastLogin());
        setInstant(statement, 10, accountRecord.registerDate());
        statement.setString(11, truncate(accountRecord.language(), 32));
        statement.setLong(12, accountRecord.totalLogins());
        statement.setInt(13, accountRecord.failedAttempts());
        setInstant(statement, 14, accountRecord.blockedUntil());
        statement.setLong(15, accountRecord.captchaSuccessCount());
        statement.setLong(16, accountRecord.captchaFailureCount());
        statement.setString(17, accountRecord.webhookFlags());
        statement.setString(18, accountRecord.adminNotes());
        statement.setString(19, truncate(accountRecord.accountMode().name(), 16));
        statement.setString(20, truncate(accountRecord.lastServer(), 64));
        statement.setString(21, truncate(accountRecord.lastKnownVersion(), 255));
        statement.setString(22, truncate(accountRecord.rememberHash(), 255));
        setInstant(statement, 23, accountRecord.rememberUntil());
    }

    private void executeUpdate(String sql, Binder binder) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                binder.bind(statement);
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to execute account update", ex);
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to get JDBC connection", ex);
        }
    }

    private void close(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private static void setInstant(PreparedStatement statement, int index, Instant instant) throws SQLException {
        if (instant == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, instant.toEpochMilli());
        }
    }

    private static Instant getInstant(ResultSet resultSet, String column) throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : Instant.ofEpochMilli(value);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
