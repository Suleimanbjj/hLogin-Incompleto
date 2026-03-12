/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.compat.visual;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface VisualAdapter {

    String family();

    boolean supportsTitleChannel();

    boolean supportsActionBarChannel();

    boolean supportsBossBarChannel();

    boolean supportsHologramChannel();

    void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void sendActionBar(Player player, String message);

    void showBossBar(Player player, String message, float progress);

    void clearBossBar(Player player);

    void showHologram(Player player, List<String> lines, Location location);

    void clearHologram(Player player);

    void hidePlayer(Plugin plugin, Player viewer, Player target);

    void showPlayer(Plugin plugin, Player viewer, Player target);
}
