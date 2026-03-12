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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public final class ConfigurationLoader {
    private final Yaml yaml = new Yaml();

    public ConfigNode load(File file) throws IOException {
        if (file == null || !file.exists()) {
            return new ConfigNode(Collections.<String, Object>emptyMap());
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        int offset = hasUtf8Bom(bytes) ? 3 : 0;
        String content = new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8);
        Object loaded = yaml.load(content);
        if (loaded instanceof Map) {
            return new ConfigNode(castMap(loaded));
        }
        return new ConfigNode(Collections.<String, Object>emptyMap());
    }

    public ConfigNode merge(ConfigNode base, ConfigNode override) {
        Map<String, Object> merged = deepCopy(base.raw());
        deepMerge(merged, override.raw());
        return new ConfigNode(merged);
    }

    private void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object existing = target.get(entry.getKey());
            Object incoming = entry.getValue();
            if (existing instanceof Map && incoming instanceof Map) {
                deepMerge(castMap(existing), castMap(incoming));
            } else {
                target.put(entry.getKey(), incoming);
            }
        }
    }

    private Map<String, Object> deepCopy(Map<String, Object> source) {
        Map<String, Object> copy = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy(castMap(value)));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private static boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3
            && (bytes[0] & 0xFF) == 0xEF
            && (bytes[1] & 0xFF) == 0xBB
            && (bytes[2] & 0xFF) == 0xBF;
    }
}
