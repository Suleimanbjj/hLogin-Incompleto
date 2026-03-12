/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.service;

import dev.helix.suleiman.api.model.AccountProfile;
import dev.helix.suleiman.api.model.OperationResult;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountService {

    CompletableFuture<Optional<AccountProfile>> findByUniqueId(UUID uniqueId);

    CompletableFuture<Optional<AccountProfile>> findByName(String playerName);

    CompletableFuture<OperationResult> unregister(UUID uniqueId, UUID actorUniqueId);

    CompletableFuture<OperationResult> resetPassword(UUID uniqueId, String newPassword, UUID actorUniqueId);

    CompletableFuture<OperationResult> setLanguage(UUID uniqueId, String language, UUID actorUniqueId);
}
