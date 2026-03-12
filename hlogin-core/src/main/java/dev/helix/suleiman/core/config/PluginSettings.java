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

import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.enums.HashAlgorithm;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PluginSettings {
    private final General general;
    private final PasswordPolicy passwordPolicy;
    private final Security security;
    private final Captcha captcha;
    private final Webhook webhook;
    private final Storage storage;
    private final Session session;
    private final Protection protection;
    private final Commands commands;
    private final Visual visual;
    private final Premium premium;

    public PluginSettings(General general, PasswordPolicy passwordPolicy, Security security, Captcha captcha,
                          Webhook webhook, Storage storage, Session session, Protection protection,
                          Commands commands, Visual visual, Premium premium) {
        this.general = general;
        this.passwordPolicy = passwordPolicy;
        this.security = security;
        this.captcha = captcha;
        this.webhook = webhook;
        this.storage = storage;
        this.session = session;
        this.protection = protection;
        this.commands = commands;
        this.visual = visual;
        this.premium = premium;
    }

    public General general() { return general; }
    public PasswordPolicy passwordPolicy() { return passwordPolicy; }
    public Security security() { return security; }
    public Captcha captcha() { return captcha; }
    public Webhook webhook() { return webhook; }
    public Storage storage() { return storage; }
    public Session session() { return session; }
    public Protection protection() { return protection; }
    public Commands commands() { return commands; }
    public Visual visual() { return visual; }
    public Premium premium() { return premium; }

    public static PluginSettings from(File dataFolder, ConfigNode config, ConfigNode api, ConfigNode setup) {
        General general = new General(
            config.string("general.server-name", "default"),
            setup.string("language.default", setup.string("storage.default-language", "en")),
            config.bool("general.debug", false),
            config.bool("auth.auto-login-after-register", true),
            config.integer("auth.timeout-seconds", 60),
            config.integer("ux.reminder-interval-seconds", 5),
            config.bool("general.audit-enabled", true),
            new SafeLocation(
                config.bool("protection.safe-teleport.enabled", false),
                config.string("protection.safe-teleport.world", "world"),
                config.decimal("protection.safe-teleport.x", 0.0D),
                config.decimal("protection.safe-teleport.y", 64.0D),
                config.decimal("protection.safe-teleport.z", 0.0D),
                (float) config.decimal("protection.safe-teleport.yaw", 0.0D),
                (float) config.decimal("protection.safe-teleport.pitch", 0.0D)
            )
        );

        PasswordPolicy passwordPolicy = new PasswordPolicy(
            config.integer("security.password.min-length", 6),
            config.integer("security.password.max-length", 32),
            config.bool("security.password.require-confirmation", true),
            config.bool("security.password.require-uppercase", false),
            config.bool("security.password.require-lowercase", true),
            config.bool("security.password.require-number", true),
            config.bool("security.password.require-special", false),
            config.stringList("security.password.blocked-patterns")
        );

        Security security = new Security(
            parseHashAlgorithm(config.string("security.hash.algorithm", "BCRYPT")),
            config.integer("security.hash.bcrypt-cost", 12),
            config.integer("security.hash.pbkdf2-iterations", 200000),
            config.integer("security.hash.argon2-memory-kib", 65536),
            config.integer("security.hash.argon2-iterations", 3),
            config.integer("security.hash.argon2-parallelism", 2),
            config.integer("security.max-attempts", 3),
            config.integer("security.block-duration-seconds", 300),
            config.longNumber("security.command-cooldown-millis", 1000L),
            config.bool("security.ip.whitelist-enabled", false),
            config.bool("security.ip.blacklist-enabled", false),
            config.stringList("security.ip.whitelist"),
            config.stringList("security.ip.blacklist"),
            config.bool("security.mask-passwords-in-logs", true)
        );

        Captcha captcha = new Captcha(
            config.bool("captcha.enabled", false),
            parseCaptchaType(config.string("captcha.default-type", "TEXT")),
            config.bool("captcha.require-on-register", false),
            config.bool("captcha.require-on-login", false),
            config.integer("captcha.after-failed-attempts", 2),
            config.bool("captcha.only-suspicious-ip", false),
            config.integer("captcha.timeout-seconds", 60),
            config.integer("captcha.max-attempts", 3),
            config.string("captcha.failure-action", "KICK"),
            config.bool("captcha.bypass-permission", true)
        );

        Webhook webhook = new Webhook(
            api.bool("webhook.enabled", false),
            api.string("webhook.url", ""),
            api.string("webhook.method", "POST"),
            api.string("webhook.content-type", "application/json"),
            api.integer("webhook.timeout-millis", 3000),
            api.integer("webhook.retries", 2),
            api.stringMap("webhook.headers"),
            api.stringMap("webhook.payload-templates")
        );

        String storageType = setup.string("storage.type", "MYSQL").toUpperCase();
        Storage storage = new Storage(
            storageType,
            new Mysql(
                setup.string("mysql.host", "127.0.0.1"),
                setup.integer("mysql.port", 3306),
                setup.string("mysql.database", "hlogin"),
                setup.string("mysql.username", "root"),
                setup.string("mysql.password", ""),
                setup.bool("mysql.ssl", false),
                setup.integer("mysql.pool-size", 10),
                setup.longNumber("mysql.connection-timeout", 5000L),
                setup.string("mysql.table-prefix", "hlogin_"),
                setup.bool("mysql.auto-create-tables", true),
                setup.bool("mysql.auto-migrate", true),
                setup.string("mysql.charset", "utf8mb4")
            ),
            new Sqlite(new File(dataFolder, setup.string("sqlite.file", "storage/hlogin.db")))
        );

        Session session = new Session(
            config.bool("session.remember-session", true),
            config.bool("session.remember-ip", true),
            config.bool("session.remember-device", false),
            config.integer("session.remember-duration-hours", 72),
            config.bool("session.require-reauth-on-reconnect", false),
            config.integer("session.expire-minutes", 60)
        );

        Protection protection = new Protection(
            config.bool("protection.block-movement", true),
            config.bool("protection.block-damage", true),
            config.bool("protection.block-chat", true),
            config.bool("protection.block-commands", true),
            config.bool("protection.block-item-drop", true),
            config.bool("protection.block-item-pickup", true),
            config.bool("protection.block-inventory-click", true),
            config.bool("protection.block-interaction", true),
            config.bool("protection.hide-unauthenticated", false),
            config.bool("protection.invisible-unauthenticated", false),
            config.stringList("protection.allowed-commands")
        );

        Commands commands = new Commands(
            config.bool("commands.use-translated-aliases", true),
            config.stringList("commands.login.global-aliases"),
            config.stringList("commands.register.global-aliases"),
            config.stringList("commands.admin.global-aliases")
        );

        Visual visual = new Visual(
            config.bool("visual.title.enabled", true),
            config.bool("visual.actionbar.enabled", true),
            config.bool("visual.bossbar.enabled", true),
            config.bool("visual.hologram.enabled", false),
            config.bool("visual.hologram.fallback-chat", true),
            config.bool("visual.countdown.enabled", true),
            config.bool("visual.inventory-captcha.enabled", true)
        );

        Premium premium = new Premium(
            config.bool("premium.auto-detect.enabled", false),
            config.integer("premium.auto-detect.timeout-millis", 2500),
            config.string("premium.auto-detect.endpoint", "https://api.mojang.com/users/profiles/minecraft/%player%")
        );

        return new PluginSettings(general, passwordPolicy, security, captcha, webhook, storage, session, protection, commands, visual, premium);
    }

    private static CaptchaType parseCaptchaType(String value) {
        try {
            return CaptchaType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CaptchaType.TEXT;
        }
    }

    private static HashAlgorithm parseHashAlgorithm(String value) {
        try {
            return HashAlgorithm.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return HashAlgorithm.BCRYPT;
        }
    }

    public static final class General {
        private final String serverName;
        private final String defaultLanguage;
        private final boolean debug;
        private final boolean autoLoginAfterRegister;
        private final int authTimeoutSeconds;
        private final int reminderIntervalSeconds;
        private final boolean auditEnabled;
        private final SafeLocation safeLocation;

        public General(String serverName, String defaultLanguage, boolean debug, boolean autoLoginAfterRegister,
                       int authTimeoutSeconds, int reminderIntervalSeconds, boolean auditEnabled, SafeLocation safeLocation) {
            this.serverName = serverName;
            this.defaultLanguage = defaultLanguage;
            this.debug = debug;
            this.autoLoginAfterRegister = autoLoginAfterRegister;
            this.authTimeoutSeconds = authTimeoutSeconds;
            this.reminderIntervalSeconds = reminderIntervalSeconds;
            this.auditEnabled = auditEnabled;
            this.safeLocation = safeLocation;
        }

        public String serverName() { return serverName; }
        public String defaultLanguage() { return defaultLanguage; }
        public boolean debug() { return debug; }
        public boolean autoLoginAfterRegister() { return autoLoginAfterRegister; }
        public int authTimeoutSeconds() { return authTimeoutSeconds; }
        public int reminderIntervalSeconds() { return reminderIntervalSeconds; }
        public boolean auditEnabled() { return auditEnabled; }
        public SafeLocation safeLocation() { return safeLocation; }
    }

    public static final class SafeLocation {
        private final boolean enabled;
        private final String world;
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;

        public SafeLocation(boolean enabled, String world, double x, double y, double z, float yaw, float pitch) {
            this.enabled = enabled;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public boolean enabled() { return enabled; }
        public String world() { return world; }
        public double x() { return x; }
        public double y() { return y; }
        public double z() { return z; }
        public float yaw() { return yaw; }
        public float pitch() { return pitch; }
    }

    public static final class PasswordPolicy {
        private final int minLength;
        private final int maxLength;
        private final boolean requireConfirmation;
        private final boolean requireUppercase;
        private final boolean requireLowercase;
        private final boolean requireNumber;
        private final boolean requireSpecial;
        private final List<String> blockedPatterns;

        public PasswordPolicy(int minLength, int maxLength, boolean requireConfirmation, boolean requireUppercase,
                              boolean requireLowercase, boolean requireNumber, boolean requireSpecial, List<String> blockedPatterns) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.requireConfirmation = requireConfirmation;
            this.requireUppercase = requireUppercase;
            this.requireLowercase = requireLowercase;
            this.requireNumber = requireNumber;
            this.requireSpecial = requireSpecial;
            this.blockedPatterns = Collections.unmodifiableList(blockedPatterns);
        }

        public int minLength() { return minLength; }
        public int maxLength() { return maxLength; }
        public boolean requireConfirmation() { return requireConfirmation; }
        public boolean requireUppercase() { return requireUppercase; }
        public boolean requireLowercase() { return requireLowercase; }
        public boolean requireNumber() { return requireNumber; }
        public boolean requireSpecial() { return requireSpecial; }
        public List<String> blockedPatterns() { return blockedPatterns; }
    }

    public static final class Security {
        private final HashAlgorithm algorithm;
        private final int bcryptCost;
        private final int pbkdf2Iterations;
        private final int argon2MemoryKiB;
        private final int argon2Iterations;
        private final int argon2Parallelism;
        private final int maxAttempts;
        private final int blockDurationSeconds;
        private final long commandCooldownMillis;
        private final boolean ipWhitelistEnabled;
        private final boolean ipBlacklistEnabled;
        private final List<String> ipWhitelist;
        private final List<String> ipBlacklist;
        private final boolean maskPasswordsInLogs;

        public Security(HashAlgorithm algorithm, int bcryptCost, int pbkdf2Iterations, int argon2MemoryKiB,
                        int argon2Iterations, int argon2Parallelism, int maxAttempts, int blockDurationSeconds,
                        long commandCooldownMillis, boolean ipWhitelistEnabled, boolean ipBlacklistEnabled,
                        List<String> ipWhitelist, List<String> ipBlacklist, boolean maskPasswordsInLogs) {
            this.algorithm = algorithm;
            this.bcryptCost = bcryptCost;
            this.pbkdf2Iterations = pbkdf2Iterations;
            this.argon2MemoryKiB = argon2MemoryKiB;
            this.argon2Iterations = argon2Iterations;
            this.argon2Parallelism = argon2Parallelism;
            this.maxAttempts = maxAttempts;
            this.blockDurationSeconds = blockDurationSeconds;
            this.commandCooldownMillis = commandCooldownMillis;
            this.ipWhitelistEnabled = ipWhitelistEnabled;
            this.ipBlacklistEnabled = ipBlacklistEnabled;
            this.ipWhitelist = Collections.unmodifiableList(ipWhitelist);
            this.ipBlacklist = Collections.unmodifiableList(ipBlacklist);
            this.maskPasswordsInLogs = maskPasswordsInLogs;
        }

        public HashAlgorithm algorithm() { return algorithm; }
        public int bcryptCost() { return bcryptCost; }
        public int pbkdf2Iterations() { return pbkdf2Iterations; }
        public int argon2MemoryKiB() { return argon2MemoryKiB; }
        public int argon2Iterations() { return argon2Iterations; }
        public int argon2Parallelism() { return argon2Parallelism; }
        public int maxAttempts() { return maxAttempts; }
        public int blockDurationSeconds() { return blockDurationSeconds; }
        public long commandCooldownMillis() { return commandCooldownMillis; }
        public boolean ipWhitelistEnabled() { return ipWhitelistEnabled; }
        public boolean ipBlacklistEnabled() { return ipBlacklistEnabled; }
        public List<String> ipWhitelist() { return ipWhitelist; }
        public List<String> ipBlacklist() { return ipBlacklist; }
        public boolean maskPasswordsInLogs() { return maskPasswordsInLogs; }
    }

    public static final class Captcha {
        private final boolean enabled;
        private final CaptchaType defaultType;
        private final boolean requireOnRegister;
        private final boolean requireOnLogin;
        private final int afterFailedAttempts;
        private final boolean suspiciousIpsOnly;
        private final int timeoutSeconds;
        private final int maxAttempts;
        private final String failureAction;
        private final boolean bypassPermission;

        public Captcha(boolean enabled, CaptchaType defaultType, boolean requireOnRegister, boolean requireOnLogin,
                       int afterFailedAttempts, boolean suspiciousIpsOnly, int timeoutSeconds, int maxAttempts,
                       String failureAction, boolean bypassPermission) {
            this.enabled = enabled;
            this.defaultType = defaultType;
            this.requireOnRegister = requireOnRegister;
            this.requireOnLogin = requireOnLogin;
            this.afterFailedAttempts = afterFailedAttempts;
            this.suspiciousIpsOnly = suspiciousIpsOnly;
            this.timeoutSeconds = timeoutSeconds;
            this.maxAttempts = maxAttempts;
            this.failureAction = failureAction;
            this.bypassPermission = bypassPermission;
        }

        public boolean enabled() { return enabled; }
        public CaptchaType defaultType() { return defaultType; }
        public boolean requireOnRegister() { return requireOnRegister; }
        public boolean requireOnLogin() { return requireOnLogin; }
        public int afterFailedAttempts() { return afterFailedAttempts; }
        public boolean suspiciousIpsOnly() { return suspiciousIpsOnly; }
        public int timeoutSeconds() { return timeoutSeconds; }
        public int maxAttempts() { return maxAttempts; }
        public String failureAction() { return failureAction; }
        public boolean bypassPermission() { return bypassPermission; }
    }

    public static final class Webhook {
        private final boolean enabled;
        private final String url;
        private final String method;
        private final String contentType;
        private final int timeoutMillis;
        private final int retries;
        private final Map<String, String> headers;
        private final Map<String, String> payloadTemplates;

        public Webhook(boolean enabled, String url, String method, String contentType, int timeoutMillis, int retries,
                       Map<String, String> headers, Map<String, String> payloadTemplates) {
            this.enabled = enabled;
            this.url = url;
            this.method = method;
            this.contentType = contentType;
            this.timeoutMillis = timeoutMillis;
            this.retries = retries;
            this.headers = Collections.unmodifiableMap(headers);
            this.payloadTemplates = Collections.unmodifiableMap(payloadTemplates);
        }

        public boolean enabled() { return enabled; }
        public String url() { return url; }
        public String method() { return method; }
        public String contentType() { return contentType; }
        public int timeoutMillis() { return timeoutMillis; }
        public int retries() { return retries; }
        public Map<String, String> headers() { return headers; }
        public Map<String, String> payloadTemplates() { return payloadTemplates; }
    }

    public static final class Storage {
        private final String type;
        private final Mysql mysql;
        private final Sqlite sqlite;

        public Storage(String type, Mysql mysql, Sqlite sqlite) {
            this.type = type;
            this.mysql = mysql;
            this.sqlite = sqlite;
        }

        public String type() { return type; }
        public Mysql mysql() { return mysql; }
        public Sqlite sqlite() { return sqlite; }
    }

    public static final class Mysql {
        private final String host;
        private final int port;
        private final String database;
        private final String username;
        private final String password;
        private final boolean ssl;
        private final int poolSize;
        private final long connectionTimeout;
        private final String tablePrefix;
        private final boolean autoCreateTables;
        private final boolean autoMigrate;
        private final String charset;

        public Mysql(String host, int port, String database, String username, String password, boolean ssl, int poolSize,
                     long connectionTimeout, String tablePrefix, boolean autoCreateTables, boolean autoMigrate, String charset) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
            this.ssl = ssl;
            this.poolSize = poolSize;
            this.connectionTimeout = connectionTimeout;
            this.tablePrefix = tablePrefix;
            this.autoCreateTables = autoCreateTables;
            this.autoMigrate = autoMigrate;
            this.charset = charset;
        }

        public String host() { return host; }
        public int port() { return port; }
        public String database() { return database; }
        public String username() { return username; }
        public String password() { return password; }
        public boolean ssl() { return ssl; }
        public int poolSize() { return poolSize; }
        public long connectionTimeout() { return connectionTimeout; }
        public String tablePrefix() { return tablePrefix; }
        public boolean autoCreateTables() { return autoCreateTables; }
        public boolean autoMigrate() { return autoMigrate; }
        public String charset() { return charset; }
    }

    public static final class Sqlite {
        private final File file;

        public Sqlite(File file) {
            this.file = file;
        }

        public File file() { return file; }
    }

    public static final class Session {
        private final boolean rememberSession;
        private final boolean rememberIp;
        private final boolean rememberDevice;
        private final int rememberDurationHours;
        private final boolean requireReauthOnReconnect;
        private final int expireMinutes;

        public Session(boolean rememberSession, boolean rememberIp, boolean rememberDevice, int rememberDurationHours,
                       boolean requireReauthOnReconnect, int expireMinutes) {
            this.rememberSession = rememberSession;
            this.rememberIp = rememberIp;
            this.rememberDevice = rememberDevice;
            this.rememberDurationHours = rememberDurationHours;
            this.requireReauthOnReconnect = requireReauthOnReconnect;
            this.expireMinutes = expireMinutes;
        }

        public boolean rememberSession() { return rememberSession; }
        public boolean rememberIp() { return rememberIp; }
        public boolean rememberDevice() { return rememberDevice; }
        public int rememberDurationHours() { return rememberDurationHours; }
        public boolean requireReauthOnReconnect() { return requireReauthOnReconnect; }
        public int expireMinutes() { return expireMinutes; }
    }

    public static final class Protection {
        private final boolean blockMovement;
        private final boolean blockDamage;
        private final boolean blockChat;
        private final boolean blockCommands;
        private final boolean blockItemDrop;
        private final boolean blockItemPickup;
        private final boolean blockInventoryClick;
        private final boolean blockInteraction;
        private final boolean hideUnauthenticated;
        private final boolean invisibleUnauthenticated;
        private final List<String> allowedCommands;

        public Protection(boolean blockMovement, boolean blockDamage, boolean blockChat, boolean blockCommands,
                          boolean blockItemDrop, boolean blockItemPickup, boolean blockInventoryClick,
                          boolean blockInteraction, boolean hideUnauthenticated, boolean invisibleUnauthenticated,
                          List<String> allowedCommands) {
            this.blockMovement = blockMovement;
            this.blockDamage = blockDamage;
            this.blockChat = blockChat;
            this.blockCommands = blockCommands;
            this.blockItemDrop = blockItemDrop;
            this.blockItemPickup = blockItemPickup;
            this.blockInventoryClick = blockInventoryClick;
            this.blockInteraction = blockInteraction;
            this.hideUnauthenticated = hideUnauthenticated;
            this.invisibleUnauthenticated = invisibleUnauthenticated;
            this.allowedCommands = Collections.unmodifiableList(allowedCommands);
        }

        public boolean blockMovement() { return blockMovement; }
        public boolean blockDamage() { return blockDamage; }
        public boolean blockChat() { return blockChat; }
        public boolean blockCommands() { return blockCommands; }
        public boolean blockItemDrop() { return blockItemDrop; }
        public boolean blockItemPickup() { return blockItemPickup; }
        public boolean blockInventoryClick() { return blockInventoryClick; }
        public boolean blockInteraction() { return blockInteraction; }
        public boolean hideUnauthenticated() { return hideUnauthenticated; }
        public boolean invisibleUnauthenticated() { return invisibleUnauthenticated; }
        public List<String> allowedCommands() { return allowedCommands; }
    }

    public static final class Commands {
        private final boolean useTranslatedAliases;
        private final List<String> globalLoginAliases;
        private final List<String> globalRegisterAliases;
        private final List<String> globalAdminAliases;

        public Commands(boolean useTranslatedAliases, List<String> globalLoginAliases, List<String> globalRegisterAliases,
                        List<String> globalAdminAliases) {
            this.useTranslatedAliases = useTranslatedAliases;
            this.globalLoginAliases = Collections.unmodifiableList(globalLoginAliases);
            this.globalRegisterAliases = Collections.unmodifiableList(globalRegisterAliases);
            this.globalAdminAliases = Collections.unmodifiableList(globalAdminAliases);
        }

        public boolean useTranslatedAliases() { return useTranslatedAliases; }
        public List<String> globalLoginAliases() { return globalLoginAliases; }
        public List<String> globalRegisterAliases() { return globalRegisterAliases; }
        public List<String> globalAdminAliases() { return globalAdminAliases; }
    }

    public static final class Visual {
        private final boolean titleEnabled;
        private final boolean actionBarEnabled;
        private final boolean bossBarEnabled;
        private final boolean hologramEnabled;
        private final boolean hologramFallbackChat;
        private final boolean countdownEnabled;
        private final boolean inventoryCaptchaEnabled;

        public Visual(boolean titleEnabled, boolean actionBarEnabled, boolean bossBarEnabled, boolean hologramEnabled,
                      boolean hologramFallbackChat, boolean countdownEnabled, boolean inventoryCaptchaEnabled) {
            this.titleEnabled = titleEnabled;
            this.actionBarEnabled = actionBarEnabled;
            this.bossBarEnabled = bossBarEnabled;
            this.hologramEnabled = hologramEnabled;
            this.hologramFallbackChat = hologramFallbackChat;
            this.countdownEnabled = countdownEnabled;
            this.inventoryCaptchaEnabled = inventoryCaptchaEnabled;
        }

        public boolean titleEnabled() { return titleEnabled; }
        public boolean actionBarEnabled() { return actionBarEnabled; }
        public boolean bossBarEnabled() { return bossBarEnabled; }
        public boolean hologramEnabled() { return hologramEnabled; }
        public boolean hologramFallbackChat() { return hologramFallbackChat; }
        public boolean countdownEnabled() { return countdownEnabled; }
        public boolean inventoryCaptchaEnabled() { return inventoryCaptchaEnabled; }
    }

    public static final class Premium {
        private final boolean autoDetectEnabled;
        private final int timeoutMillis;
        private final String endpoint;

        public Premium(boolean autoDetectEnabled, int timeoutMillis, String endpoint) {
            this.autoDetectEnabled = autoDetectEnabled;
            this.timeoutMillis = timeoutMillis;
            this.endpoint = endpoint;
        }

        public boolean autoDetectEnabled() { return autoDetectEnabled; }
        public int timeoutMillis() { return timeoutMillis; }
        public String endpoint() { return endpoint; }
    }
}
