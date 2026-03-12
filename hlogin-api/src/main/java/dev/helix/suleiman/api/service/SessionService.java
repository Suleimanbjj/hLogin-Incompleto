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

import dev.helix.suleiman.api.model.SessionSnapshot;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    Optional<SessionSnapshot> getSession(UUID uniqueId);

    Collection<SessionSnapshot> getActiveSessions();

    boolean isAuthenticated(UUID uniqueId);

    void invalidate(UUID uniqueId, boolean invalidateRememberSession);
}
