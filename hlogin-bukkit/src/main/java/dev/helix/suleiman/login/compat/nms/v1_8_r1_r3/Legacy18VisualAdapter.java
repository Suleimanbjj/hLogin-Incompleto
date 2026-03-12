/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.compat.nms.v1_8_r1_r3;

import dev.helix.suleiman.login.compat.visual.AbstractVisualAdapter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Legacy18VisualAdapter extends AbstractVisualAdapter {
    @Override
    public String family() {
        return "v1_8_r1_r3";
    }

    @Override
    protected boolean supportsTitles() {
        return true;
    }

    @Override
    protected boolean supportsBossBar() {
        return false;
    }

    @Override
    protected boolean supportsHologram() {
        return true;
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Class<?> enumTitleAction = nmsClass("PacketPlayOutTitle$EnumTitleAction");
            Class<?> chatComponent = nmsClass("IChatBaseComponent");
            Class<?> packetTitle = nmsClass("PacketPlayOutTitle");

            Constructor<?> timingConstructor = packetTitle.getConstructor(int.class, int.class, int.class);
            sendPacket(player, timingConstructor.newInstance(Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut)));

            Constructor<?> textConstructor = packetTitle.getConstructor(enumTitleAction, chatComponent);
            sendPacket(player, textConstructor.newInstance(enumValue(enumTitleAction, "TITLE"), jsonComponent(title)));
            if (subtitle != null && !subtitle.isEmpty()) {
                sendPacket(player, textConstructor.newInstance(enumValue(enumTitleAction, "SUBTITLE"), jsonComponent(subtitle)));
            }
        } catch (Exception exception) {
            super.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        try {
            Constructor<?> constructor = nmsClass("PacketPlayOutChat")
                .getConstructor(nmsClass("IChatBaseComponent"), byte.class);
            sendPacket(player, constructor.newInstance(jsonComponent(message), Byte.valueOf((byte) 2)));
        } catch (Exception exception) {
            super.sendActionBar(player, message);
        }
    }

    private void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Field connectionField = handle.getClass().getField("playerConnection");
        Object connection = connectionField.get(handle);
        Method sendPacket = connection.getClass().getMethod("sendPacket", nmsClass("Packet"));
        sendPacket.invoke(connection, packet);
    }

    private Object jsonComponent(String message) throws Exception {
        Class<?> serializer = nmsClass("IChatBaseComponent$ChatSerializer");
        Method parse = serializer.getMethod("a", String.class);
        return parse.invoke(null, "{\"text\":\"" + escapeJson(message) + "\"}");
    }

    @SuppressWarnings("unchecked")
    private Object enumValue(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<Enum>) enumClass.asSubclass(Enum.class), name);
    }

    private Class<?> nmsClass(String simpleName) throws ClassNotFoundException {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        return Class.forName("net.minecraft.server." + version + "." + simpleName);
    }

    private String escapeJson(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
