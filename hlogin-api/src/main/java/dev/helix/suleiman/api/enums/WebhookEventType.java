/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.enums;

public enum WebhookEventType {
    REGISTER_SUCCESS,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_CREATED,
    ACCOUNT_BLOCKED,
    PASSWORD_CHANGED,
    CAPTCHA_FAILED,
    SUSPICIOUS_ACTIVITY,
    ADMIN_FORCE_UNREGISTER,
    ADMIN_FORCE_RESET_PASSWORD
}
