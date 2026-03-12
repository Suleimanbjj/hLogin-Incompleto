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

public final class PlayerPasswordResetEvent extends AbstractPlayerSessionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String issuerName;

    public PlayerPasswordResetEvent(Player player, SessionSnapshot session, String issuerName) {
        super(player, session);
        this.issuerName = issuerName;
    }

    public String getIssuerName() { return issuerName; }
    public static HandlerList getHandlerList() { return HANDLERS; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
