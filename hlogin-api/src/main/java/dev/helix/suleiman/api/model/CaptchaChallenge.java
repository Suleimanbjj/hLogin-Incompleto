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

import dev.helix.suleiman.api.enums.CaptchaType;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CaptchaChallenge {
    private final CaptchaType type;
    private final String prompt;
    private final Instant expiresAt;
    private final int maxAttempts;
    private final Map<String, String> metadata;

    public CaptchaChallenge(CaptchaType type, String prompt, Instant expiresAt, int maxAttempts, Map<String, String> metadata) {
        this.type = type;
        this.prompt = prompt;
        this.expiresAt = expiresAt;
        this.maxAttempts = maxAttempts;
        this.metadata = metadata == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new HashMap<String, String>(metadata));
    }

    public CaptchaType getType() {
        return type;
    }

    public String getPrompt() {
        return prompt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
