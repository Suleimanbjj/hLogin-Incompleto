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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class AbstractVisualAdapter implements VisualAdapter {
    private final Map<UUID, Object> bossBars = new HashMap<UUID, Object>();
    private final Map<UUID, List<Entity>> holograms = new HashMap<UUID, List<Entity>>();

    protected abstract boolean supportsTitles();

    protected boolean supportsActionBar() {
        return true;
    }

    protected abstract boolean supportsBossBar();

    protected abstract boolean supportsHologram();

    @Override
    public final boolean supportsTitleChannel() {
        return supportsTitles();
    }

    @Override
    public final boolean supportsActionBarChannel() {
        return supportsActionBar();
    }

    @Override
    public final boolean supportsBossBarChannel() {
        return supportsBossBar();
    }

    @Override
    public final boolean supportsHologramChannel() {
        return supportsHologram();
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (!supportsTitles()) {
            player.sendMessage(title);
            if (subtitle != null && !subtitle.isEmpty()) {
                player.sendMessage(subtitle);
            }
            return;
        }
        try {
            Method method = player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            method.invoke(player, title, subtitle, Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut));
        } catch (Exception ex) {
            try {
                Method method = player.getClass().getMethod("sendTitle", String.class, String.class);
                method.invoke(player, title, subtitle);
            } catch (Exception ignored) {
                player.sendMessage(title);
                if (subtitle != null && !subtitle.isEmpty()) {
                    player.sendMessage(subtitle);
                }
            }
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        if (!supportsActionBar()) {
            player.sendMessage(message);
            return;
        }
        try {
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Class<?> chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> baseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object actionBarType = Enum.valueOf((Class<Enum>) chatMessageType.asSubclass(Enum.class), "ACTION_BAR");
            Object component = textComponent.getConstructor(String.class).newInstance(message);
            Object components = java.lang.reflect.Array.newInstance(baseComponent, 1);
            java.lang.reflect.Array.set(components, 0, component);
            spigot.getClass().getMethod("sendMessage", chatMessageType, components.getClass()).invoke(spigot, actionBarType, components);
        } catch (Exception ex) {
            player.sendMessage(message);
        }
    }

    @Override
    public void showBossBar(Player player, String message, float progress) {
        if (!supportsBossBar()) {
            sendActionBar(player, message);
            return;
        }
        try {
            Object bossBar = bossBars.get(player.getUniqueId());
            if (bossBar == null) {
                Class<?> barColor = Class.forName("org.bukkit.boss.BarColor");
                Class<?> barStyle = Class.forName("org.bukkit.boss.BarStyle");
                Method createBossBar = Bukkit.class.getMethod("createBossBar", String.class, barColor, barStyle);
                Object color = Enum.valueOf((Class<Enum>) barColor.asSubclass(Enum.class), "YELLOW");
                Object style = Enum.valueOf((Class<Enum>) barStyle.asSubclass(Enum.class), "SOLID");
                bossBar = createBossBar.invoke(null, message, color, style);
                bossBar.getClass().getMethod("addPlayer", Player.class).invoke(bossBar, player);
                bossBars.put(player.getUniqueId(), bossBar);
            }
            bossBar.getClass().getMethod("setTitle", String.class).invoke(bossBar, message);
            bossBar.getClass().getMethod("setProgress", double.class).invoke(bossBar, Double.valueOf(Math.max(0.0D, Math.min(1.0D, progress))));
            bossBar.getClass().getMethod("setVisible", boolean.class).invoke(bossBar, Boolean.TRUE);
        } catch (Exception ex) {
            sendActionBar(player, message);
        }
    }

    @Override
    public void clearBossBar(Player player) {
        Object bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar == null) {
            return;
        }
        try {
            bossBar.getClass().getMethod("removeAll").invoke(bossBar);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void showHologram(Player player, List<String> lines, Location location) {
        clearHologram(player);
        if (!supportsHologram()) {
            for (String line : lines) {
                player.sendMessage(line);
            }
            return;
        }
        try {
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            Class<?> entityTypeClass = Class.forName("org.bukkit.entity.EntityType");
            Object armorStandType = Enum.valueOf((Class<Enum>) entityTypeClass.asSubclass(Enum.class), "ARMOR_STAND");
            Method spawnEntity = world.getClass().getMethod("spawnEntity", Location.class, entityTypeClass);
            List<Entity> spawned = new ArrayList<Entity>();
            double offset = 0.0D;
            for (String line : lines) {
                Location target = location.clone().add(0.0D, offset, 0.0D);
                Entity armorStand = (Entity) spawnEntity.invoke(world, target, armorStandType);
                applyArmorStand(line, armorStand);
                spawned.add(armorStand);
                offset -= 0.28D;
            }
            holograms.put(player.getUniqueId(), spawned);
        } catch (Exception ex) {
            for (String line : lines) {
                player.sendMessage(line);
            }
        }
    }

    @Override
    public void clearHologram(Player player) {
        List<Entity> entities = holograms.remove(player.getUniqueId());
        if (entities == null) {
            return;
        }
        for (Entity entity : entities) {
            entity.remove();
        }
    }

    private void applyArmorStand(String line, Entity armorStand) throws Exception {
        Method setVisible = armorStand.getClass().getMethod("setVisible", boolean.class);
        setVisible.invoke(armorStand, Boolean.FALSE);
        armorStand.getClass().getMethod("setCustomName", String.class).invoke(armorStand, line);
        armorStand.getClass().getMethod("setCustomNameVisible", boolean.class).invoke(armorStand, Boolean.TRUE);
        try {
            armorStand.getClass().getMethod("setSmall", boolean.class).invoke(armorStand, Boolean.TRUE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            armorStand.getClass().getMethod("setBasePlate", boolean.class).invoke(armorStand, Boolean.FALSE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            armorStand.getClass().getMethod("setGravity", boolean.class).invoke(armorStand, Boolean.FALSE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            armorStand.getClass().getMethod("setMarker", boolean.class).invoke(armorStand, Boolean.TRUE);
        } catch (NoSuchMethodException ignored) {
        }
    }

    @Override
    public void hidePlayer(Plugin plugin, Player viewer, Player target) {
        try {
            viewer.getClass().getMethod("hidePlayer", Plugin.class, Player.class).invoke(viewer, plugin, target);
        } catch (Exception modernFailure) {
            try {
                viewer.getClass().getMethod("hidePlayer", Player.class).invoke(viewer, target);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void showPlayer(Plugin plugin, Player viewer, Player target) {
        try {
            viewer.getClass().getMethod("showPlayer", Plugin.class, Player.class).invoke(viewer, plugin, target);
        } catch (Exception modernFailure) {
            try {
                viewer.getClass().getMethod("showPlayer", Player.class).invoke(viewer, target);
            } catch (Exception ignored) {
            }
        }
    }
}
