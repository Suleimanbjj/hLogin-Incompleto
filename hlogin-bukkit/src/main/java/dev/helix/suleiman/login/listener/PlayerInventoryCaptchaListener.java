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

import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.AuthResult;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.core.service.AuthenticationServiceImpl;
import dev.helix.suleiman.login.HLoginPlugin;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class PlayerInventoryCaptchaListener implements Listener {
    private final HLoginPlugin plugin;
    private final Set<UUID> pending = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    public PlayerInventoryCaptchaListener(HLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryCaptcha(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        SessionSnapshot snapshot = plugin.getCore().sessions().getSession(player.getUniqueId()).orElse(null);
        if (snapshot == null || snapshot.isLogged()) {
            return;
        }
        CaptchaChallenge challenge = plugin.getCore().captcha().getActiveChallenge(player.getUniqueId());
        if (challenge == null || challenge.getType() != CaptchaType.INVENTORY) {
            return;
        }
        event.setCancelled(true);
        String expectedTitle = challenge.getMetadata().get("inventory_title");
        if (expectedTitle != null && !expectedTitle.equals(event.getView().getTitle())) {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }
        String selectedAnimal = challenge.getMetadata().get("slot_" + event.getRawSlot());
        if (selectedAnimal == null) {
            return;
        }
        if (!pending.add(player.getUniqueId())) {
            return;
        }
        String purpose = challenge.getMetadata().get("purpose");
        if ("REGISTER".equalsIgnoreCase(purpose)) {
            try {
                AuthResult result = ((AuthenticationServiceImpl) plugin.getCore().authentication()).validateCaptchaNow(player, selectedAnimal);
                pending.remove(player.getUniqueId());
                plugin.handleAuthResult(player, result, "captcha");
            } catch (Exception exception) {
                pending.remove(player.getUniqueId());
                plugin.getLogger().warning("Captcha GUI validation failed for " + player.getName() + ": " + exception.getMessage());
            }
            return;
        }
        plugin.getCore().authentication().validateCaptcha(player, selectedAnimal)
            .whenComplete((result, throwable) -> Bukkit.getScheduler().runTask(plugin, () -> {
                pending.remove(player.getUniqueId());
                if (throwable != null) {
                    plugin.getLogger().warning("Captcha GUI validation failed for " + player.getName() + ": " + throwable.getMessage());
                    return;
                }
                plugin.handleAuthResult(player, result, "captcha");
            }));
    }
}
