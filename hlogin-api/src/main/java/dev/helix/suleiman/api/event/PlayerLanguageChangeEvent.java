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

public final class PlayerLanguageChangeEvent extends AbstractPlayerSessionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String oldLanguage;
    private final String newLanguage;

    public PlayerLanguageChangeEvent(Player player, SessionSnapshot session, String oldLanguage, String newLanguage) {
        super(player, session);
        this.oldLanguage = oldLanguage;
        this.newLanguage = newLanguage;
    }

    public String getOldLanguage() { return oldLanguage; }
    public String getNewLanguage() { return newLanguage; }
    public static HandlerList getHandlerList() { return HANDLERS; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
