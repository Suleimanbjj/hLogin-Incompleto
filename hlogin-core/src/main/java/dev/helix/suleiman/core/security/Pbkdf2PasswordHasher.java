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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final int KEY_LENGTH = 256;
    private final int iterations;
    private final SecureRandom random = new SecureRandom();

    public Pbkdf2PasswordHasher(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.PBKDF2_SHA256;
    }

    @Override
    public String hash(String plainText) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return iterations + ":" + Base64.getEncoder().encodeToString(salt) + ":" + derive(plainText.toCharArray(), salt);
    }

    @Override
    public boolean verify(String plainText, String hashed) {
        String[] parts = hashed.split(":");
        if (parts.length != 3) {
            return false;
        }
        int storedIterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        String comparison = derive(plainText.toCharArray(), salt, storedIterations);
        return slowEquals(comparison, parts[2]);
    }

    private String derive(char[] plainText, byte[] salt) {
        return derive(plainText, salt, iterations);
    }

    private String derive(char[] plainText, byte[] salt, int actualIterations) {
        PBEKeySpec spec = new PBEKeySpec(plainText, salt, actualIterations, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("PBKDF2 algorithm is unavailable", ex);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("PBKDF2 key spec is invalid", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private boolean slowEquals(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        int diff = 0;
        for (int index = 0; index < left.length(); index++) {
            diff |= left.charAt(index) ^ right.charAt(index);
        }
        return diff == 0;
    }
}
