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

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.core.captcha.CaptchaPurpose;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.model.AccountRecord;
import dev.helix.suleiman.core.session.RuntimeSession;
import dev.helix.suleiman.core.util.DigestUtil;
import dev.helix.suleiman.core.util.StringNormalizer;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public final class SessionCoordinator {
    private final CoreContainer container;
    private final CaptchaServiceImpl captchaService;

    public SessionCoordinator(CoreContainer container, CaptchaServiceImpl captchaService) {
        this.container = container;
        this.captchaService = captchaService;
    }

    public CompletableFuture<RuntimeSession> initialize(Player player, String localeHint, String version) {
        return CompletableFuture.supplyAsync(() -> loadSession(player, localeHint, version), container.executorService());
    }

    private RuntimeSession loadSession(Player player, String localeHint, String version) {
        RuntimeSession created = new RuntimeSession(player.getUniqueId(), player.getName(),
            container.translationRegistry().resolveLocale(localeHint), safeIp(player), version);
        RuntimeSession session = container.sessionRegistry().getOrCreate(created);
        session.playerName(player.getName());
        session.language(container.translationRegistry().resolveLocale(localeHint));
        session.ipAddress(safeIp(player));
        session.version(version);

        Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(player.getUniqueId());
        if (!account.isPresent()) {
            account = container.storageContext().accountRepository().findByNormalizedName(StringNormalizer.normalizeName(player.getName()));
        }

        Instant now = Instant.now();
        if (!account.isPresent()) {
            session.registered(false);
            session.failedAttempts(0);
            session.blockedUntil(null);
            session.state(AuthState.UNREGISTERED);
            session.expiresAt(now.plusSeconds(container.settings().general().authTimeoutSeconds()));
            if (container.settings().captcha().enabled() && container.settings().captcha().requireOnRegister()) {
                captchaService.issue(session, CaptchaPurpose.REGISTER, AuthState.UNREGISTERED, container.settings().captcha().defaultType());
            }
            session.loaded(true);
            return session;
        }

        AccountRecord record = account.get();
        session.registered(true);
        session.language(record.language() == null || record.language().isEmpty() ? session.language() : record.language());
        session.failedAttempts(record.failedAttempts());
        session.blockedUntil(record.blockedUntil());
        if (record.blockedUntil() != null && record.blockedUntil().isAfter(now)) {
            session.state(AuthState.BLOCKED);
        } else if (shouldAutoLogin(record, session)) {
            markLogged(session);
            session.remembered(true);
        } else {
            session.state(AuthState.REGISTERED_NOT_LOGGED);
            session.expiresAt(now.plusSeconds(container.settings().general().authTimeoutSeconds()));
            if (container.settings().captcha().enabled() && container.settings().captcha().requireOnLogin()) {
                captchaService.issue(session, CaptchaPurpose.LOGIN, AuthState.REGISTERED_NOT_LOGGED, container.settings().captcha().defaultType());
            }
        }
        session.loaded(true);
        return session;
    }

    public void markLogged(RuntimeSession session) {
        Instant now = Instant.now();
        session.state(AuthState.LOGGED);
        session.authenticatedAt(now);
        session.expiresAt(now.plusSeconds(container.settings().session().expireMinutes() * 60L));
        session.failedAttempts(0);
        session.blockedUntil(null);
    }

    public void markLoggedOut(RuntimeSession session) {
        session.state(session.registered() ? AuthState.REGISTERED_NOT_LOGGED : AuthState.UNREGISTERED);
        session.authenticatedAt(null);
        session.remembered(false);
        session.expiresAt(Instant.now().plusSeconds(container.settings().general().authTimeoutSeconds()));
    }

    public String fingerprint(RuntimeSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append(session.ipAddress());
        if (container.settings().session().rememberDevice()) {
            builder.append('|').append(session.version());
        }
        builder.append('|').append(session.playerName());
        return DigestUtil.sha256(builder.toString());
    }

    private boolean shouldAutoLogin(AccountRecord record, RuntimeSession session) {
        if (!container.settings().session().rememberSession()) {
            return false;
        }
        if (container.settings().session().requireReauthOnReconnect()) {
            return false;
        }
        if (record.rememberUntil() == null || record.rememberUntil().isBefore(Instant.now())) {
            return false;
        }
        if (record.rememberHash() == null || record.rememberHash().isEmpty()) {
            return false;
        }
        return record.rememberHash().equals(fingerprint(session));
    }

    private String safeIp(Player player) {
        return player.getAddress() == null ? "unknown" : player.getAddress().getAddress().getHostAddress();
    }
}
