/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigNode {
    private final Map<String, Object> values;

    public ConfigNode(Map<String, Object> values) {
        this.values = values == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, Object>(values));
    }

    public Map<String, Object> raw() {
        return values;
    }

    public ConfigNode section(String path) {
        Object value = find(path);
        if (value instanceof Map) {
            return new ConfigNode(castMap(value));
        }
        return new ConfigNode(Collections.<String, Object>emptyMap());
    }

    public boolean contains(String path) {
        return find(path) != null;
    }

    public String string(String path, String fallback) {
        Object value = find(path);
        return value == null ? fallback : String.valueOf(value);
    }

    public boolean bool(String path, boolean fallback) {
        Object value = find(path);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }

    public int integer(String path, int fallback) {
        Object value = find(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public long longNumber(String path, long fallback) {
        Object value = find(path);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public double decimal(String path, double fallback) {
        Object value = find(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public List<String> stringList(String path) {
        Object value = find(path);
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> source = castList(value);
        List<String> result = new ArrayList<String>(source.size());
        for (Object item : source) {
            result.add(String.valueOf(item));
        }
        return Collections.unmodifiableList(result);
    }

    public Map<String, String> stringMap(String path) {
        Object value = find(path);
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> source = castMap(value);
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    public Object find(String path) {
        if (path == null || path.isEmpty()) {
            return values;
        }
        String[] parts = path.split("\\.");
        Object current = values;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = castMap(current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> castList(Object value) {
        return (List<Object>) value;
    }
}
