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

import dev.helix.suleiman.api.enums.HashAlgorithm;
import dev.helix.suleiman.core.model.AccountRecord;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<AccountRecord> findByUniqueId(UUID uniqueId);

    Optional<AccountRecord> findByNormalizedName(String normalizedName);

    void insert(AccountRecord accountRecord);

    void update(AccountRecord accountRecord);

    void updateFailedAttempts(UUID uniqueId, int failedAttempts, Instant blockedUntil);

    void updateLoginSuccess(UUID uniqueId, String currentName, String lastLoginIp, Instant lastLogin,
                            long totalLogins, int failedAttempts, Instant blockedUntil, String rememberHash,
                            Instant rememberUntil, String lastServer, String lastKnownVersion);

    void updateLanguage(UUID uniqueId, String language);

    void updatePassword(UUID uniqueId, String hash, HashAlgorithm algorithm);

    void updateCaptchaStats(UUID uniqueId, long successCount, long failureCount);

    void delete(UUID uniqueId);
}
