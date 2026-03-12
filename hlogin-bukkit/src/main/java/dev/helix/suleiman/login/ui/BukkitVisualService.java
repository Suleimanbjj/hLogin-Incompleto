/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.ui;

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.core.HLoginCore;
import dev.helix.suleiman.core.captcha.CaptchaAnimal;
import dev.helix.suleiman.login.compat.visual.VisualAdapter;
import dev.helix.suleiman.login.util.BukkitCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public final class BukkitVisualService {
    private final HLoginCore core;
    private final VisualAdapter adapter;
    private final Plugin plugin;
    private final Map<UUID, String> openInventoryChallenges = new HashMap<UUID, String>();

    public BukkitVisualService(HLoginCore core, VisualAdapter adapter, Plugin plugin) {
        this.core = core;
        this.adapter = adapter;
        this.plugin = plugin;
    }

    public VisualAdapter adapter() {
        return adapter;
    }

    public void clear(Player player) {
        clearOverlays(player);
        openInventoryChallenges.remove(player.getUniqueId());
    }

    private void clearOverlays(Player player) {
        adapter.clearBossBar(player);
        adapter.clearHologram(player);
    }

    public void showForSession(Player player) {
        SessionSnapshot snapshot = core.sessions().getSession(player.getUniqueId()).orElse(null);
        if (snapshot == null) {
            return;
        }
        if (snapshot.getState() == AuthState.LOGGED) {
            clear(player);
            return;
        }
        String locale = snapshot.getLanguage();
        if (snapshot.getState() == AuthState.CAPTCHA_PENDING) {
            CaptchaChallenge challenge = core.captcha().getActiveChallenge(player.getUniqueId());
            if (challenge != null) {
                showCaptcha(player, challenge, locale);
            }
            return;
        }
        clear(player);
        if (snapshot.getState() == AuthState.UNREGISTERED) {
            showRegisterPrompt(player, locale);
            return;
        }
        if (snapshot.getState() == AuthState.REGISTERED_NOT_LOGGED || snapshot.getState() == AuthState.BLOCKED) {
            showLoginPrompt(player, locale);
        }
    }

    public void showRegisterPrompt(Player player, String locale) {
        String title = core.translations().render(locale, "visual.register.title");
        String subtitle = core.translations().render(locale, "visual.register.subtitle");
        if (shouldSendTitle()) {
            adapter.sendTitle(player, title, subtitle, 5, 50, 10);
        }
        if (core.settings().visual().actionBarEnabled()) {
            adapter.sendActionBar(player, core.translations().render(locale, "auth.register-prompt"));
        }
        if (shouldSendBossBar()) {
            adapter.showBossBar(player, core.translations().render(locale, "auth.register-prompt"), 1.0F);
        }
        if (core.settings().visual().hologramEnabled() && adapter.supportsHologramChannel()) {
            scheduleVisual(1L, () -> adapter.showHologram(player, lines(locale, "visual.register.hologram"), hologramLocation(player)));
        }
    }

    public void showLoginPrompt(Player player, String locale) {
        String title = core.translations().render(locale, "visual.login.title");
        String subtitle = core.translations().render(locale, "visual.login.subtitle");
        if (shouldSendTitle()) {
            adapter.sendTitle(player, title, subtitle, 5, 50, 10);
        }
        if (core.settings().visual().actionBarEnabled()) {
            adapter.sendActionBar(player, core.translations().render(locale, "auth.login-prompt"));
        }
        if (shouldSendBossBar()) {
            adapter.showBossBar(player, core.translations().render(locale, "auth.login-prompt"), 1.0F);
        }
        if (core.settings().visual().hologramEnabled() && adapter.supportsHologramChannel()) {
            scheduleVisual(1L, () -> adapter.showHologram(player, lines(locale, "visual.login.hologram"), hologramLocation(player)));
        }
    }

    public void showCaptcha(Player player, CaptchaChallenge challenge, String locale) {
        clearOverlays(player);
        boolean inventoryCaptcha = challenge.getType() == CaptchaType.INVENTORY && core.settings().visual().inventoryCaptchaEnabled();
        if (shouldSendTitle()) {
            adapter.sendTitle(player, core.translations().render(locale, "visual.captcha.title"), core.translations().render(locale, "visual.captcha.subtitle"), 5, 40, 10);
        }
        if (core.settings().visual().actionBarEnabled()) {
            adapter.sendActionBar(player, challenge.getPrompt());
        }
        if (!inventoryCaptcha && shouldSendBossBar()) {
            adapter.showBossBar(player, core.translations().render(locale, "captcha.solve-now"), 1.0F);
        }
        if (inventoryCaptcha) {
            openInventoryCaptcha(player, challenge, locale);
        } else if (core.settings().visual().hologramEnabled() && adapter.supportsHologramChannel()) {
            List<String> lines = new ArrayList<String>(lines(locale, "visual.captcha.hologram"));
            lines.add(challenge.getPrompt());
            scheduleVisual(1L, () -> adapter.showHologram(player, lines, hologramLocation(player)));
        } else {
            player.sendMessage(challenge.getPrompt());
        }
    }

    private void openInventoryCaptcha(Player player, CaptchaChallenge challenge, String locale) {
        String challengeId = challenge.getMetadata().get("challenge_id");
        String title = challenge.getMetadata().containsKey("inventory_title")
            ? challenge.getMetadata().get("inventory_title")
            : core.translations().render(locale, "captcha.inventory-title");
        title = title.length() > 32 ? title.substring(0, 32) : title;
        if (challengeId != null
            && challengeId.equals(openInventoryChallenges.get(player.getUniqueId()))
            && title.equals(inventoryTitle(player))) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 9, title);
        for (int slot = 2; slot <= 6; slot++) {
            String animalId = challenge.getMetadata().get("slot_" + slot);
            if (animalId == null) {
                continue;
            }
            CaptchaAnimal animal = CaptchaAnimal.fromId(animalId);
            String displayName = "\u00A7e" + (animal == null ? animalId : animalName(locale, animal));
            inventory.setItem(slot, BukkitCompat.createCaptchaHead(animalId, displayName));
        }
        openInventoryChallenges.put(player.getUniqueId(), challengeId);
        final String expectedChallengeId = challengeId;
        scheduleVisual(1L, () -> {
            CaptchaChallenge current = core.captcha().getActiveChallenge(player.getUniqueId());
            if (!player.isOnline() || current == null || current.getType() != CaptchaType.INVENTORY) {
                return;
            }
            String currentChallengeId = current.getMetadata().get("challenge_id");
            if (expectedChallengeId != null && !expectedChallengeId.equals(currentChallengeId)) {
                return;
            }
            player.openInventory(inventory);
            BukkitCompat.updateInventory(player);
            player.sendMessage(challenge.getPrompt());
        });
    }

    private List<String> lines(String locale, String key) {
        return core.translations().renderList(locale, key);
    }

    private Location hologramLocation(Player player) {
        Location eyeLocation = player.getEyeLocation().clone();
        Vector direction = eyeLocation.getDirection().normalize().multiply(2.2D);
        return eyeLocation.add(direction).add(0.0D, 0.35D, 0.0D);
    }

    private boolean shouldSendTitle() {
        return core.settings().visual().titleEnabled()
            && (adapter.supportsTitleChannel()
                || (!core.settings().visual().actionBarEnabled()
                    && !core.settings().visual().bossBarEnabled()
                    && !core.settings().visual().hologramEnabled()));
    }

    private boolean shouldSendBossBar() {
        if (!core.settings().visual().bossBarEnabled()) {
            return false;
        }
        return adapter.supportsBossBarChannel() || !core.settings().visual().actionBarEnabled();
    }

    private String inventoryTitle(Player player) {
        return player.getOpenInventory() == null ? "" : player.getOpenInventory().getTitle();
    }

    private String animalName(String locale, CaptchaAnimal animal) {
        String key = animal.translationKey();
        String translated = core.translations().render(locale, key);
        if (!key.equals(translated)) {
            return translated;
        }
        String id = animal.id();
        return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1).toLowerCase(Locale.ROOT);
    }

    private void scheduleVisual(long delayTicks, Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }
}
