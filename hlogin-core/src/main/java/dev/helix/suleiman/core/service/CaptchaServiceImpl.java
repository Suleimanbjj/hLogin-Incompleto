/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.service;

import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.CaptchaType;
import dev.helix.suleiman.api.model.CaptchaChallenge;
import dev.helix.suleiman.api.model.CaptchaResult;
import dev.helix.suleiman.api.service.CaptchaService;
import dev.helix.suleiman.core.captcha.CaptchaAnimal;
import dev.helix.suleiman.core.captcha.CaptchaPurpose;
import dev.helix.suleiman.core.captcha.CaptchaTicket;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.session.RuntimeSession;
import dev.helix.suleiman.core.util.DigestUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CaptchaServiceImpl implements CaptchaService {
    private final CoreContainer container;

    public CaptchaServiceImpl(CoreContainer container) {
        this.container = container;
    }

    @Override
    public CaptchaChallenge requestChallenge(UUID playerId, String playerName, String ipAddress, CaptchaType preferredType) {
        RuntimeSession session = container.sessionRegistry().get(playerId);
        if (session == null) {
            return null;
        }
        return issue(session, CaptchaPurpose.THRESHOLD, AuthState.REGISTERED_NOT_LOGGED, preferredType).toChallenge();
    }

    public CaptchaTicket issue(RuntimeSession session, CaptchaPurpose purpose, AuthState stateAfterSuccess, CaptchaType preferredType) {
        int attempts = container.settings().captcha().maxAttempts();
        return createTicket(session, purpose, stateAfterSuccess, preferredType, attempts);
    }

    public CaptchaTicket rotate(RuntimeSession session) {
        if (session == null || session.captchaTicket() == null) {
            return null;
        }
        CaptchaTicket current = session.captchaTicket();
        return createTicket(session, current.purpose(), current.stateAfterSuccess(), current.type(), current.attemptsRemaining());
    }

    private CaptchaTicket createTicket(RuntimeSession session, CaptchaPurpose purpose, AuthState stateAfterSuccess,
                                       CaptchaType preferredType, int attempts) {
        CaptchaType type = preferredType == null ? container.settings().captcha().defaultType() : preferredType;
        if (type == CaptchaType.INVENTORY && !container.settings().visual().inventoryCaptchaEnabled()) {
            type = CaptchaType.TEXT;
        }
        if (purpose == CaptchaPurpose.REGISTER) {
            session.registerCaptchaVerified(false);
        } else {
            session.loginCaptchaVerified(false);
        }
        Instant expiresAt = Instant.now().plusSeconds(container.settings().captcha().timeoutSeconds());
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("purpose", purpose.name());
        String prompt;
        String answer;
        if (type == CaptchaType.MATH) {
            int first = 2 + (int) (Math.random() * 8);
            int second = 1 + (int) (Math.random() * 9);
            answer = String.valueOf(first + second);
            prompt = "&eCaptcha: &f" + first + " + " + second + " = ?";
        } else if (type == CaptchaType.INVENTORY) {
            List<CaptchaAnimal> animals = new ArrayList<CaptchaAnimal>();
            Collections.addAll(animals, CaptchaAnimal.values());
            Collections.shuffle(animals);
            int[] displaySlots = new int[] { 2, 3, 4, 5, 6 };
            CaptchaAnimal targetAnimal = animals.get((int) (Math.random() * animals.size()));
            answer = targetAnimal.id();
            prompt = container.translationRegistry().render(session.language(), "captcha.inventory-prompt-animal", animalName(session.language(), targetAnimal));
            metadata.put("correct_animal", answer);
            metadata.put("challenge_id", DigestUtil.randomToken(8));
            metadata.put("inventory_title", container.translationRegistry().render(session.language(), "captcha.inventory-title"));
            for (int index = 0; index < animals.size(); index++) {
                metadata.put("slot_" + displaySlots[index], animals.get(index).id());
            }
        } else {
            answer = DigestUtil.randomToken(type == CaptchaType.VISUAL ? 4 : 5);
            prompt = "&eCaptcha: &f" + answer;
        }
        CaptchaTicket ticket = new CaptchaTicket(type, answer, prompt, purpose, stateAfterSuccess, expiresAt, attempts, metadata);
        session.captchaTicket(ticket);
        session.state(AuthState.CAPTCHA_PENDING);
        return ticket;
    }

    @Override
    public CaptchaResult verify(UUID playerId, String response) {
        RuntimeSession session = container.sessionRegistry().get(playerId);
        if (session == null || session.captchaTicket() == null) {
            return CaptchaResult.failure("captcha.none", 0);
        }
        CaptchaTicket ticket = session.captchaTicket();
        if (ticket.expired()) {
            session.captchaTicket(null);
            return CaptchaResult.failure("captcha.timeout", 0);
        }
        if (ticket.answer().equalsIgnoreCase(response == null ? "" : response.trim())) {
            if (ticket.purpose() == CaptchaPurpose.REGISTER) {
                session.registerCaptchaVerified(true);
            } else {
                session.loginCaptchaVerified(true);
            }
            session.captchaTicket(null);
            return CaptchaResult.success("captcha.success");
        }
        int remaining = ticket.consumeFailure();
        if (remaining <= 0) {
            session.captchaTicket(null);
        }
        return CaptchaResult.failure("captcha.failed", remaining);
    }

    @Override
    public CaptchaChallenge getActiveChallenge(UUID playerId) {
        RuntimeSession session = container.sessionRegistry().get(playerId);
        return session == null || session.captchaTicket() == null ? null : session.captchaTicket().toChallenge();
    }

    @Override
    public void invalidate(UUID playerId) {
        RuntimeSession session = container.sessionRegistry().get(playerId);
        if (session != null) {
            session.captchaTicket(null);
        }
    }

    private String animalName(String locale, CaptchaAnimal animal) {
        String key = animal.translationKey();
        String translated = container.translationRegistry().render(locale, key);
        return key.equals(translated) ? animal.id() : translated;
    }
}
