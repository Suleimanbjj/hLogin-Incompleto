/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.service;

import dev.helix.suleiman.api.model.AccountProfile;
import dev.helix.suleiman.api.model.SessionSnapshot;

public interface PlaceholderService {

    String apply(String template, SessionSnapshot session, AccountProfile account);
}
