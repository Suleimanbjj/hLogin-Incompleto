/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.event;

import dev.helix.suleiman.api.model.SessionSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class AbstractPlayerSessionEvent extends Event {
    private final Player player;
    private final SessionSnapshot session;

    protected AbstractPlayerSessionEvent(Player player, SessionSnapshot session) {
        this.player = player;
        this.session = session;
    }

    public Player getPlayer() {
        return player;
    }

    public SessionSnapshot getSession() {
        return session;
    }
}
