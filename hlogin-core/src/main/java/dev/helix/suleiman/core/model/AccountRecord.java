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

import dev.helix.suleiman.api.enums.AccountMode;
import dev.helix.suleiman.api.enums.HashAlgorithm;
import dev.helix.suleiman.api.model.AccountProfile;
import java.time.Instant;
import java.util.UUID;

public final class AccountRecord {
    private final UUID uniqueId;
    private final String currentName;
    private final String normalizedName;
    private final String passwordHash;
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
    private final String webhookFlags;
    private final String adminNotes;
    private final AccountMode accountMode;
    private final String lastServer;
    private final String lastKnownVersion;
    private final String rememberHash;
    private final Instant rememberUntil;

    public AccountRecord(UUID uniqueId, String currentName, String normalizedName, String passwordHash,
                         HashAlgorithm hashAlgorithm, String registrationIp, String lastLoginIp, Instant firstLogin,
                         Instant lastLogin, Instant registerDate, String language, long totalLogins, int failedAttempts,
                         Instant blockedUntil, long captchaSuccessCount, long captchaFailureCount, String webhookFlags,
                         String adminNotes, AccountMode accountMode, String lastServer, String lastKnownVersion,
                         String rememberHash, Instant rememberUntil) {
        this.uniqueId = uniqueId;
        this.currentName = currentName;
        this.normalizedName = normalizedName;
        this.passwordHash = passwordHash;
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
        this.webhookFlags = webhookFlags;
        this.adminNotes = adminNotes;
        this.accountMode = accountMode;
        this.lastServer = lastServer;
        this.lastKnownVersion = lastKnownVersion;
        this.rememberHash = rememberHash;
        this.rememberUntil = rememberUntil;
    }

    public UUID uniqueId() { return uniqueId; }
    public String currentName() { return currentName; }
    public String normalizedName() { return normalizedName; }
    public String passwordHash() { return passwordHash; }
    public HashAlgorithm hashAlgorithm() { return hashAlgorithm; }
    public String registrationIp() { return registrationIp; }
    public String lastLoginIp() { return lastLoginIp; }
    public Instant firstLogin() { return firstLogin; }
    public Instant lastLogin() { return lastLogin; }
    public Instant registerDate() { return registerDate; }
    public String language() { return language; }
    public long totalLogins() { return totalLogins; }
    public int failedAttempts() { return failedAttempts; }
    public Instant blockedUntil() { return blockedUntil; }
    public long captchaSuccessCount() { return captchaSuccessCount; }
    public long captchaFailureCount() { return captchaFailureCount; }
    public String webhookFlags() { return webhookFlags; }
    public String adminNotes() { return adminNotes; }
    public AccountMode accountMode() { return accountMode; }
    public String lastServer() { return lastServer; }
    public String lastKnownVersion() { return lastKnownVersion; }
    public String rememberHash() { return rememberHash; }
    public Instant rememberUntil() { return rememberUntil; }

    public AccountProfile toProfile() {
        return new AccountProfile(uniqueId, currentName, normalizedName, hashAlgorithm, registrationIp, lastLoginIp, firstLogin,
            lastLogin, registerDate, language, totalLogins, failedAttempts, blockedUntil, captchaSuccessCount,
            captchaFailureCount, adminNotes, accountMode, lastServer, lastKnownVersion, rememberUntil);
    }
}
