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

import dev.helix.suleiman.api.model.OperationResult;
import dev.helix.suleiman.core.config.PluginSettings;

public final class PasswordPolicyValidator {
    private final PluginSettings.PasswordPolicy policy;

    public PasswordPolicyValidator(PluginSettings.PasswordPolicy policy) {
        this.policy = policy;
    }

    public OperationResult validate(String password, String confirmation) {
        if (password == null || password.isEmpty()) {
            return OperationResult.failure("auth.password-empty");
        }
        if (password.length() < policy.minLength()) {
            return OperationResult.failure("auth.password-too-short", Integer.valueOf(policy.minLength()));
        }
        if (password.length() > policy.maxLength()) {
            return OperationResult.failure("auth.password-too-long", Integer.valueOf(policy.maxLength()));
        }
        if (policy.requireConfirmation() && (confirmation == null || !password.equals(confirmation))) {
            return OperationResult.failure("auth.password-mismatch");
        }
        if (policy.requireUppercase() && !password.matches(".*[A-Z].*")) {
            return OperationResult.failure("auth.password-missing-uppercase");
        }
        if (policy.requireLowercase() && !password.matches(".*[a-z].*")) {
            return OperationResult.failure("auth.password-missing-lowercase");
        }
        if (policy.requireNumber() && !password.matches(".*\\d.*")) {
            return OperationResult.failure("auth.password-missing-number");
        }
        if (policy.requireSpecial() && password.matches("[A-Za-z0-9]*")) {
            return OperationResult.failure("auth.password-missing-special");
        }
        for (String blocked : policy.blockedPatterns()) {
            if (!blocked.isEmpty() && password.toLowerCase().contains(blocked.toLowerCase())) {
                return OperationResult.failure("auth.password-blocked-pattern");
            }
        }
        return OperationResult.success("auth.password-valid");
    }
}
