/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api;

import dev.helix.suleiman.api.service.AccountService;
import dev.helix.suleiman.api.service.AuthenticationService;
import dev.helix.suleiman.api.service.CaptchaService;
import dev.helix.suleiman.api.service.PlaceholderService;
import dev.helix.suleiman.api.service.SessionService;
import dev.helix.suleiman.api.service.TranslationService;
import dev.helix.suleiman.api.service.WebhookService;

/**
 * Stable public API exposed by hLogin for third-party integrations.
 */
public interface HLoginApi {

    AuthenticationService authentication();

    AccountService accounts();

    SessionService sessions();

    TranslationService translations();

    CaptchaService captcha();

    WebhookService webhooks();

    PlaceholderService placeholders();

    String getVersion();
}
