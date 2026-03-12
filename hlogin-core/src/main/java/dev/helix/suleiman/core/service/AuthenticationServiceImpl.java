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

import dev.helix.suleiman.api.enums.AccountMode;
import dev.helix.suleiman.api.enums.AuthState;
import dev.helix.suleiman.api.enums.WebhookEventType;
import dev.helix.suleiman.api.model.AuthResult;
import dev.helix.suleiman.api.model.CaptchaResult;
import dev.helix.suleiman.api.model.OperationResult;
import dev.helix.suleiman.api.service.AuthenticationService;
import dev.helix.suleiman.core.captcha.CaptchaPurpose;
import dev.helix.suleiman.core.captcha.CaptchaTicket;
import dev.helix.suleiman.core.context.CoreContainer;
import dev.helix.suleiman.core.model.AccountRecord;
import dev.helix.suleiman.core.model.AuditEntry;
import dev.helix.suleiman.core.security.PasswordHasher;
import dev.helix.suleiman.core.security.PasswordPolicyValidator;
import dev.helix.suleiman.core.session.RuntimeSession;
import dev.helix.suleiman.core.util.StringNormalizer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public final class AuthenticationServiceImpl implements AuthenticationService {
    private final CoreContainer container;
    private final CaptchaServiceImpl captchaService;
    private final WebhookServiceImpl webhookService;
    private final SessionCoordinator sessionCoordinator;

    public AuthenticationServiceImpl(CoreContainer container, CaptchaServiceImpl captchaService,
                                     WebhookServiceImpl webhookService, SessionCoordinator sessionCoordinator) {
        this.container = container;
        this.captchaService = captchaService;
        this.webhookService = webhookService;
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    public CompletableFuture<AuthResult> register(Player player, String password, String confirmation) {
        return CompletableFuture.supplyAsync(() -> doRegister(player, password, confirmation), container.executorService());
    }

    @Override
    public CompletableFuture<AuthResult> login(Player player, String password) {
        return CompletableFuture.supplyAsync(() -> doLogin(player, password), container.executorService());
    }

    @Override
    public CompletableFuture<AuthResult> logout(Player player, boolean invalidateRememberSession) {
        return CompletableFuture.supplyAsync(() -> {
            RuntimeSession session = container.sessionRegistry().get(player.getUniqueId());
            if (session == null) {
                return AuthResult.failure("auth.session-missing", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
            }
            if (invalidateRememberSession) {
                Optional<AccountRecord> account = resolveAccount(session);
                if (account.isPresent()) {
                    AccountRecord source = account.get();
                    container.storageContext().accountRepository().update(new AccountRecord(
                        source.uniqueId(), source.currentName(), source.normalizedName(), source.passwordHash(), source.hashAlgorithm(),
                        source.registrationIp(), source.lastLoginIp(), source.firstLogin(), source.lastLogin(), source.registerDate(),
                        source.language(), source.totalLogins(), source.failedAttempts(), source.blockedUntil(),
                        source.captchaSuccessCount(), source.captchaFailureCount(), source.webhookFlags(), source.adminNotes(),
                        source.accountMode(), source.lastServer(), source.lastKnownVersion(), null, null
                    ));
                }
            }
            sessionCoordinator.markLoggedOut(session);
            return AuthResult.success("auth.logout-success", session.state());
        }, container.executorService());
    }

    @Override
    public CompletableFuture<AuthResult> validateCaptcha(Player player, String response) {
        return CompletableFuture.supplyAsync(() -> doValidateCaptcha(player, response), container.executorService());
    }

    public AuthResult validateCaptchaNow(Player player, String response) {
        return doValidateCaptcha(player, response);
    }

    @Override
    public CompletableFuture<AuthResult> forceCaptcha(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            RuntimeSession session = container.sessionRegistry().get(player.getUniqueId());
            if (session == null) {
                return AuthResult.failure("auth.session-missing", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
            }
            AuthState nextState = session.state() == AuthState.LOGGED ? AuthState.LOGGED : (session.registered() ? AuthState.REGISTERED_NOT_LOGGED : AuthState.UNREGISTERED);
            CaptchaTicket ticket = captchaService.issue(session, CaptchaPurpose.FORCED, nextState, container.settings().captcha().defaultType());
            return AuthResult.failure("captcha.required", AuthState.CAPTCHA_PENDING, ticket.attemptsRemaining(), null, ticket.toChallenge());
        }, container.executorService());
    }

    @Override
    public CompletableFuture<AuthResult> changePassword(UUID uniqueId, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(uniqueId);
            if (!account.isPresent()) {
                return AuthResult.failure("admin.user-not-found", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
            }
            PasswordHasher hasher = container.passwordHasherFactory().current(container.settings().security().algorithm());
            container.storageContext().accountRepository().updatePassword(uniqueId, hasher.hash(newPassword), hasher.algorithm());
            return AuthResult.success("auth.password-updated", AuthState.REGISTERED_NOT_LOGGED);
        }, container.executorService());
    }

    private AuthResult doRegister(Player player, String password, String confirmation) {
        RuntimeSession session = container.sessionRegistry().get(player.getUniqueId());
        if (session == null) {
            return AuthResult.failure("auth.session-missing", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
        }
        if (session.registered()) {
            return AuthResult.failure("auth.already-registered", session.state(), 0, session.blockedUntil(), captchaChallenge(session));
        }
        if (requiresCaptcha(session, CaptchaPurpose.REGISTER)) {
            return AuthResult.failure("captcha.required", AuthState.CAPTCHA_PENDING, attemptsRemaining(session), null, captchaChallenge(session));
        }
        OperationResult validation = new PasswordPolicyValidator(container.settings().passwordPolicy()).validate(password, confirmation);
        if (!validation.isSuccess()) {
            return AuthResult.failure(validation.getMessageKey(), AuthState.UNREGISTERED, 0, null, null);
        }
        if (container.storageContext().accountRepository().findByNormalizedName(StringNormalizer.normalizeName(player.getName())).isPresent()) {
            return AuthResult.failure("auth.already-registered", AuthState.UNREGISTERED, 0, null, null);
        }
        PasswordHasher hasher = container.passwordHasherFactory().current(container.settings().security().algorithm());
        Instant now = Instant.now();
        boolean autoLogin = container.settings().general().autoLoginAfterRegister();
        AccountRecord account = new AccountRecord(
            player.getUniqueId(),
            player.getName(),
            StringNormalizer.normalizeName(player.getName()),
            hasher.hash(password),
            hasher.algorithm(),
            session.ipAddress(),
            autoLogin ? session.ipAddress() : null,
            autoLogin ? now : null,
            autoLogin ? now : null,
            now,
            session.language(),
            autoLogin ? 1L : 0L,
            0,
            null,
            0L,
            0L,
            "",
            "",
            AccountMode.OFFLINE,
            container.settings().general().serverName(),
            session.version(),
            null,
            null
        );
        container.storageContext().accountRepository().insert(account);
        session.registered(true);
        session.remembered(false);
        session.registerCaptchaVerified(false);
        if (autoLogin) {
            sessionCoordinator.markLogged(session);
        } else {
            session.state(AuthState.REGISTERED_NOT_LOGGED);
            session.expiresAt(now.plusSeconds(container.settings().general().authTimeoutSeconds()));
        }
        audit(player.getUniqueId(), player.getName(), "REGISTER_SUCCESS", session.ipAddress(), "register");
        webhookService.publish(WebhookEventType.REGISTER_SUCCESS, payload(session, "success", 0));
        webhookService.publish(WebhookEventType.ACCOUNT_CREATED, payload(session, "created", 0));
        return AuthResult.success(autoLogin ? "auth.register-login-success" : "auth.register-success", session.state());
    }

    private AuthResult doLogin(Player player, String password) {
        RuntimeSession session = container.sessionRegistry().get(player.getUniqueId());
        if (session == null) {
            return AuthResult.failure("auth.session-missing", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
        }
        Optional<AccountRecord> optional = resolveAccount(session);
        if (!optional.isPresent()) {
            session.registered(false);
            session.state(AuthState.UNREGISTERED);
            return AuthResult.failure("auth.not-registered", AuthState.UNREGISTERED, 0, null, null);
        }
        AccountRecord account = optional.get();
        session.registered(true);
        if (account.blockedUntil() != null && account.blockedUntil().isAfter(Instant.now())) {
            session.state(AuthState.BLOCKED);
            session.blockedUntil(account.blockedUntil());
            return AuthResult.failure("auth.blocked", AuthState.BLOCKED, 0, account.blockedUntil(), null);
        }
        if (shouldIssueCaptcha(session)) {
            CaptchaTicket ticket = session.captchaTicket() == null
                ? captchaService.issue(session, CaptchaPurpose.LOGIN, AuthState.REGISTERED_NOT_LOGGED, container.settings().captcha().defaultType())
                : session.captchaTicket();
            return AuthResult.failure("captcha.required", AuthState.CAPTCHA_PENDING, ticket.attemptsRemaining(), null, ticket.toChallenge());
        }
        PasswordHasher hasher = container.passwordHasherFactory().current(account.hashAlgorithm());
        if (!hasher.verify(password, account.passwordHash())) {
            int failedAttempts = session.failedAttempts() + 1;
            int attemptsRemaining = Math.max(0, container.settings().security().maxAttempts() - failedAttempts);
            Instant blockedUntil = failedAttempts >= container.settings().security().maxAttempts()
                ? Instant.now().plusSeconds(container.settings().security().blockDurationSeconds()) : null;
            session.failedAttempts(failedAttempts);
            session.blockedUntil(blockedUntil);
            session.state(blockedUntil == null ? AuthState.REGISTERED_NOT_LOGGED : AuthState.BLOCKED);
            container.storageContext().accountRepository().updateFailedAttempts(account.uniqueId(), failedAttempts, blockedUntil);
            webhookService.publish(WebhookEventType.LOGIN_FAILURE, payload(session, "failure", failedAttempts));
            if (blockedUntil != null) {
                webhookService.publish(WebhookEventType.ACCOUNT_BLOCKED, payload(session, "blocked", failedAttempts));
            }
            audit(account.uniqueId(), session.playerName(), "LOGIN_FAILURE", session.ipAddress(), "attempts=" + failedAttempts);
            return AuthResult.failure(blockedUntil == null ? "auth.login-invalid" : "auth.blocked", session.state(), attemptsRemaining, blockedUntil, captchaChallenge(session));
        }
        Instant now = Instant.now();
        String rememberHash = container.settings().session().rememberSession() ? sessionCoordinator.fingerprint(session) : null;
        Instant rememberUntil = rememberHash == null ? null : now.plusSeconds(container.settings().session().rememberDurationHours() * 3600L);
        container.storageContext().accountRepository().updateLoginSuccess(
            account.uniqueId(),
            session.playerName(),
            session.ipAddress(),
            now,
            account.totalLogins() + 1L,
            0,
            null,
            rememberHash,
            rememberUntil,
            container.settings().general().serverName(),
            session.version()
        );
        session.remembered(rememberHash != null);
        session.loginCaptchaVerified(false);
        sessionCoordinator.markLogged(session);
        captchaService.invalidate(session.uniqueId());
        webhookService.publish(WebhookEventType.LOGIN_SUCCESS, payload(session, "success", 0));
        audit(account.uniqueId(), session.playerName(), "LOGIN_SUCCESS", session.ipAddress(), "login");
        return AuthResult.success("auth.login-success", AuthState.LOGGED);
    }

    private AuthResult doValidateCaptcha(Player player, String response) {
        RuntimeSession session = container.sessionRegistry().get(player.getUniqueId());
        if (session == null || session.captchaTicket() == null) {
            return AuthResult.failure("captcha.none", AuthState.REGISTERED_NOT_LOGGED, 0, null, null);
        }
        CaptchaTicket ticket = session.captchaTicket();
        CaptchaResult result = captchaService.verify(player.getUniqueId(), response);
        Optional<AccountRecord> account = resolveAccount(session);
        if (account.isPresent()) {
            long successCount = account.get().captchaSuccessCount() + (result.isSuccess() ? 1L : 0L);
            long failureCount = account.get().captchaFailureCount() + (result.isSuccess() ? 0L : 1L);
            container.storageContext().accountRepository().updateCaptchaStats(account.get().uniqueId(), successCount, failureCount);
        }
        if (result.isSuccess()) {
            session.state(ticket.stateAfterSuccess());
            if (ticket.stateAfterSuccess() == AuthState.LOGGED) {
                sessionCoordinator.markLogged(session);
            }
            return AuthResult.success("captcha.success", session.state());
        }
        session.state(AuthState.CAPTCHA_PENDING);
        if (session.captchaTicket() != null && session.captchaTicket().type() == dev.helix.suleiman.api.enums.CaptchaType.INVENTORY) {
            captchaService.rotate(session);
        }
        webhookService.publish(WebhookEventType.CAPTCHA_FAILED, payload(session, "failure", session.failedAttempts()));
        return AuthResult.failure(result.getMessageKey(), AuthState.CAPTCHA_PENDING, result.getRemainingAttempts(), null, captchaChallenge(session));
    }

    private Optional<AccountRecord> resolveAccount(RuntimeSession session) {
        Optional<AccountRecord> account = container.storageContext().accountRepository().findByUniqueId(session.uniqueId());
        if (account.isPresent()) {
            return account;
        }
        return container.storageContext().accountRepository().findByNormalizedName(StringNormalizer.normalizeName(session.playerName()));
    }

    private boolean shouldIssueCaptcha(RuntimeSession session) {
        if (!container.settings().captcha().enabled()) {
            return false;
        }
        if (session.loginCaptchaVerified()) {
            return false;
        }
        if (session.captchaTicket() != null) {
            return true;
        }
        return container.settings().captcha().requireOnLogin()
            || session.failedAttempts() >= container.settings().captcha().afterFailedAttempts();
    }

    private boolean requiresCaptcha(RuntimeSession session, CaptchaPurpose purpose) {
        if (!container.settings().captcha().enabled()) {
            return false;
        }
        if (purpose == CaptchaPurpose.REGISTER && session.registerCaptchaVerified()) {
            return false;
        }
        if (session.captchaTicket() != null) {
            return true;
        }
        if (purpose == CaptchaPurpose.REGISTER) {
            return container.settings().captcha().requireOnRegister();
        }
        return false;
    }

    private int attemptsRemaining(RuntimeSession session) {
        return session.captchaTicket() == null ? 0 : session.captchaTicket().attemptsRemaining();
    }

    private Map<String, Object> payload(RuntimeSession session, String result, int attempts) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("player", session.playerName());
        payload.put("uuid", session.uniqueId());
        payload.put("ip", session.ipAddress());
        payload.put("timestamp", Instant.now().toString());
        payload.put("language", session.language());
        payload.put("attempts", Integer.valueOf(attempts));
        payload.put("result", result);
        payload.put("server", container.settings().general().serverName());
        payload.put("auth_method", "password");
        return payload;
    }

    private void audit(UUID uniqueId, String name, String action, String ip, String details) {
        if (!container.settings().general().auditEnabled()) {
            return;
        }
        container.storageContext().auditRepository().append(new AuditEntry(uniqueId, name, null, action, ip, details, Instant.now()));
    }

    private dev.helix.suleiman.api.model.CaptchaChallenge captchaChallenge(RuntimeSession session) {
        return session.captchaTicket() == null ? null : session.captchaTicket().toChallenge();
    }
}
