/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.model;

import java.time.Instant;
import java.util.UUID;

public final class AuditEntry {
    private final UUID subjectUniqueId;
    private final String subjectName;
    private final UUID actorUniqueId;
    private final String action;
    private final String ipAddress;
    private final String details;
    private final Instant createdAt;

    public AuditEntry(UUID subjectUniqueId, String subjectName, UUID actorUniqueId, String action,
                      String ipAddress, String details, Instant createdAt) {
        this.subjectUniqueId = subjectUniqueId;
        this.subjectName = subjectName;
        this.actorUniqueId = actorUniqueId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID subjectUniqueId() { return subjectUniqueId; }
    public String subjectName() { return subjectName; }
    public UUID actorUniqueId() { return actorUniqueId; }
    public String action() { return action; }
    public String ipAddress() { return ipAddress; }
    public String details() { return details; }
    public Instant createdAt() { return createdAt; }
}
