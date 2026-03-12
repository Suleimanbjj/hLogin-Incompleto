/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.context;

import dev.helix.suleiman.core.config.PluginSettings;
import dev.helix.suleiman.core.security.PasswordHasherFactory;
import dev.helix.suleiman.core.session.SessionRegistry;
import dev.helix.suleiman.core.storage.StorageContext;
import dev.helix.suleiman.core.translation.TranslationRegistry;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class CoreContainer {
    private final File dataFolder;
    private final Logger logger;
    private final String pluginVersion;
    private final SessionRegistry sessionRegistry;
    private final ExecutorService executorService;
    private volatile PluginSettings settings;
    private volatile TranslationRegistry translationRegistry;
    private volatile PasswordHasherFactory passwordHasherFactory;
    private volatile StorageContext storageContext;

    public CoreContainer(File dataFolder, Logger logger, String pluginVersion) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.pluginVersion = pluginVersion;
        this.sessionRegistry = new SessionRegistry();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public File dataFolder() { return dataFolder; }
    public Logger logger() { return logger; }
    public String pluginVersion() { return pluginVersion; }
    public SessionRegistry sessionRegistry() { return sessionRegistry; }
    public ExecutorService executorService() { return executorService; }
    public PluginSettings settings() { return settings; }
    public void settings(PluginSettings settings) { this.settings = settings; }
    public TranslationRegistry translationRegistry() { return translationRegistry; }
    public void translationRegistry(TranslationRegistry translationRegistry) { this.translationRegistry = translationRegistry; }
    public PasswordHasherFactory passwordHasherFactory() { return passwordHasherFactory; }
    public void passwordHasherFactory(PasswordHasherFactory passwordHasherFactory) { this.passwordHasherFactory = passwordHasherFactory; }
    public StorageContext storageContext() { return storageContext; }
    public void storageContext(StorageContext storageContext) { this.storageContext = storageContext; }

    public void shutdown() {
        executorService.shutdownNow();
        if (storageContext != null) {
            storageContext.close();
        }
    }
}
