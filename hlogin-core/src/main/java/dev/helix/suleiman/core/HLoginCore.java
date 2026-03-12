/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core;

import dev.helix.suleiman.api.HLoginApi;
import dev.helix.suleiman.api.service.AccountService;
import dev.helix.suleiman.api.service.AuthenticationService;
import dev.helix.suleiman.api.service.CaptchaService;
import dev.helix.suleiman.api.service.PlaceholderService;
import dev.helix.suleiman.api.service.SessionService;
import dev.helix.suleiman.api.service.TranslationService;
import dev.helix.suleiman.api.service.WebhookService;
import dev.helix.suleiman.core.config.PluginSettings;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.service.AccountServiceImpl;
import dev.helix.suleiman.core.service.AuthenticationServiceImpl;
import dev.helix.suleiman.core.service.CaptchaServiceImpl;
import dev.helix.suleiman.core.service.PlaceholderServiceImpl;
import dev.helix.suleiman.core.service.SessionCoordinator;
import dev.helix.suleiman.core.service.SessionServiceImpl;
import dev.helix.suleiman.core.service.TranslationServiceImpl;
import dev.helix.suleiman.core.service.WebhookServiceImpl;

public final class HLoginCore implements HLoginApi {
    private final CoreContainer container;
    private final AuthenticationServiceImpl authenticationService;
    private final AccountServiceImpl accountService;
    private final SessionServiceImpl sessionService;
    private final TranslationServiceImpl translationService;
    private final CaptchaServiceImpl captchaService;
    private final WebhookServiceImpl webhookService;
    private final PlaceholderServiceImpl placeholderService;
    private final SessionCoordinator sessionCoordinator;

    HLoginCore(CoreContainer container, AuthenticationServiceImpl authenticationService, AccountServiceImpl accountService,
               SessionServiceImpl sessionService, TranslationServiceImpl translationService, CaptchaServiceImpl captchaService,
               WebhookServiceImpl webhookService, PlaceholderServiceImpl placeholderService, SessionCoordinator sessionCoordinator) {
        this.container = container;
        this.authenticationService = authenticationService;
        this.accountService = accountService;
        this.sessionService = sessionService;
        this.translationService = translationService;
        this.captchaService = captchaService;
        this.webhookService = webhookService;
        this.placeholderService = placeholderService;
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    public AuthenticationService authentication() {
        return authenticationService;
    }

    @Override
    public AccountService accounts() {
        return accountService;
    }

    @Override
    public SessionService sessions() {
        return sessionService;
    }

    @Override
    public TranslationService translations() {
        return translationService;
    }

    @Override
    public CaptchaService captcha() {
        return captchaService;
    }

    @Override
    public WebhookService webhooks() {
        return webhookService;
    }

    @Override
    public PlaceholderService placeholders() {
        return placeholderService;
    }

    @Override
    public String getVersion() {
        return container.pluginVersion();
    }

    public CoreContainer container() {
        return container;
    }

    public SessionCoordinator sessionCoordinator() {
        return sessionCoordinator;
    }

    public PluginSettings settings() {
        return container.settings();
    }

    public void reload() throws Exception {
        HLoginCoreBootstrap.reloadInto(this);
    }

    public void shutdown() {
        container.shutdown();
    }
}
