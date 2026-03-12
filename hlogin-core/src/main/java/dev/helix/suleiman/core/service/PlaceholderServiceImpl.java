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

import dev.helix.suleiman.api.model.AccountProfile;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.api.service.PlaceholderService;

public final class PlaceholderServiceImpl implements PlaceholderService {

    @Override
    public String apply(String template, SessionSnapshot session, AccountProfile account) {
        String rendered = template == null ? "" : template;
        rendered = rendered.replace("%hlogin_status%", session == null ? "UNKNOWN" : session.getState().name());
        rendered = rendered.replace("%hlogin_logged%", String.valueOf(session != null && session.isLogged()));
        rendered = rendered.replace("%hlogin_registered%", String.valueOf(session != null && session.isRegistered()));
        rendered = rendered.replace("%hlogin_attempts%", String.valueOf(session == null ? 0 : session.getFailedAttempts()));
        rendered = rendered.replace("%hlogin_language%", session != null ? session.getLanguage() : account != null ? account.getLanguage() : "en");
        rendered = rendered.replace("%hlogin_last_login%", account != null && account.getLastLogin() != null ? account.getLastLogin().toString() : "never");
        rendered = rendered.replace("%hlogin_first_login%", account != null && account.getFirstLogin() != null ? account.getFirstLogin().toString() : "never");
        rendered = rendered.replace("%hlogin_total_logins%", String.valueOf(account == null ? 0L : account.getTotalLogins()));
        return rendered;
    }
}
