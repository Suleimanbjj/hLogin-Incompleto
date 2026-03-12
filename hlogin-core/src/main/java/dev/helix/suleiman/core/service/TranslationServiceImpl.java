/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.service;

import dev.helix.suleiman.api.service.TranslationService;
import dev.helix.suleiman.core.context.CoreContainer;
import java.util.List;
import java.util.Set;

public final class TranslationServiceImpl implements TranslationService {
    private final CoreContainer container;

    public TranslationServiceImpl(CoreContainer container) {
        this.container = container;
    }

    @Override
    public String render(String locale, String key, Object... arguments) {
        return container.translationRegistry().render(locale, key, arguments);
    }

    @Override
    public List<String> renderList(String locale, String key, Object... arguments) {
        return container.translationRegistry().renderList(locale, key, arguments);
    }

    @Override
    public String resolveLocale(String locale) {
        return container.translationRegistry().resolveLocale(locale);
    }

    @Override
    public Set<String> getAvailableLocales() {
        return container.translationRegistry().locales();
    }
}
