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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class BukkitCompat {
    private BukkitCompat() {
    }

    public static List<Player> onlinePlayers() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object value = method.invoke(null);
            if (value instanceof Collection) {
                List<Player> players = new ArrayList<Player>();
                for (Object entry : (Collection<?>) value) {
                    if (entry instanceof Player) {
                        players.add((Player) entry);
                    }
                }
                return players;
            }
            if (value instanceof Player[]) {
                Player[] players = (Player[]) value;
                List<Player> result = new ArrayList<Player>(players.length);
                Collections.addAll(result, players);
                return result;
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    public static ItemStack createCaptchaItem(boolean correct, String displayName) {
        Material material = resolveMaterial(correct
            ? new String[] { "LIME_WOOL", "WOOL" }
            : new String[] { "RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "THIN_GLASS" });
        short durability = 0;
        if ("WOOL".equals(material.name())) {
            durability = (short) 5;
        } else if ("STAINED_GLASS_PANE".equals(material.name())) {
            durability = (short) 14;
        }
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createCaptchaHead(String animalId, String displayName) {
        Material material = resolveMaterial(new String[] { "PLAYER_HEAD", "SKULL_ITEM" });
        short durability = "SKULL_ITEM".equals(material.name()) ? (short) 3 : 0;
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (meta instanceof SkullMeta) {
                applyTexture((SkullMeta) meta, textureValue(animalId));
            } else {
                applyTexture(meta, textureValue(animalId));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void updateInventory(Player player) {
        if (player == null) {
            return;
        }
        try {
            Method method = Player.class.getMethod("updateInventory");
            method.invoke(player);
        } catch (Exception ignored) {
        }
    }

    public static Material resolveMaterial(String[] candidates) {
        for (String candidate : candidates) {
            Material material = Material.getMaterial(candidate);
            if (material != null) {
                return material;
            }
        }
        return Material.STONE;
    }

    private static void applyTexture(ItemMeta meta, String textureValue) {
        if (textureValue == null || textureValue.isEmpty()) {
            return;
        }
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Constructor<?> profileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
            Object profile = profileConstructor.newInstance(UUID.randomUUID(), "hlogin");
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class);
            Object property = propertyConstructor.newInstance("textures", textureValue);
            Method putMethod = findMethod(properties.getClass(), "put", 2);
            if (putMethod != null) {
                putMethod.setAccessible(true);
                putMethod.invoke(properties, "textures", property);
            }
            try {
                Method setProfile = meta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
                setProfile.setAccessible(true);
                setProfile.invoke(meta, profile);
                return;
            } catch (NoSuchMethodException ignored) {
            }
            Field profileField = findField(meta.getClass(), "profile");
            if (profileField == null) {
                return;
            }
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception ignored) {
        }
    }

    private static String textureValue(String animalId) {
        if (animalId == null) {
            return null;
        }
        String normalized = animalId.toLowerCase(Locale.ROOT);
        if ("penguin".equals(normalized)) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ4N2NlYjFmYTQxMzQyYTgzYTZlZDc1YTljMzE4MTA0YTA1YmY1M2I1YzNlMzE2ZjhhZmFhNmFjMjlhIn19fQ==";
        }
        if ("fox".equals(normalized)) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNhZGU2N2M3ZDNiZTBlZDJhMTQ4NzllZmFjNWUxNTNjZDg4NmRhYzJmNjNjODNhMzdmOWI2Y2QxYTFkIn19fQ==";
        }
        if ("elephant".equals(normalized)) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTU4YWRmZWRlODg2ZWQ2MzE5ZmQ5NzA4ZTc4N2M1OGE0YzdiN2IzYmViNzkzMjk1YWU3ZTFmOGI1MDFjIn19fQ==";
        }
        if ("zebra".equals(normalized)) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWM1MmU2ZWUzMjAyYzMwNmQzMTZhZTM2NWQ1MzkxNzE3ZDVmMTllMTRjNzRkYjVjYjk0Yjc2ZDZlNzA2MTIifX19";
        }
        if ("lion".equals(normalized)) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODAxMjUyMzkzN2IzYTZjNGE4ZjBlMGRmOTlkMjI1MDFiY2Y4MDA0ZTcyNTBlNzQxOTJhMTE2YzVlNjAxYWYxNSJ9fX0=";
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, int parameterCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterTypes().length == parameterCount) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
