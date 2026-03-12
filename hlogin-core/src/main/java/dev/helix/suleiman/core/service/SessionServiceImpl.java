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

import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.api.service.SessionService;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.session.RuntimeSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SessionServiceImpl implements SessionService {
    private final CoreContainer container;

    public SessionServiceImpl(CoreContainer container) {
        this.container = container;
    }

    @Override
    public Optional<SessionSnapshot> getSession(UUID uniqueId) {
        RuntimeSession session = container.sessionRegistry().get(uniqueId);
        return session == null ? Optional.<SessionSnapshot>empty() : Optional.of(session.snapshot());
    }

    @Override
    public Collection<SessionSnapshot> getActiveSessions() {
        List<SessionSnapshot> snapshots = new ArrayList<SessionSnapshot>();
        for (RuntimeSession session : container.sessionRegistry().values()) {
            snapshots.add(session.snapshot());
        }
        return snapshots;
    }

    @Override
    public boolean isAuthenticated(UUID uniqueId) {
        RuntimeSession session = container.sessionRegistry().get(uniqueId);
        return session != null && session.isAuthenticated();
    }

    @Override
    public void invalidate(UUID uniqueId, boolean invalidateRememberSession) {
        container.sessionRegistry().remove(uniqueId);
    }
}
