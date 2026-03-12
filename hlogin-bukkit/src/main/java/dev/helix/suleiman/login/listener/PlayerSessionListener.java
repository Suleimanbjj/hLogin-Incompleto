/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.listener;

import dev.helix.suleiman.api.event.PlayerSessionEndEvent;
import dev.helix.suleiman.api.event.PlayerSessionStartEvent;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.login.HLoginPlugin;
import dev.helix.suleiman.login.util.LocaleHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerSessionListener implements Listener {
    private final HLoginPlugin plugin;

    public PlayerSessionListener(HLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getCore().sessionCoordinator()
            .initialize(player, LocaleHelper.resolve(player, plugin.getCore().settings().general().defaultLanguage()), plugin.getServer().getVersion())
            .whenComplete((session, throwable) -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    plugin.getLogger().warning("Unable to initialize session for " + player.getName() + ": " + throwable.getMessage());
                    player.kickPlayer(plugin.translate(player, "general.unexpected-error"));
                    return;
                }
                if (!player.isOnline()) {
                    return;
                }
                plugin.getVisualService().showForSession(player);
                plugin.touchReminder(player);
                plugin.applyProtectionState(player);
                Bukkit.getPluginManager().callEvent(new PlayerSessionStartEvent(player, session.snapshot()));
            }));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SessionSnapshot snapshot = plugin.getCore().sessions().getSession(player.getUniqueId()).orElse(null);
        if (snapshot != null) {
            Bukkit.getPluginManager().callEvent(new PlayerSessionEndEvent(player, snapshot));
        }
        plugin.getVisualService().clear(player);
        plugin.getCore().sessions().invalidate(player.getUniqueId(), false);
    }
}
