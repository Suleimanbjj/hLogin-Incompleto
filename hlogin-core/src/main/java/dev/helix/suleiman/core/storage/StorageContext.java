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

import dev.helix.suleiman.core.storage.repository.AccountRepository;
import dev.helix.suleiman.core.storage.repository.AuditRepository;
import javax.sql.DataSource;

public final class StorageContext {
    private final StorageType type;
    private final DataSource dataSource;
    private final AccountRepository accountRepository;
    private final AuditRepository auditRepository;
    private final AutoCloseable closeable;

    public StorageContext(StorageType type, DataSource dataSource, AccountRepository accountRepository,
                          AuditRepository auditRepository, AutoCloseable closeable) {
        this.type = type;
        this.dataSource = dataSource;
        this.accountRepository = accountRepository;
        this.auditRepository = auditRepository;
        this.closeable = closeable;
    }

    public StorageType type() { return type; }
    public DataSource dataSource() { return dataSource; }
    public AccountRepository accountRepository() { return accountRepository; }
    public AuditRepository auditRepository() { return auditRepository; }

    public void close() {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }
}
