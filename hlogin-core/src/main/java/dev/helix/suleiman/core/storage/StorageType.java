/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.storage;

public enum StorageType {
    MYSQL,
    SQLITE;

    public static StorageType parse(String value) {
        if (value == null) {
            return MYSQL;
        }
        try {
            return StorageType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MYSQL;
        }
    }
}
