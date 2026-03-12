/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package dev.helix.suleiman.api.service;

import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import dev.helix.suleiman.api.model.CaptchaResult;
import java.util.UUID;

public interface CaptchaService {

    CaptchaChallenge requestChallenge(UUID playerId, String playerName, String ipAddress, CaptchaType preferredType);

    CaptchaResult verify(UUID playerId, String response);

    CaptchaChallenge getActiveChallenge(UUID playerId);

    void invalidate(UUID playerId);
}
