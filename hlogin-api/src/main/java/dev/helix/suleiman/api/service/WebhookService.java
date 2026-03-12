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

import dev.helix.suleiman.api.enums.WebhookEventType;
import java.util.Map;

public interface WebhookService {

    void publish(WebhookEventType eventType, Map<String, Object> values);
}
