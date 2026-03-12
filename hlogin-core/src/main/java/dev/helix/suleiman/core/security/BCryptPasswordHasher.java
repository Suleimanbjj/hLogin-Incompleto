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
import org.mindrot.jbcrypt.BCrypt;

public final class BCryptPasswordHasher implements PasswordHasher {
    private final int cost;

    public BCryptPasswordHasher(int cost) {
        this.cost = cost;
    }

    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.BCRYPT;
    }

    @Override
    public String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(cost));
    }

    @Override
    public boolean verify(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }
}
