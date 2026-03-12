/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private final Map<UUID, RuntimeSession> sessions = new ConcurrentHashMap<UUID, RuntimeSession>();

    public RuntimeSession getOrCreate(RuntimeSession session) {
        RuntimeSession existing = sessions.putIfAbsent(session.uniqueId(), session);
        return existing == null ? session : existing;
    }

    public RuntimeSession get(UUID uniqueId) {
        return sessions.get(uniqueId);
    }

    public RuntimeSession remove(UUID uniqueId) {
        return sessions.remove(uniqueId);
    }

    public Collection<RuntimeSession> values() {
        return Collections.unmodifiableCollection(sessions.values());
    }
}
