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

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.login.HLoginPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public final class PlayerProtectionListener implements Listener {
    private final HLoginPlugin plugin;

    public PlayerProtectionListener(HLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!shouldProtect(event.getPlayer()) || !plugin.getCore().settings().protection().blockMovement() || event.getPlayer().hasPermission("hlogin.bypass.movement")) {
            return;
        }
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (shouldProtect(player) && plugin.getCore().settings().protection().blockDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (shouldProtect(event.getPlayer()) && plugin.getCore().settings().protection().blockItemDrop()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (shouldProtect(event.getPlayer()) && plugin.getCore().settings().protection().blockItemPickup()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (shouldProtect(event.getPlayer()) && plugin.getCore().settings().protection().blockInteraction()) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (shouldProtect(player) && plugin.getCore().settings().protection().blockInventoryClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SessionSnapshot snapshot = plugin.getCore().sessions().getSession(player.getUniqueId()).orElse(null);
        if (snapshot == null || snapshot.isLogged()) {
            return;
        }
        if (snapshot.getState() == AuthState.CAPTCHA_PENDING) {
            CaptchaChallenge challenge = plugin.getCore().captcha().getActiveChallenge(player.getUniqueId());
            if (challenge != null && challenge.getType() != CaptchaType.INVENTORY) {
                event.setCancelled(true);
                String response = event.getMessage();
                Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getCore().authentication().validateCaptcha(player, response)
                        .whenComplete((result, throwable) -> Bukkit.getScheduler().runTask(plugin, () -> {
                            if (throwable != null) {
                                Throwable cause = unwrap(throwable);
                                plugin.getLogger().warning("Captcha chat validation failed for " + player.getName() + ": " + cause.getMessage());
                                plugin.sendMessage(player, "general.unexpected-error");
                                return;
                            }
                            plugin.handleAuthResult(player, result, "captcha");
                        })));
                return;
            }
        }
        if (plugin.getCore().settings().protection().blockChat()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String commandLine = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();
        commandLine = commandLine.trim();
        if (commandLine.isEmpty()) {
            return;
        }
        String[] split = commandLine.split("\\s+");
        if (split.length == 0) {
            return;
        }
        String label = split[0];
        String canonical = plugin.getAliasRegistry().resolve(label);
        String[] args = new String[Math.max(0, split.length - 1)];
        if (split.length > 1) {
            System.arraycopy(split, 1, args, 0, split.length - 1);
        }
        if (canonical != null) {
            event.setCancelled(true);
            plugin.getRootCommandExecutor().executeCanonical(event.getPlayer(), canonical, args);
            return;
        }
        if (shouldProtect(event.getPlayer()) && plugin.getCore().settings().protection().blockCommands()
            && !event.getPlayer().hasPermission("hlogin.bypass.commandblock")
            && !plugin.getAliasRegistry().isWhitelistedForUnauth(label)) {
            event.setCancelled(true);
            plugin.sendMessage(event.getPlayer(), "protection.command-blocked");
        }
    }

    private boolean shouldProtect(Player player) {
        SessionSnapshot snapshot = plugin.getCore().sessions().getSession(player.getUniqueId()).orElse(null);
        return snapshot != null && !snapshot.isLogged() && !player.hasPermission("hlogin.bypass.loginblock");
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException || current instanceof ExecutionException) {
            if (current.getCause() == null) {
                break;
            }
            current = current.getCause();
        }
        return current;
    }
}
