/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.storage;

import dev.helix.suleiman.core.config.PluginSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

public final class SchemaMigrator {

    public void migrate(DataSource dataSource, StorageType type, PluginSettings.Storage settings) throws SQLException, IOException {
        String resource = type == StorageType.SQLITE ? "schema/sqlite/V1__init.sql" : "schema/mysql/V1__init.sql";
        List<String> statements = readStatements(resource, settings.mysql().tablePrefix());
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            for (String sql : statements) {
                Statement statement = connection.createStatement();
                try {
                    statement.execute(sql);
                } finally {
                    statement.close();
                }
            }
            ensureAccountColumns(connection, type, settings.mysql().tablePrefix());
            ensureAccountColumnTypes(connection, type, settings.mysql().tablePrefix());
            connection.commit();
        } finally {
            connection.close();
        }
    }

    private List<String> readStatements(String resourcePath, String tablePrefix) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Missing schema resource: " + resourcePath);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
            String[] rawStatements = builder.toString().replace("${table_prefix}", tablePrefix).split(";");
            List<String> statements = new ArrayList<String>();
            for (String statement : rawStatements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    statements.add(trimmed);
                }
            }
            return statements;
        } finally {
            stream.close();
        }
    }

    private void ensureAccountColumns(Connection connection, StorageType type, String tablePrefix) throws SQLException {
        String table = tablePrefix + "accounts";
        Set<String> existingColumns = loadExistingColumns(connection, table);
        Map<String, String> requiredColumns = requiredAccountColumns(type);
        for (Map.Entry<String, String> entry : requiredColumns.entrySet()) {
            if (existingColumns.contains(entry.getKey())) {
                continue;
            }
            Statement statement = connection.createStatement();
            try {
                statement.execute("ALTER TABLE " + table + " ADD COLUMN " + entry.getKey() + " " + entry.getValue());
            } finally {
                statement.close();
            }
        }
    }

    private Set<String> loadExistingColumns(Connection connection, String table) throws SQLException {
        Set<String> columns = new LinkedHashSet<String>();
        DatabaseMetaData metadata = connection.getMetaData();
        String catalog = connection.getCatalog();
        ResultSet resultSet = metadata.getColumns(catalog, null, table, null);
        try {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
            }
        } finally {
            resultSet.close();
        }
        if (!columns.isEmpty()) {
            return columns;
        }
        resultSet = metadata.getColumns(catalog, null, table.toUpperCase(), null);
        try {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
            }
        } finally {
            resultSet.close();
        }
        return columns;
    }

    private Map<String, String> requiredAccountColumns(StorageType type) {
        Map<String, String> columns = new LinkedHashMap<String, String>();
        if (type == StorageType.MYSQL) {
            columns.put("captcha_successes", "BIGINT NOT NULL DEFAULT 0");
            columns.put("captcha_failures", "BIGINT NOT NULL DEFAULT 0");
            columns.put("webhook_flags", "TEXT NULL");
            columns.put("admin_notes", "TEXT NULL");
            columns.put("account_mode", "VARCHAR(16) NOT NULL DEFAULT 'OFFLINE'");
            columns.put("last_server", "VARCHAR(64) NULL");
            columns.put("last_known_version", "VARCHAR(32) NULL");
            columns.put("remember_hash", "VARCHAR(255) NULL");
            columns.put("remember_until", "BIGINT NULL");
            return columns;
        }
        columns.put("captcha_successes", "INTEGER NOT NULL DEFAULT 0");
        columns.put("captcha_failures", "INTEGER NOT NULL DEFAULT 0");
        columns.put("webhook_flags", "TEXT NULL");
        columns.put("admin_notes", "TEXT NULL");
        columns.put("account_mode", "TEXT NOT NULL DEFAULT 'OFFLINE'");
        columns.put("last_server", "TEXT NULL");
        columns.put("last_known_version", "TEXT NULL");
        columns.put("remember_hash", "TEXT NULL");
        columns.put("remember_until", "INTEGER NULL");
        return columns;
    }

    private void ensureAccountColumnTypes(Connection connection, StorageType type, String tablePrefix) throws SQLException {
        if (type != StorageType.MYSQL) {
            return;
        }
        String table = tablePrefix + "accounts";
        Statement statement = connection.createStatement();
        try {
            statement.execute("ALTER TABLE " + table + " MODIFY COLUMN last_known_version VARCHAR(255) NULL");
        } finally {
            statement.close();
        }
    }
}
