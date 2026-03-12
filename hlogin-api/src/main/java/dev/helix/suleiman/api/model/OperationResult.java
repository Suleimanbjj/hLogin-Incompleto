/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.model;

import java.util.Arrays;

public final class OperationResult {
    private final boolean success;
    private final String messageKey;
    private final Object[] messageArguments;

    public OperationResult(boolean success, String messageKey, Object... messageArguments) {
        this.success = success;
        this.messageKey = messageKey;
        this.messageArguments = messageArguments == null ? new Object[0] : Arrays.copyOf(messageArguments, messageArguments.length);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArguments() {
        return Arrays.copyOf(messageArguments, messageArguments.length);
    }

    public static OperationResult success(String messageKey, Object... messageArguments) {
        return new OperationResult(true, messageKey, messageArguments);
    }

    public static OperationResult failure(String messageKey, Object... messageArguments) {
        return new OperationResult(false, messageKey, messageArguments);
    }
}
