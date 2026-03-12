/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.util;

import org.bukkit.entity.Player;

public final class LocaleHelper {
    private LocaleHelper() {
    }

    public static String resolve(Player player, String fallback) {
        try {
            Object value = player.getClass().getMethod("getLocale").invoke(player);
            return value == null ? fallback : String.valueOf(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
