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

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import dev.helix.suleiman.api.enums.HashAlgorithm;

public final class Argon2PasswordHasher implements PasswordHasher {
    private final int memoryKiB;
    private final int iterations;
    private final int parallelism;

    public Argon2PasswordHasher(int memoryKiB, int iterations, int parallelism) {
        this.memoryKiB = memoryKiB;
        this.iterations = iterations;
        this.parallelism = parallelism;
    }

    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.ARGON2ID;
    }

    @Override
    public String hash(String plainText) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] password = plainText.toCharArray();
        try {
            return argon2.hash(iterations, memoryKiB, parallelism, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    @Override
    public boolean verify(String plainText, String hashed) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] password = plainText.toCharArray();
        try {
            return argon2.verify(hashed, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
