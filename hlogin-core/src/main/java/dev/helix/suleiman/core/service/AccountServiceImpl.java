/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.service;

import dev.helix.suleiman.api.model.AccountProfile;
import dev.helix.suleiman.api.model.OperationResult;
import dev.helix.suleiman.api.service.AccountService;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.model.AccountRecord;
import dev.helix.suleiman.core.model.AuditEntry;
import dev.helix.suleiman.core.security.PasswordHasher;
import dev.helix.suleiman.core.util.StringNormalizer;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AccountServiceImpl implements AccountService {
    private final CoreContainer container;

    public AccountServiceImpl(CoreContainer container) {
        this.container = container;
    }

    @Override
    public CompletableFuture<Optional<AccountProfile>> findByUniqueId(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(uniqueId);
            return account.isPresent() ? Optional.of(account.get().toProfile()) : Optional.<AccountProfile>empty();
        }, container.executorService());
    }

    @Override
    public CompletableFuture<Optional<AccountProfile>> findByName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByNormalizedName(StringNormalizer.normalizeName(playerName));
            return account.isPresent() ? Optional.of(account.get().toProfile()) : Optional.<AccountProfile>empty();
        }, container.executorService());
    }

    @Override
    public CompletableFuture<OperationResult> unregister(UUID uniqueId, UUID actorUniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(uniqueId);
            if (!account.isPresent()) {
                return OperationResult.failure("admin.user-not-found");
            }
            container.storageContext().accountRepository().delete(uniqueId);
            container.sessionRegistry().remove(uniqueId);
            audit(uniqueId, account.get().currentName(), actorUniqueId, "ADMIN_UNREGISTER", account.get().lastLoginIp(), "unregister");
            return OperationResult.success("admin.unregister-success", account.get().currentName());
        }, container.executorService());
    }

    @Override
    public CompletableFuture<OperationResult> resetPassword(UUID uniqueId, String newPassword, UUID actorUniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(uniqueId);
            if (!account.isPresent()) {
                return OperationResult.failure("admin.user-not-found");
            }
            PasswordHasher hasher = container.passwordHasherFactory().current(container.settings().security().algorithm());
            container.storageContext().accountRepository().updatePassword(uniqueId, hasher.hash(newPassword), hasher.algorithm());
            audit(uniqueId, account.get().currentName(), actorUniqueId, "ADMIN_RESET_PASSWORD", account.get().lastLoginIp(), "password reset");
            return OperationResult.success("admin.resetpassword-success", account.get().currentName());
        }, container.executorService());
    }

    @Override
    public CompletableFuture<OperationResult> setLanguage(UUID uniqueId, String language, UUID actorUniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(uniqueId);
            if (!account.isPresent()) {
                return OperationResult.failure("admin.user-not-found");
            }
            String resolvedLanguage = container.translationRegistry().resolveLocale(language);
            container.storageContext().accountRepository().updateLanguage(uniqueId, resolvedLanguage);
            if (container.sessionRegistry().get(uniqueId) != null) {
                container.sessionRegistry().get(uniqueId).language(resolvedLanguage);
            }
            audit(uniqueId, account.get().currentName(), actorUniqueId, "ADMIN_SET_LANGUAGE", account.get().lastLoginIp(), resolvedLanguage);
            return OperationResult.success("admin.language-set", resolvedLanguage);
        }, container.executorService());
    }

    private void audit(UUID subjectUniqueId, String subjectName, UUID actorUniqueId, String action, String ip, String details) {
        if (!container.settings().general().auditEnabled()) {
            return;
        }
        container.storageContext().auditRepository().append(new AuditEntry(subjectUniqueId, subjectName, actorUniqueId, action, ip, details, Instant.now()));
    }
}
