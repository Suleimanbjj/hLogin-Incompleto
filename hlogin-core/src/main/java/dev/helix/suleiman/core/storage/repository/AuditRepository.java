/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.storage.repository;

import dev.helix.suleiman.core.model.AuditEntry;

public interface AuditRepository {

    void append(AuditEntry auditEntry);
}
