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

public interface PasswordHasher {

    HashAlgorithm algorithm();

    String hash(String plainText);

    boolean verify(String plainText, String hashed);
}
