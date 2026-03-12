/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.util;

import java.util.Locale;

public final class StringNormalizer {
    private StringNormalizer() {
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String safeIp(String value) {
        return value == null ? "unknown" : value.trim();
    }
}
