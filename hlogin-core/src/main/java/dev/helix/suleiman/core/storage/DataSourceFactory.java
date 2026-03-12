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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.helix.suleiman.core.config.PluginSettings;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Locale;

public final class DataSourceFactory {

    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    private static final String MYSQL_DRIVER = "dev.helix.suleiman.login.libs.mysql.cj.jdbc.Driver";

    public HikariDataSource create(PluginSettings.Storage storageSettings) {
        StorageType type = StorageType.parse(storageSettings.type());
        HikariConfig config = new HikariConfig();
        config.setPoolName("hLoginPool");
        if (type == StorageType.SQLITE) {
            ensureDriver(SQLITE_DRIVER);
            File sqliteFile = storageSettings.sqlite().file();
            File parent = sqliteFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            config.setJdbcUrl("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
            config.setDriverClassName(SQLITE_DRIVER);
            config.setMaximumPoolSize(1);
            config.setConnectionTestQuery("SELECT 1");
        } else {
            ensureDriver(MYSQL_DRIVER);
            PluginSettings.Mysql mysql = storageSettings.mysql();
            String jdbcUrl = "jdbc:mysql://" + mysql.host() + ":" + mysql.port() + "/" + mysql.database()
                + "?useSSL=" + mysql.ssl() + "&serverTimezone=UTC";
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName(MYSQL_DRIVER);
            config.setUsername(mysql.username());
            config.setPassword(mysql.password());
            config.setMaximumPoolSize(mysql.poolSize());
            config.setConnectionTimeout(mysql.connectionTimeout());
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("characterEncoding", normalizeMysqlJavaEncoding(mysql.charset()));
            String sessionCharset = normalizeMysqlSessionCharset(mysql.charset());
            if (sessionCharset != null) {
                config.addDataSourceProperty("sessionVariables",
                    "character_set_client=" + sessionCharset
                        + ",character_set_connection=" + sessionCharset
                        + ",character_set_results=" + sessionCharset);
            }
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
        }
        return new HikariDataSource(config);
    }

    private void ensureDriver(String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Missing JDBC driver on plugin classpath: " + driverClassName, exception);
        }
    }

    private String normalizeMysqlJavaEncoding(String configuredCharset) {
        String value = configuredCharset == null ? "" : configuredCharset.trim();
        if (value.isEmpty()) {
            return "UTF-8";
        }

        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_');
        if ("utf8mb4".equals(normalized) || "utf8".equals(normalized) || "utf_8".equals(normalized)) {
            return "UTF-8";
        }
        if ("latin1".equals(normalized)) {
            return "ISO-8859-1";
        }
        if ("ascii".equals(normalized)) {
            return "US-ASCII";
        }
        if (Charset.isSupported(value)) {
            return value;
        }
        return "UTF-8";
    }

    private String normalizeMysqlSessionCharset(String configuredCharset) {
        String value = configuredCharset == null ? "" : configuredCharset.trim();
        if (value.isEmpty()) {
            return null;
        }

        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_');
        if ("utf8mb4".equals(normalized)) {
            return "utf8mb4";
        }
        if ("utf8".equals(normalized) || "utf_8".equals(normalized)) {
            return "utf8";
        }
        if ("latin1".equals(normalized) || "iso_8859_1".equals(normalized)) {
            return "latin1";
        }
        if ("ascii".equals(normalized) || "us_ascii".equals(normalized)) {
            return "ascii";
        }
        return value;
    }
}
