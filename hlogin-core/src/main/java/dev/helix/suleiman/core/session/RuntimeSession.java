/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.session;

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.core.captcha.CaptchaTicket;
import java.time.Instant;
import java.util.UUID;

public final class RuntimeSession {
    private final UUID uniqueId;
    private volatile String playerName;
    private volatile String language;
    private volatile String ipAddress;
    private volatile String version;
    private volatile AuthState state;
    private volatile boolean registered;
    private volatile boolean remembered;
    private volatile int failedAttempts;
    private volatile Instant blockedUntil;
    private volatile Instant authenticatedAt;
    private volatile Instant expiresAt;
    private volatile Instant joinAt;
    private volatile Instant lastReminderAt;
    private volatile boolean loaded;
    private volatile CaptchaTicket captchaTicket;
    private volatile boolean registerCaptchaVerified;
    private volatile boolean loginCaptchaVerified;

    public RuntimeSession(UUID uniqueId, String playerName, String language, String ipAddress, String version) {
        this.uniqueId = uniqueId;
        this.playerName = playerName;
        this.language = language;
        this.ipAddress = ipAddress;
        this.version = version;
        this.state = AuthState.REGISTERED_NOT_LOGGED;
        this.joinAt = Instant.now();
        this.expiresAt = Instant.now();
    }

    public UUID uniqueId() { return uniqueId; }
    public String playerName() { return playerName; }
    public void playerName(String playerName) { this.playerName = playerName; }
    public String language() { return language; }
    public void language(String language) { this.language = language; }
    public String ipAddress() { return ipAddress; }
    public void ipAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String version() { return version; }
    public void version(String version) { this.version = version; }
    public AuthState state() { return state; }
    public void state(AuthState state) { this.state = state; }
    public boolean registered() { return registered; }
    public void registered(boolean registered) { this.registered = registered; }
    public boolean remembered() { return remembered; }
    public void remembered(boolean remembered) { this.remembered = remembered; }
    public int failedAttempts() { return failedAttempts; }
    public void failedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public Instant blockedUntil() { return blockedUntil; }
    public void blockedUntil(Instant blockedUntil) { this.blockedUntil = blockedUntil; }
    public Instant authenticatedAt() { return authenticatedAt; }
    public void authenticatedAt(Instant authenticatedAt) { this.authenticatedAt = authenticatedAt; }
    public Instant expiresAt() { return expiresAt; }
    public void expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant joinAt() { return joinAt; }
    public Instant lastReminderAt() { return lastReminderAt; }
    public void lastReminderAt(Instant lastReminderAt) { this.lastReminderAt = lastReminderAt; }
    public boolean loaded() { return loaded; }
    public void loaded(boolean loaded) { this.loaded = loaded; }
    public CaptchaTicket captchaTicket() { return captchaTicket; }
    public void captchaTicket(CaptchaTicket captchaTicket) { this.captchaTicket = captchaTicket; }
    public boolean registerCaptchaVerified() { return registerCaptchaVerified; }
    public void registerCaptchaVerified(boolean registerCaptchaVerified) { this.registerCaptchaVerified = registerCaptchaVerified; }
    public boolean loginCaptchaVerified() { return loginCaptchaVerified; }
    public void loginCaptchaVerified(boolean loginCaptchaVerified) { this.loginCaptchaVerified = loginCaptchaVerified; }

    public boolean isAuthenticated() {
        return state == AuthState.LOGGED;
    }

    public SessionSnapshot snapshot() {
        return new SessionSnapshot(uniqueId, playerName, state, language, registered, isAuthenticated(), failedAttempts,
            captchaTicket == null ? null : captchaTicket.type(), blockedUntil, authenticatedAt, expiresAt, remembered);
    }

    public boolean isBlockedNow() {
        return blockedUntil != null && blockedUntil.isAfter(Instant.now());
    }

    public boolean expired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
}
