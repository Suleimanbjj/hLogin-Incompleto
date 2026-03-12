/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.translation;

import dev.helix.suleiman.core.config.ConfigNode;
import dev.helix.suleiman.core.config.ConfigurationLoader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TranslationRegistry {
    private final String fallbackLocale;
    private final Map<String, Map<String, Object>> bundles = new LinkedHashMap<String, Map<String, Object>>();
    private final Map<String, String> aliases = new LinkedHashMap<String, String>();
    private final ConfigurationLoader loader = new ConfigurationLoader();

    public TranslationRegistry(String fallbackLocale) {
        this.fallbackLocale = fallbackLocale == null ? "en" : fallbackLocale.toLowerCase(Locale.ROOT);
    }

    public void loadDirectory(File directory) throws IOException {
        bundles.clear();
        aliases.clear();
        if (directory == null || !directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.isFile() || !file.getName().startsWith("messages_") || !file.getName().endsWith(".yml")) {
                continue;
            }
            ConfigNode node = loader.load(file);
            String code = node.string("language.code", file.getName().replace("messages_", "").replace(".yml", ""));
            Map<String, Object> flattened = new LinkedHashMap<String, Object>();
            flatten("", node.raw(), flattened);
            bundles.put(code.toLowerCase(Locale.ROOT), flattened);
            aliases.put(code.toLowerCase(Locale.ROOT), code.toLowerCase(Locale.ROOT));
            for (String alias : node.stringList("language.aliases")) {
                aliases.put(alias.toLowerCase(Locale.ROOT), code.toLowerCase(Locale.ROOT));
            }
        }
    }

    public String resolveLocale(String requested) {
        if (requested == null || requested.trim().isEmpty()) {
            return fallbackLocale;
        }
        String normalized = requested.toLowerCase(Locale.ROOT);
        String resolved = aliases.get(normalized);
        return resolved == null ? fallbackLocale : resolved;
    }

    public String render(String locale, String key, Object... arguments) {
        String resolvedLocale = resolveLocale(locale);
        Object value = findValue(resolvedLocale, key);
        if (value == null) {
            value = findValue(fallbackLocale, key);
        }
        String text = value == null ? key : String.valueOf(value);
        if (!"general.prefix".equals(key)) {
            text = text.replace("%prefix%", render(resolvedLocale, "general.prefix"));
        }
        return colorize(MessageFormat.format(text, arguments == null ? new Object[0] : arguments));
    }

    public List<String> renderList(String locale, String key, Object... arguments) {
        String resolvedLocale = resolveLocale(locale);
        Object value = findValue(resolvedLocale, key);
        if (!(value instanceof Collection)) {
            value = findValue(fallbackLocale, key);
        }
        if (!(value instanceof Collection)) {
            return Collections.singletonList(render(locale, key, arguments));
        }
        List<String> result = new ArrayList<String>();
        for (Object item : (Collection<?>) value) {
            result.add(colorize(MessageFormat.format(String.valueOf(item), arguments == null ? new Object[0] : arguments)));
        }
        return Collections.unmodifiableList(result);
    }

    public Set<String> locales() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(bundles.keySet()));
    }

    public List<String> commandAliases(String locale, String commandKey) {
        Object value = findValue(resolveLocale(locale), "commands." + commandKey + ".aliases");
        if (!(value instanceof Collection)) {
            value = findValue(fallbackLocale, "commands." + commandKey + ".aliases");
        }
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (Object item : (Collection<?>) value) {
            result.add(String.valueOf(item).toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableList(result);
    }

    private Object findValue(String locale, String key) {
        Map<String, Object> bundle = bundles.get(locale);
        return bundle == null ? null : bundle.get(key);
    }

    private void flatten(String prefix, Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(key, castMap(value), target);
            } else {
                target.put(key, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private String colorize(String text) {
        return text == null ? "" : text.replace('&', '\u00A7');
    }
}
