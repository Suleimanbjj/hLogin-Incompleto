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

public final class CaptchaResult {
    private final boolean success;
    private final String messageKey;
    private final int remainingAttempts;

    public CaptchaResult(boolean success, String messageKey, int remainingAttempts) {
        this.success = success;
        this.messageKey = messageKey;
        this.remainingAttempts = remainingAttempts;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public static CaptchaResult success(String messageKey) {
        return new CaptchaResult(true, messageKey, 0);
    }

    public static CaptchaResult failure(String messageKey, int remainingAttempts) {
        return new CaptchaResult(false, messageKey, remainingAttempts);
    }
}
