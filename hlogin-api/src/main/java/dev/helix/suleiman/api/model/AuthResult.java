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
import java.time.Instant;

public final class AuthResult {
    private final boolean success;
    private final String messageKey;
    private final AuthState resultingState;
    private final int attemptsRemaining;
    private final Instant blockedUntil;
    private final CaptchaChallenge captchaChallenge;

    public AuthResult(boolean success, String messageKey, AuthState resultingState, int attemptsRemaining,
                      Instant blockedUntil, CaptchaChallenge captchaChallenge) {
        this.success = success;
        this.messageKey = messageKey;
        this.resultingState = resultingState;
        this.attemptsRemaining = attemptsRemaining;
        this.blockedUntil = blockedUntil;
        this.captchaChallenge = captchaChallenge;
    }

    public boolean isSuccess() { return success; }
    public String getMessageKey() { return messageKey; }
    public AuthState getResultingState() { return resultingState; }
    public int getAttemptsRemaining() { return attemptsRemaining; }
    public Instant getBlockedUntil() { return blockedUntil; }
    public CaptchaChallenge getCaptchaChallenge() { return captchaChallenge; }

    public static AuthResult success(String messageKey, AuthState resultingState) {
        return new AuthResult(true, messageKey, resultingState, 0, null, null);
    }

    public static AuthResult failure(String messageKey, AuthState state, int attemptsRemaining,
                                     Instant blockedUntil, CaptchaChallenge captchaChallenge) {
        return new AuthResult(false, messageKey, state, attemptsRemaining, blockedUntil, captchaChallenge);
    }
}
