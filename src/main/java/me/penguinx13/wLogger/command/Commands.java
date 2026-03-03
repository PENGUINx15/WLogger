package me.penguinx13.wLogger.command;

import me.penguinx13.wLogger.config.PluginLifecycleService;
import me.penguinx13.wLogger.service.AdminPlayerStateService;
import me.penguinx13.wLogger.service.RewardService;
import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@RootCommand("wlogger")
public final class Commands {
    private final ConfigManager configManager;
    private final RewardService rewardService;
    private final AdminPlayerStateService adminPlayerStateService;
    private final PluginLifecycleService lifecycleService;

    public Commands(
            ConfigManager configManager,
            RewardService rewardService,
            AdminPlayerStateService adminPlayerStateService,
            PluginLifecycleService lifecycleService
    ) {
        this.configManager = configManager;
        this.rewardService = rewardService;
        this.adminPlayerStateService = adminPlayerStateService;
        this.lifecycleService = lifecycleService;
    }

    @SubCommand("")
    public void root(CommandSender sender) {
        sendUsage(sender);
    }

    @SubCommand(value = "claim", playerOnly = true)
    public void claim(Player player) {
        RewardService.ClaimResult claimResult = rewardService.claim(player);
        if (!claimResult.success()) {
            MessageManager.sendMessage(player, msg("command.claim.nothingToClaim"));
            return;
        }

        MessageManager.sendMessage(player, msg("command.claim.success"), Map.of(
                "blocks", String.valueOf(claimResult.blocks()),
                "reward", String.format("%.2f", claimResult.reward())
        ));
    }

    @SubCommand(value = "reload", permission = "wlogger.admin")
    public void reload(CommandSender sender) {
        boolean regionOk = lifecycleService.safeReload();
        send(sender, msg("command.reload.success"));
        if (!regionOk) {
            send(sender, msg("log.regionConfigMismatch"));
        }
    }

    @SubCommand(value = "set", permission = "wlogger.admin")
    public void set(CommandSender sender, @Arg("parameter") String parameter, @Arg("target") Player target, @Arg("value") String value) {
        processAdminUpdate(sender, AdminPlayerStateService.Operation.SET, parameter, target, value);
    }

    @SubCommand(value = "add", permission = "wlogger.admin")
    public void add(CommandSender sender, @Arg("parameter") String parameter, @Arg("target") Player target, @Arg("value") String value) {
        processAdminUpdate(sender, AdminPlayerStateService.Operation.ADD, parameter, target, value);
    }

    @SubCommand(value = "rem", permission = "wlogger.admin")
    public void rem(CommandSender sender, @Arg("parameter") String parameter, @Arg("target") Player target, @Arg("value") String value) {
        processAdminUpdate(sender, AdminPlayerStateService.Operation.REM, parameter, target, value);
    }

    private void processAdminUpdate(CommandSender sender, AdminPlayerStateService.Operation operation, String parameter, Player target, String rawValue) {
        if (parameter.equalsIgnoreCase("backpack")) {
            Integer value = parseInt(rawValue);
            if (value == null) {
                send(sender, msg("command.valueMustBeInteger"));
                return;
            }
            try {
                int updated = adminPlayerStateService.updateBackpack(target, operation, value);
                send(sender, format(msg("command.backpackUpdated"), Map.of("value", String.valueOf(updated))));
            } catch (IllegalArgumentException exception) {
                send(sender, msg("command.backpackMin"));
            }
            return;
        }

        if (parameter.equalsIgnoreCase("costmultiplier")) {
            Double value = parseDouble(rawValue);
            if (value == null) {
                send(sender, msg("command.valueMustBeNumber"));
                return;
            }
            try {
                double updated = adminPlayerStateService.updateCostMultiplier(target, operation, value);
                send(sender, format(msg("command.costMultiplierUpdated"), Map.of("value", String.format("%.2f", updated))));
            } catch (IllegalArgumentException exception) {
                send(sender, msg("command.costMultiplierMin"));
            }
            return;
        }

        send(sender, msg("command.invalidParameter"));
    }

    private void sendUsage(CommandSender sender) {
        List<String> usageLines = configManager.getConfig("messeges.yml").getStringList("command.usage.main");
        if (usageLines.isEmpty()) {
            send(sender, msg("command.usage.operation"));
            return;
        }

        for (String line : usageLines) {
            send(sender, line);
        }
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    private String format(String message, Map<String, String> placeholders) {
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private Integer parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String msg(String path) {
        return configManager.getConfig("messeges.yml").getString(path, path);
    }
}
