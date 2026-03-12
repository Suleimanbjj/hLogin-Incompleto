/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.captcha;

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CaptchaTicket {
    private final CaptchaType type;
    private final String answer;
    private final String prompt;
    private final CaptchaPurpose purpose;
    private final AuthState stateAfterSuccess;
    private final Instant expiresAt;
    private final Map<String, String> metadata;
    private int attemptsRemaining;

    public CaptchaTicket(CaptchaType type, String answer, String prompt, CaptchaPurpose purpose,
                         AuthState stateAfterSuccess, Instant expiresAt, int attemptsRemaining,
                         Map<String, String> metadata) {
        this.type = type;
        this.answer = answer;
        this.prompt = prompt;
        this.purpose = purpose;
        this.stateAfterSuccess = stateAfterSuccess;
        this.expiresAt = expiresAt;
        this.attemptsRemaining = attemptsRemaining;
        this.metadata = metadata == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, String>(metadata));
    }

    public CaptchaType type() { return type; }
    public String answer() { return answer; }
    public String prompt() { return prompt; }
    public CaptchaPurpose purpose() { return purpose; }
    public AuthState stateAfterSuccess() { return stateAfterSuccess; }
    public Instant expiresAt() { return expiresAt; }
    public int attemptsRemaining() { return attemptsRemaining; }
    public Map<String, String> metadata() { return metadata; }

    public boolean expired() {
        return Instant.now().isAfter(expiresAt);
    }

    public int consumeFailure() {
        attemptsRemaining = Math.max(0, attemptsRemaining - 1);
        return attemptsRemaining;
    }

    public CaptchaChallenge toChallenge() {
        return new CaptchaChallenge(type, prompt, expiresAt, attemptsRemaining, metadata);
    }
}
