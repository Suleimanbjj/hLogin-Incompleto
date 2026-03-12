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

import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.SessionSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public final class PlayerCaptchaSuccessEvent extends AbstractPlayerSessionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final CaptchaType captchaType;

    public PlayerCaptchaSuccessEvent(Player player, SessionSnapshot session, CaptchaType captchaType) {
        super(player, session);
        this.captchaType = captchaType;
    }

    public CaptchaType getCaptchaType() { return captchaType; }
    public static HandlerList getHandlerList() { return HANDLERS; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
}
