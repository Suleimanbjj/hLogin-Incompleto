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

import dev.helix.suleiman.api.model.AuthResult;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public interface AuthenticationService {

    CompletableFuture<AuthResult> register(Player player, String password, String confirmation);

    CompletableFuture<AuthResult> login(Player player, String password);

    CompletableFuture<AuthResult> logout(Player player, boolean invalidateRememberSession);

    CompletableFuture<AuthResult> validateCaptcha(Player player, String response);

    CompletableFuture<AuthResult> forceCaptcha(Player player);

    CompletableFuture<AuthResult> changePassword(UUID uniqueId, String newPassword);
}
