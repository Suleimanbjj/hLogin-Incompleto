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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map) {
            StringBuilder builder = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> iterator = ((Map<?, ?>) value).entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                builder.append(toJson(String.valueOf(entry.getKey()))).append(':').append(toJson(entry.getValue()));
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            return builder.append('}').toString();
        }
        if (value instanceof Collection) {
            StringBuilder builder = new StringBuilder("[");
            Iterator<?> iterator = ((Collection<?>) value).iterator();
            while (iterator.hasNext()) {
                builder.append(toJson(iterator.next()));
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            return builder.append(']').toString();
        }
        return '"' + escape(String.valueOf(value)) + '"';
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
