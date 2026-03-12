/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login;

import dev.helix.suleiman.api.HLoginApi;
import dev.helix.suleiman.api.event.PlayerAuthFailEvent;
import dev.helix.suleiman.api.event.PlayerAuthLoginEvent;
import dev.helix.suleiman.api.event.PlayerAuthRegisterEvent;
import dev.helix.suleiman.api.event.PlayerCaptchaFailEvent;
import dev.helix.suleiman.api.event.PlayerCaptchaRequestEvent;
import dev.helix.suleiman.api.event.PlayerCaptchaSuccessEvent;
import dev.helix.suleiman.api.model.AuthResult;
import dev.helix.suleiman.api.model.SessionSnapshot;
import dev.helix.suleiman.core.HLoginCore;
import dev.helix.suleiman.core.HLoginCoreBootstrap;
import dev.helix.suleiman.login.command.CommandAliasRegistry;
import dev.helix.suleiman.login.command.RootCommandExecutor;
import dev.helix.suleiman.login.compat.visual.VisualAdapterFactory;
import dev.helix.suleiman.login.listener.PlayerInventoryCaptchaListener;
import dev.helix.suleiman.login.listener.PlayerProtectionListener;
import dev.helix.suleiman.login.listener.PlayerSessionListener;
import dev.helix.suleiman.login.ui.BukkitVisualService;
import dev.helix.suleiman.login.util.BukkitCompat;
import dev.helix.suleiman.login.util.LocaleHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class HLoginPlugin extends JavaPlugin {
    private HLoginCore core;
    private BukkitVisualService visualService;
    private CommandAliasRegistry aliasRegistry;
    private RootCommandExecutor commandExecutor;
    private Boolean debugOverride;
    private final Map<UUID, Long> reminderMap = new HashMap<UUID, Long>();

    @Override
    public void onEnable() {
        try {
            installDefaults();
            core = HLoginCoreBootstrap.bootstrap(getDataFolder(), getLogger(), getDescription().getVersion());
            aliasRegistry = new CommandAliasRegistry(core);
            aliasRegistry.reload();
            visualService = new BukkitVisualService(core, VisualAdapterFactory.create(), this);
            commandExecutor = new RootCommandExecutor(this);
            getCommand("login").setExecutor(commandExecutor);
            getCommand("login").setTabCompleter(commandExecutor);
            getCommand("register").setExecutor(commandExecutor);
            getCommand("register").setTabCompleter(commandExecutor);
            getCommand("hlogin").setExecutor(commandExecutor);
            getCommand("hlogin").setTabCompleter(commandExecutor);
            Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerProtectionListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerInventoryCaptchaListener(this), this);
            Bukkit.getServicesManager().register(HLoginApi.class, core, this, ServicePriority.Normal);
            getLogger().info("Enabled hLogin v" + core.getVersion() + " with adapter " + visualService.adapter().family()
                + " on " + getServer().getVersion() + " using " + core.settings().storage().type());
            startReminderTask();
        } catch (Exception exception) {
            getLogger().severe("Unable to enable hLogin: " + exception.getMessage());
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (core != null) {
            for (Player player : BukkitCompat.onlinePlayers()) {
                visualService.clear(player);
            }
            core.shutdown();
        }
        Bukkit.getServicesManager().unregister(HLoginApi.class, core);
    }

    public HLoginCore getCore() {
        return core;
    }

    public BukkitVisualService getVisualService() {
        return visualService;
    }

    public CommandAliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }

    public RootCommandExecutor getRootCommandExecutor() {
        return commandExecutor;
    }

    public boolean isPlayerAuthenticated(Player player) {
        return core.sessions().isAuthenticated(player.getUniqueId());
    }

    public String locale(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SessionSnapshot snapshot = core.sessions().getSession(player.getUniqueId()).orElse(null);
            if (snapshot != null) {
                return snapshot.getLanguage();
            }
            return core.translations().resolveLocale(LocaleHelper.resolve(player, core.settings().general().defaultLanguage()));
        }
        return core.settings().general().defaultLanguage();
    }

    public void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(translate(sender, key, args));
    }

    public void sendList(CommandSender sender, String key, Object... args) {
        for (String line : core.translations().renderList(locale(sender), key, args)) {
            sender.sendMessage(line);
        }
    }

    public String translate(CommandSender sender, String key, Object... args) {
        return core.translations().render(locale(sender), key, args);
    }

    public boolean toggleDebugOverride() {
        debugOverride = debugOverride == null ? Boolean.valueOf(!core.settings().general().debug()) : Boolean.valueOf(!debugOverride.booleanValue());
        return debugOverride.booleanValue();
    }

    public boolean debugEnabled() {
        return debugOverride == null ? core.settings().general().debug() : debugOverride.booleanValue();
    }

    public void reloadPluginState() throws Exception {
        installDefaults();
        core.reload();
        aliasRegistry.reload();
        visualService = new BukkitVisualService(core, VisualAdapterFactory.create(), this);
        getLogger().info("Reloaded hLogin configuration. Adapter=" + visualService.adapter().family() + ", storage=" + core.settings().storage().type());
        reminderMap.clear();
        for (Player player : BukkitCompat.onlinePlayers()) {
            visualService.showForSession(player);
            touchReminder(player);
            applyProtectionState(player);
        }
    }

    public void handleAuthResult(Player player, AuthResult result, String action) {
        if (!player.isOnline()) {
            return;
        }
        sendMessage(player, result.getMessageKey(), result.getAttemptsRemaining());
        SessionSnapshot snapshot = core.sessions().getSession(player.getUniqueId()).orElse(null);
        if (result.getCaptchaChallenge() != null) {
            Bukkit.getPluginManager().callEvent(new PlayerCaptchaRequestEvent(player, snapshot, result.getCaptchaChallenge().getType()));
            visualService.showCaptcha(player, result.getCaptchaChallenge(), locale(player));
            touchReminder(player);
        } else {
            visualService.showForSession(player);
            if (snapshot != null && !snapshot.isLogged()) {
                touchReminder(player);
            } else {
                reminderMap.remove(player.getUniqueId());
            }
        }
        if (result.isSuccess()) {
            if ("register".equals(action)) {
                Bukkit.getPluginManager().callEvent(new PlayerAuthRegisterEvent(player, snapshot));
            }
            if ("captcha".equals(action)) {
                if (snapshot != null && snapshot.getCaptchaType() != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerCaptchaSuccessEvent(player, snapshot, snapshot.getCaptchaType()));
                }
                Bukkit.getScheduler().runTask(this, () -> {
                    player.closeInventory();
                    BukkitCompat.updateInventory(player);
                });
            }
            if (result.getResultingState() == dev.helix.suleiman.api.enums.AuthState.LOGGED) {
                Bukkit.getPluginManager().callEvent(new PlayerAuthLoginEvent(player, snapshot));
                Bukkit.getScheduler().runTask(this, () -> {
                    player.closeInventory();
                    BukkitCompat.updateInventory(player);
                });
            }
        } else {
            if ("captcha".equals(action) && snapshot != null && snapshot.getCaptchaType() != null) {
                Bukkit.getPluginManager().callEvent(new PlayerCaptchaFailEvent(player, snapshot, snapshot.getCaptchaType()));
            } else {
                Bukkit.getPluginManager().callEvent(new PlayerAuthFailEvent(player, snapshot, result.getMessageKey()));
            }
        }
        applyProtectionState(player);
    }

    public void applyProtectionState(Player player) {
        SessionSnapshot snapshot = core.sessions().getSession(player.getUniqueId()).orElse(null);
        boolean authenticated = snapshot != null && snapshot.isLogged();
        if (!authenticated && core.settings().general().safeLocation().enabled()) {
            Location location = new Location(
                Bukkit.getWorld(core.settings().general().safeLocation().world()),
                core.settings().general().safeLocation().x(),
                core.settings().general().safeLocation().y(),
                core.settings().general().safeLocation().z(),
                core.settings().general().safeLocation().yaw(),
                core.settings().general().safeLocation().pitch()
            );
            if (location.getWorld() != null) {
                player.teleport(location);
            }
        }
        if (core.settings().protection().hideUnauthenticated()) {
            for (Player other : BukkitCompat.onlinePlayers()) {
                if (other.equals(player)) {
                    continue;
                }
                if (authenticated) {
                    visualService.adapter().showPlayer(this, player, other);
                    visualService.adapter().showPlayer(this, other, player);
                } else {
                    visualService.adapter().hidePlayer(this, player, other);
                    visualService.adapter().hidePlayer(this, other, player);
                }
            }
        }
        if (core.settings().protection().invisibleUnauthenticated()) {
            if (authenticated) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }
        }
    }

    private void startReminderTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : BukkitCompat.onlinePlayers()) {
                SessionSnapshot snapshot = core.sessions().getSession(player.getUniqueId()).orElse(null);
                if (snapshot == null || snapshot.isLogged()) {
                    reminderMap.remove(player.getUniqueId());
                    continue;
                }
                if (snapshot.getExpiresAt() != null && snapshot.getExpiresAt().isBefore(Instant.now())) {
                    player.kickPlayer(core.translations().render(locale(player), "session.timeout-kick"));
                    continue;
                }
                long now = System.currentTimeMillis();
                long lastReminder = reminderMap.containsKey(player.getUniqueId()) ? reminderMap.get(player.getUniqueId()).longValue() : 0L;
                long intervalMillis = core.settings().general().reminderIntervalSeconds() * 1000L;
                if (now - lastReminder >= intervalMillis) {
                    visualService.showForSession(player);
                    touchReminder(player);
                }
            }
        }, 20L, 20L);
    }

    public void touchReminder(Player player) {
        reminderMap.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis()));
    }

    private void installDefaults() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveIfMissing("config.yml");
        saveIfMissing("api.yml");
        saveIfMissing("setup.yml");
        saveIfMissing("custom.yml");
        File translationFolder = new File(getDataFolder(), "translation");
        if (!translationFolder.exists()) {
            translationFolder.mkdirs();
        }
        String[] translations = {
            "messages_ar.yml", "messages_br.yml", "messages_cn.yml", "messages_cz.yml", "messages_de.yml",
            "messages_en.yml", "messages_es.yml", "messages_fr.yml", "messages_hr.yml", "messages_hu.yml",
            "messages_it.yml", "messages_lt.yml", "messages_pl.yml", "messages_ru.yml", "messages_sr.yml",
            "messages_tr.yml", "messages_pt_br.yml", "messages_en_us.yml", "messages_fr_fr.yml", "messages_ja_jp.yml"
        };
        for (String translation : translations) {
            String path = "translation/" + translation;
            saveIfMissing(path);
            repairCorruptedResource(path);
        }
    }

    private void saveIfMissing(String path) {
        File target = new File(getDataFolder(), path);
        if (!target.exists()) {
            saveResource(path, false);
        }
    }

    private void repairCorruptedResource(String path) {
        File target = new File(getDataFolder(), path);
        if (!target.exists()) {
            return;
        }
        try {
            String content = new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8);
            if (!looksCorrupted(content)) {
                return;
            }
            File backup = new File(target.getParentFile(), target.getName() + ".broken-" + System.currentTimeMillis() + ".bak");
            Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (InputStream inputStream = getResource(path)) {
                if (inputStream == null) {
                    return;
                }
                Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().warning("Replaced corrupted resource " + path + " and created backup " + backup.getName());
            }
        } catch (IOException exception) {
            getLogger().warning("Unable to repair resource " + path + ": " + exception.getMessage());
        }
    }

    private boolean looksCorrupted(String content) {
        return content != null
            && (content.indexOf('\uFFFD') >= 0
                || content.contains("Ã§")
                || content.contains("Ã£")
                || content.contains("Ã¡")
                || content.contains("Ã©")
                || content.contains("Ãª")
                || content.contains("Ã­")
                || content.contains("Ã³")
                || content.contains("Ãº")
                || content.contains("Â§")
                || content.contains("â€")
                || content.contains("â€™"));
    }
}
