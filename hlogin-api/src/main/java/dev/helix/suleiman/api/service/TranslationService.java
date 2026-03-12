/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.service;

import java.util.List;
import java.util.Set;

public interface TranslationService {

    String render(String locale, String key, Object... arguments);

    List<String> renderList(String locale, String key, Object... arguments);

    String resolveLocale(String locale);

    Set<String> getAvailableLocales();
}
