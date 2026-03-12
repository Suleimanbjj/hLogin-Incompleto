/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core;

import com.zaxxer.hikari.HikariDataSource;
import dev.helix.suleiman.core.config.ConfigNode;
import dev.helix.suleiman.core.config.ConfigurationLoader;
import dev.helix.suleiman.core.config.PluginSettings;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.security.PasswordHasherFactory;
import dev.helix.suleiman.core.service.AccountServiceImpl;
import dev.helix.suleiman.core.service.AuthenticationServiceImpl;
import dev.helix.suleiman.core.service.CaptchaServiceImpl;
import dev.helix.suleiman.core.service.PlaceholderServiceImpl;
import dev.helix.suleiman.core.service.SessionCoordinator;
import dev.helix.suleiman.core.service.SessionServiceImpl;
import dev.helix.suleiman.core.service.TranslationServiceImpl;
import dev.helix.suleiman.core.service.WebhookServiceImpl;
import dev.helix.suleiman.core.storage.DataSourceFactory;
import dev.helix.suleiman.core.storage.SchemaMigrator;
import dev.helix.suleiman.core.storage.StorageContext;
import dev.helix.suleiman.core.storage.StorageType;
import dev.helix.suleiman.core.storage.repository.jdbc.JdbcAccountRepository;
import dev.helix.suleiman.core.storage.repository.jdbc.JdbcAuditRepository;
import dev.helix.suleiman.core.translation.TranslationRegistry;
import java.io.File;
import java.util.logging.Logger;

public final class HLoginCoreBootstrap {
    private HLoginCoreBootstrap() {
    }

    public static HLoginCore bootstrap(File dataFolder, Logger logger, String pluginVersion) throws Exception {
        CoreContainer container = new CoreContainer(dataFolder, logger, pluginVersion);
        CaptchaServiceImpl captchaService = new CaptchaServiceImpl(container);
        WebhookServiceImpl webhookService = new WebhookServiceImpl(container);
        SessionCoordinator sessionCoordinator = new SessionCoordinator(container, captchaService);
        HLoginCore core = new HLoginCore(
            container,
            new AuthenticationServiceImpl(container, captchaService, webhookService, sessionCoordinator),
            new AccountServiceImpl(container),
            new SessionServiceImpl(container),
            new TranslationServiceImpl(container),
            captchaService,
            webhookService,
            new PlaceholderServiceImpl(),
            sessionCoordinator
        );
        reloadInto(core);
        return core;
    }

    static void reloadInto(HLoginCore core) throws Exception {
        CoreContainer container = core.container();
        if (!container.dataFolder().exists()) {
            container.dataFolder().mkdirs();
        }
        ConfigurationLoader loader = new ConfigurationLoader();
        ConfigNode config = loader.load(new File(container.dataFolder(), "config.yml"));
        ConfigNode custom = loader.load(new File(container.dataFolder(), "custom.yml"));
        ConfigNode api = loader.load(new File(container.dataFolder(), "api.yml"));
        ConfigNode setup = loader.load(new File(container.dataFolder(), "setup.yml"));
        ConfigNode mergedConfig = loader.merge(config, custom);
        PluginSettings settings = PluginSettings.from(container.dataFolder(), mergedConfig, api, setup);

        TranslationRegistry translationRegistry = new TranslationRegistry(settings.general().defaultLanguage());
        translationRegistry.loadDirectory(new File(container.dataFolder(), "translation"));

        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        HikariDataSource dataSource = dataSourceFactory.create(settings.storage());
        StorageType storageType = StorageType.parse(settings.storage().type());
        if (storageType == StorageType.MYSQL ? settings.storage().mysql().autoMigrate() : true) {
            new SchemaMigrator().migrate(dataSource, storageType, settings.storage());
        }
        StorageContext previousStorage = container.storageContext();
        StorageContext storageContext = new StorageContext(
            storageType,
            dataSource,
            new JdbcAccountRepository(dataSource, settings.storage().mysql().tablePrefix()),
            new JdbcAuditRepository(dataSource, settings.storage().mysql().tablePrefix()),
            dataSource
        );

        container.settings(settings);
        container.translationRegistry(translationRegistry);
        container.passwordHasherFactory(new PasswordHasherFactory(settings.security()));
        container.storageContext(storageContext);
        if (previousStorage != null) {
            previousStorage.close();
        }
    }
}
