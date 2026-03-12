/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.command;

import dev.helix.suleiman.core.HLoginCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CommandAliasRegistry {
    private final HLoginCore core;
    private final Map<String, String> aliases = new LinkedHashMap<String, String>();
    private final Set<String> builtinAllowed = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList("login", "register", "hlogin")));

    public CommandAliasRegistry(HLoginCore core) {
        this.core = core;
    }

    public void reload() {
        aliases.clear();
        register("login", Arrays.asList("login"));
        register("register", Arrays.asList("register"));
        register("hlogin", Arrays.asList("hlogin"));
        register("login", core.settings().commands().globalLoginAliases());
        register("register", core.settings().commands().globalRegisterAliases());
        register("hlogin", core.settings().commands().globalAdminAliases());
        if (core.settings().commands().useTranslatedAliases()) {
            for (String locale : core.translations().getAvailableLocales()) {
                register("login", core.container().translationRegistry().commandAliases(locale, "login"));
                register("register", core.container().translationRegistry().commandAliases(locale, "register"));
                register("hlogin", core.container().translationRegistry().commandAliases(locale, "admin"));
            }
        }
    }

    public String resolve(String alias) {
        String normalized = normalize(alias);
        if (normalized == null) {
            return null;
        }
        return aliases.get(normalized);
    }

    public boolean isWhitelistedForUnauth(String alias) {
        String normalized = normalize(alias);
        if (normalized == null) {
            return false;
        }
        if (aliases.containsKey(normalized) || builtinAllowed.contains(normalized)) {
            return true;
        }
        for (String allowed : core.settings().protection().allowedCommands()) {
            if (normalized.equals(normalize(allowed))) {
                return true;
            }
        }
        return false;
    }

    private void register(String canonical, List<String> aliasesToRegister) {
        for (String alias : aliasesToRegister) {
            String normalized = normalize(alias);
            if (normalized != null) {
                aliases.put(normalized, canonical);
            }
        }
    }

    private String normalize(String alias) {
        if (alias == null) {
            return null;
        }
        String normalized = alias.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        int namespaceIndex = normalized.indexOf(':');
        if (namespaceIndex >= 0 && namespaceIndex + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceIndex + 1);
        }
        return normalized.isEmpty() ? null : normalized;
    }
}
