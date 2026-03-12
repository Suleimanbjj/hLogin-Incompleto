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

import dev.helix.suleiman.api.enums.AccountMode;
import dev.helix.suleiman.api.enums.HashAlgorithm;
import java.time.Instant;
import java.util.UUID;

public final class AccountProfile {
    private final UUID uniqueId;
    private final String currentName;
    private final String normalizedName;
    private final HashAlgorithm hashAlgorithm;
    private final String registrationIp;
    private final String lastLoginIp;
    private final Instant firstLogin;
    private final Instant lastLogin;
    private final Instant registerDate;
    private final String language;
    private final long totalLogins;
    private final int failedAttempts;
    private final Instant blockedUntil;
    private final long captchaSuccessCount;
    private final long captchaFailureCount;
    private final String adminNotes;
    private final AccountMode accountMode;
    private final String lastServer;
    private final String lastKnownVersion;
    private final Instant rememberUntil;

    public AccountProfile(UUID uniqueId, String currentName, String normalizedName, HashAlgorithm hashAlgorithm,
                          String registrationIp, String lastLoginIp, Instant firstLogin, Instant lastLogin,
                          Instant registerDate, String language, long totalLogins, int failedAttempts,
                          Instant blockedUntil, long captchaSuccessCount, long captchaFailureCount,
                          String adminNotes, AccountMode accountMode, String lastServer,
                          String lastKnownVersion, Instant rememberUntil) {
        this.uniqueId = uniqueId;
        this.currentName = currentName;
        this.normalizedName = normalizedName;
        this.hashAlgorithm = hashAlgorithm;
        this.registrationIp = registrationIp;
        this.lastLoginIp = lastLoginIp;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
        this.registerDate = registerDate;
        this.language = language;
        this.totalLogins = totalLogins;
        this.failedAttempts = failedAttempts;
        this.blockedUntil = blockedUntil;
        this.captchaSuccessCount = captchaSuccessCount;
        this.captchaFailureCount = captchaFailureCount;
        this.adminNotes = adminNotes;
        this.accountMode = accountMode;
        this.lastServer = lastServer;
        this.lastKnownVersion = lastKnownVersion;
        this.rememberUntil = rememberUntil;
    }

    public UUID getUniqueId() { return uniqueId; }
    public String getCurrentName() { return currentName; }
    public String getNormalizedName() { return normalizedName; }
    public HashAlgorithm getHashAlgorithm() { return hashAlgorithm; }
    public String getRegistrationIp() { return registrationIp; }
    public String getLastLoginIp() { return lastLoginIp; }
    public Instant getFirstLogin() { return firstLogin; }
    public Instant getLastLogin() { return lastLogin; }
    public Instant getRegisterDate() { return registerDate; }
    public String getLanguage() { return language; }
    public long getTotalLogins() { return totalLogins; }
    public int getFailedAttempts() { return failedAttempts; }
    public Instant getBlockedUntil() { return blockedUntil; }
    public long getCaptchaSuccessCount() { return captchaSuccessCount; }
    public long getCaptchaFailureCount() { return captchaFailureCount; }
    public String getAdminNotes() { return adminNotes; }
    public AccountMode getAccountMode() { return accountMode; }
    public String getLastServer() { return lastServer; }
    public String getLastKnownVersion() { return lastKnownVersion; }
    public Instant getRememberUntil() { return rememberUntil; }
}
