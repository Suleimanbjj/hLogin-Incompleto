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

import dev.helix.suleiman.login.compat.nms.v1_13_r1_v1_16_r3.Modern113To116VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_17_r1_v1_20_r3.Modern117To1203VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_20_6.V1206VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_21_r1.V121VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_7_r4.Legacy17VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_8_r1_r3.Legacy18VisualAdapter;
import dev.helix.suleiman.login.compat.nms.v1_9_r1_v1_12_r1.Legacy19To112VisualAdapter;
import org.bukkit.Bukkit;

public final class VisualAdapterFactory {
    private VisualAdapterFactory() {
    }

    public static VisualAdapter create() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1).toLowerCase();
        if (version.startsWith("v1_7")) {
            return new Legacy17VisualAdapter();
        }
        if (version.startsWith("v1_8")) {
            return new Legacy18VisualAdapter();
        }
        if (version.startsWith("v1_9") || version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12")) {
            return new Legacy19To112VisualAdapter();
        }
        if (version.startsWith("v1_13") || version.startsWith("v1_14") || version.startsWith("v1_15") || version.startsWith("v1_16")) {
            return new Modern113To116VisualAdapter();
        }
        if (version.startsWith("v1_17") || version.startsWith("v1_18") || version.startsWith("v1_19") || version.startsWith("v1_20_r1")
            || version.startsWith("v1_20_r2") || version.startsWith("v1_20_r3")) {
            return new Modern117To1203VisualAdapter();
        }
        if (version.startsWith("v1_20_r4") || version.contains("1_20_6")) {
            return new V1206VisualAdapter();
        }
        if (version.startsWith("v1_21")) {
            return new V121VisualAdapter();
        }
        return new Modern117To1203VisualAdapter();
    }
}
