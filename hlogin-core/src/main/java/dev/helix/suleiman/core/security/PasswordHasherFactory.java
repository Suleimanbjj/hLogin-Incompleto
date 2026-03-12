/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.security;

import dev.helix.suleiman.api.enums.HashAlgorithm;
import dev.helix.suleiman.core.config.PluginSettings;
import java.util.EnumMap;
import java.util.Map;

public final class PasswordHasherFactory {
    private final Map<HashAlgorithm, PasswordHasher> hashers;

    public PasswordHasherFactory(PluginSettings.Security security) {
        Map<HashAlgorithm, PasswordHasher> created = new EnumMap<HashAlgorithm, PasswordHasher>(HashAlgorithm.class);
        created.put(HashAlgorithm.BCRYPT, new BCryptPasswordHasher(security.bcryptCost()));
        created.put(HashAlgorithm.PBKDF2_SHA256, new Pbkdf2PasswordHasher(security.pbkdf2Iterations()));
        created.put(HashAlgorithm.ARGON2ID, new Argon2PasswordHasher(security.argon2MemoryKiB(), security.argon2Iterations(), security.argon2Parallelism()));
        this.hashers = created;
    }

    public PasswordHasher current(HashAlgorithm algorithm) {
        PasswordHasher hasher = hashers.get(algorithm);
        return hasher == null ? hashers.get(HashAlgorithm.BCRYPT) : hasher;
    }
}
