/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.command;

import dev.helix.suleiman.api.model.AccountProfile;
import dev.helix.suleiman.api.model.OperationResult;
import dev.helix.suleiman.core.util.DigestUtil;
import dev.helix.suleiman.login.HLoginPlugin;
import dev.helix.suleiman.login.util.BukkitCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class RootCommandExecutor implements CommandExecutor, TabCompleter {
    private final HLoginPlugin plugin;

    public RootCommandExecutor(HLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return executeCanonical(sender, command.getName().toLowerCase(), args);
    }

    public boolean executeCanonical(CommandSender sender, String label, String[] args) {
        if ("login".equals(label)) {
            return executeLogin(sender, args);
        }
        if ("register".equals(label)) {
            return executeRegister(sender, args);
        }
        return executeAdmin(sender, args);
    }

    private boolean executeLogin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "general.player-only");
            return true;
        }
        if (args.length < 1) {
            plugin.sendMessage(sender, "command.usage-login");
            return true;
        }
        Player player = (Player) sender;
        plugin.getCore().authentication().login(player, args[0]).whenComplete((result, throwable) -> runSync(() -> {
            if (throwable != null) {
                handleAsyncFailure(player, "login", throwable);
                return;
            }
            plugin.handleAuthResult(player, result, "login");
        }));
        return true;
    }

    private boolean executeRegister(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "general.player-only");
            return true;
        }
        if (args.length < 1) {
            plugin.sendMessage(sender, "command.usage-register");
            return true;
        }
        String confirmation = args.length > 1 ? args[1] : args[0];
        Player player = (Player) sender;
        plugin.getCore().authentication().register(player, args[0], confirmation).whenComplete((result, throwable) -> runSync(() -> {
            if (throwable != null) {
                handleAsyncFailure(player, "register", throwable);
                return;
            }
            plugin.handleAuthResult(player, result, "register");
        }));
        return true;
    }

    private boolean executeAdmin(CommandSender sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            plugin.sendList(sender, "command.admin-help");
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if ("reload".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.reload")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            try {
                plugin.reloadPluginState();
                plugin.sendMessage(sender, "general.reloaded");
            } catch (Exception exception) {
                sender.sendMessage("Reload failed: " + exception.getMessage());
            }
            return true;
        }
        if ("status".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.status")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            sender.sendMessage(plugin.translate(sender, "admin.status-line", plugin.getCore().getVersion(), plugin.getVisualService().adapter().family(),
                plugin.getCore().settings().storage().type(), plugin.getCore().sessions().getActiveSessions().size()));
            return true;
        }
        if ("version".equals(subCommand)) {
            plugin.sendMessage(sender, "general.version", plugin.getCore().getVersion(), plugin.getVisualService().adapter().family());
            return true;
        }
        if ("debug".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.debug")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            boolean enabled = plugin.toggleDebugOverride();
            plugin.sendMessage(sender, enabled ? "admin.debug-enabled" : "admin.debug-disabled");
            return true;
        }
        if ("user".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.user")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            if (args.length < 2) {
                plugin.sendMessage(sender, "command.usage-user");
                return true;
            }
            plugin.getCore().accounts().findByName(args[1]).whenComplete((optional, throwable) -> runSync(() -> {
                if (throwable != null) {
                    handleAsyncFailure(sender, "hlogin user", throwable);
                    return;
                }
                sendUserInfo(sender, args[1], optional);
            }));
            return true;
        }
        if ("unregister".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.unregister")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            if (args.length < 2) {
                plugin.sendMessage(sender, "command.usage-unregister");
                return true;
            }
            resolveProfile(args[1]).thenCompose(optional -> optional.isPresent()
                ? plugin.getCore().accounts().unregister(optional.get().getUniqueId(), issuer(sender))
                : CompletableFuture.completedFuture(OperationResult.failure("admin.user-not-found")))
                .whenComplete((result, throwable) -> runSync(() -> {
                    if (throwable != null) {
                        handleAsyncFailure(sender, "hlogin unregister", throwable);
                        return;
                    }
                    plugin.sendMessage(sender, result.getMessageKey());
                }));
            return true;
        }
        if ("resetpassword".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.resetpassword")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            if (args.length < 2) {
                plugin.sendMessage(sender, "command.usage-resetpassword");
                return true;
            }
            String tempPassword = DigestUtil.randomToken(8);
            resolveProfile(args[1]).thenCompose(optional -> optional.isPresent()
                ? plugin.getCore().accounts().resetPassword(optional.get().getUniqueId(), tempPassword, issuer(sender))
                : CompletableFuture.completedFuture(OperationResult.failure("admin.user-not-found")))
                .whenComplete((result, throwable) -> runSync(() -> {
                    if (throwable != null) {
                        handleAsyncFailure(sender, "hlogin resetpassword", throwable);
                        return;
                    }
                    plugin.sendMessage(sender, result.getMessageKey());
                    if (result.isSuccess()) {
                        sender.sendMessage("Temporary password: " + tempPassword);
                    }
                }));
            return true;
        }
        if ("setlang".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.setlang")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            if (args.length < 3) {
                plugin.sendMessage(sender, "command.usage-setlang");
                return true;
            }
            resolveProfile(args[1]).thenCompose(optional -> optional.isPresent()
                ? plugin.getCore().accounts().setLanguage(optional.get().getUniqueId(), args[2], issuer(sender))
                : CompletableFuture.completedFuture(OperationResult.failure("admin.user-not-found")))
                .whenComplete((result, throwable) -> runSync(() -> {
                    if (throwable != null) {
                        handleAsyncFailure(sender, "hlogin setlang", throwable);
                        return;
                    }
                    plugin.sendMessage(sender, result.getMessageKey());
                }));
            return true;
        }
        if ("forcecaptcha".equals(subCommand)) {
            if (!sender.hasPermission("hlogin.forcecaptcha")) {
                plugin.sendMessage(sender, "general.no-permission");
                return true;
            }
            if (args.length < 2) {
                plugin.sendMessage(sender, "command.usage-forcecaptcha");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.sendMessage(sender, "admin.user-not-online");
                return true;
            }
            plugin.getCore().authentication().forceCaptcha(target).whenComplete((result, throwable) -> runSync(() -> {
                if (throwable != null) {
                    handleAsyncFailure(sender, "hlogin forcecaptcha", throwable);
                    return;
                }
                plugin.handleAuthResult(target, result, "captcha");
                plugin.sendMessage(sender, "admin.forcecaptcha-success");
            }));
            return true;
        }
        plugin.sendList(sender, "command.admin-help");
        return true;
    }

    private void sendUserInfo(CommandSender sender, String name, Optional<AccountProfile> optional) {
        if (!optional.isPresent()) {
            plugin.sendMessage(sender, "admin.user-not-found");
            return;
        }
        AccountProfile profile = optional.get();
        sender.sendMessage(plugin.translate(sender, "admin.user-header", name));
        sender.sendMessage(plugin.translate(sender, "admin.user-line", "UUID", profile.getUniqueId()));
        sender.sendMessage(plugin.translate(sender, "admin.user-line", "Language", profile.getLanguage()));
        sender.sendMessage(plugin.translate(sender, "admin.user-line", "Total logins", profile.getTotalLogins()));
        sender.sendMessage(plugin.translate(sender, "admin.user-line", "Last login", profile.getLastLogin()));
        sender.sendMessage(plugin.translate(sender, "admin.user-line", "Last IP", profile.getLastLoginIp()));
    }

    private CompletableFuture<Optional<AccountProfile>> resolveProfile(String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target != null) {
            return plugin.getCore().accounts().findByUniqueId(target.getUniqueId());
        }
        return plugin.getCore().accounts().findByName(name);
    }

    private UUID issuer(CommandSender sender) {
        return sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    }

    private void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private void handleAsyncFailure(CommandSender sender, String context, Throwable throwable) {
        Throwable cause = unwrap(throwable);
        plugin.getLogger().severe("Async operation failed (" + context + "): " + cause.getMessage());
        cause.printStackTrace();
        plugin.sendMessage(sender, "general.unexpected-error");
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException || current instanceof ExecutionException) {
            if (current.getCause() == null) {
                break;
            }
            current = current.getCause();
        }
        return current;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!"hlogin".equalsIgnoreCase(command.getName())) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(Arrays.asList("help", "reload", "status", "version", "user", "unregister", "resetpassword", "setlang", "forcecaptcha", "debug"), args[0]);
        }
        if (args.length == 2 && Arrays.asList("user", "unregister", "resetpassword", "setlang", "forcecaptcha").contains(args[0].toLowerCase())) {
            List<String> players = new ArrayList<String>();
            for (Player online : BukkitCompat.onlinePlayers()) {
                players.add(online.getName());
            }
            return filter(players, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String input) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(value);
            }
        }
        return result;
    }
}
