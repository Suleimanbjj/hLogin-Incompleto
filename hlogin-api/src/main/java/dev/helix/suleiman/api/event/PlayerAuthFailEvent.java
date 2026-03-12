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
import org.bukkit.event.HandlerList;

public final class PlayerAuthFailEvent extends AbstractPlayerSessionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String reasonKey;

    public PlayerAuthFailEvent(Player player, SessionSnapshot session, String reasonKey) {
        super(player, session);
        this.reasonKey = reasonKey;
    }

    public String getReasonKey() { return reasonKey; }
    public static HandlerList getHandlerList() { return HANDLERS; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
