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

import dev.helix.suleiman.api.enums.WebhookEventType;
import dev.helix.suleiman.api.service.WebhookService;
import dev.helix.suleiman.core.config.PluginSettings;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.util.JsonUtil;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WebhookServiceImpl implements WebhookService {
    private final CoreContainer container;

    public WebhookServiceImpl(CoreContainer container) {
        this.container = container;
    }

    @Override
    public void publish(WebhookEventType eventType, Map<String, Object> values) {
        final PluginSettings.Webhook webhook = container.settings().webhook();
        if (!webhook.enabled() || webhook.url().isEmpty()) {
            return;
        }
        final Map<String, Object> payload = new LinkedHashMap<String, Object>(values);
        payload.put("event", eventType.name().toLowerCase());
        payload.put("plugin", "hLogin");
        payload.put("version", container.pluginVersion());
        container.executorService().submit(new Runnable() {
            @Override
            public void run() {
                send(webhook, eventType, payload);
            }
        });
    }

    private void send(PluginSettings.Webhook webhook, WebhookEventType eventType, Map<String, Object> values) {
        String templateKey = eventType.name().toLowerCase();
        String template = webhook.payloadTemplates().get(templateKey);
        String body = template == null || template.trim().isEmpty() ? JsonUtil.toJson(values) : applyTemplate(template, values);
        int attempts = Math.max(1, webhook.retries() + 1);
        for (int index = 0; index < attempts; index++) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(webhook.url()).openConnection();
                connection.setRequestMethod(webhook.method());
                connection.setConnectTimeout(webhook.timeoutMillis());
                connection.setReadTimeout(webhook.timeoutMillis());
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", webhook.contentType());
                for (Map.Entry<String, String> header : webhook.headers().entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
                OutputStream outputStream = connection.getOutputStream();
                try {
                    outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                } finally {
                    outputStream.close();
                }
                int code = connection.getResponseCode();
                if (code >= 200 && code < 300) {
                    return;
                }
            } catch (Exception exception) {
                if (container.settings().general().debug()) {
                    container.logger().warning("Webhook dispatch failed: " + exception.getMessage());
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private String applyTemplate(String template, Map<String, Object> values) {
        String rendered = template;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            rendered = rendered.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
        }
        return rendered;
    }
}
