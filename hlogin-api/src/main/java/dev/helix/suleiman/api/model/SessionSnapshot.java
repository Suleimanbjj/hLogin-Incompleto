/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.model;

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import java.time.Instant;
import java.util.UUID;

public final class SessionSnapshot {
    private final UUID uniqueId;
    private final String playerName;
    private final AuthState state;
    private final String language;
    private final boolean registered;
    private final boolean logged;
    private final int failedAttempts;
    private final CaptchaType captchaType;
    private final Instant blockedUntil;
    private final Instant authenticatedAt;
    private final Instant expiresAt;
    private final boolean remembered;

    public SessionSnapshot(UUID uniqueId, String playerName, AuthState state, String language, boolean registered,
                           boolean logged, int failedAttempts, CaptchaType captchaType, Instant blockedUntil,
                           Instant authenticatedAt, Instant expiresAt, boolean remembered) {
        this.uniqueId = uniqueId;
        this.playerName = playerName;
        this.state = state;
        this.language = language;
        this.registered = registered;
        this.logged = logged;
        this.failedAttempts = failedAttempts;
        this.captchaType = captchaType;
        this.blockedUntil = blockedUntil;
        this.authenticatedAt = authenticatedAt;
        this.expiresAt = expiresAt;
        this.remembered = remembered;
    }

    public UUID getUniqueId() { return uniqueId; }
    public String getPlayerName() { return playerName; }
    public AuthState getState() { return state; }
    public String getLanguage() { return language; }
    public boolean isRegistered() { return registered; }
    public boolean isLogged() { return logged; }
    public int getFailedAttempts() { return failedAttempts; }
    public CaptchaType getCaptchaType() { return captchaType; }
    public Instant getBlockedUntil() { return blockedUntil; }
    public Instant getAuthenticatedAt() { return authenticatedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRemembered() { return remembered; }
}
