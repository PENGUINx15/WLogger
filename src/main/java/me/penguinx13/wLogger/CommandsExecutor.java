package me.penguinx13.wLogger;

import me.penguinx13.wapi.commandframework.annotations.Arg;
import me.penguinx13.wapi.commandframework.annotations.RootCommand;
import me.penguinx13.wapi.commandframework.annotations.SubCommand;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;
import java.util.Map;

@RootCommand("wlogger")
public class CommandsExecutor {
    private final WLogger plugin;
    private final ConfigManager configManager;

    public CommandsExecutor(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @SubCommand("")
    public void root(CommandSender sender) {
        sendUsage(sender);
    }

    @SubCommand("claim")
    public void claim(Player player) {
        int brokenBlocks = plugin.getDataManager().getBrokenBlocks(player.getName());
        if (brokenBlocks <= 0) {
            send(player, msg("command.claim.nothingToClaim"));
            return;
        }

        Economy economy = getEconomy();
        if (economy == null) {
            send(player, msg("command.claim.economyUnavailable"));
            return;
        }

        double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
        double costMultiplier = plugin.getDataManager().getCostMultiplier(player.getName());
        double totalReward = brokenBlocks * rewardPerBlock * costMultiplier;

        economy.depositPlayer(player, totalReward);
        plugin.getDataManager().setBrokenBlocks(player.getName(), 0);

        send(player, MessageManager.applyTemplate(msg("command.claim.success"), Map.of(
                "blocks", String.valueOf(brokenBlocks),
                "reward", String.format("%.2f", totalReward)
        )));
    }

    @SubCommand(value = "reload", permission = "wlogger.admin")
    public void reload(CommandSender sender) {
        configManager.registerConfig("config.yml");
        configManager.registerConfig("messeges.yml");
        send(sender, msg("command.reload.success"));
    }

    @SubCommand(value = "set", permission = "wlogger.admin")
    public void set(CommandSender sender, @Arg("parameter") String parameter, @Arg("value") String value) {
        updateDefaultValue(sender, "set", parameter, value);
    }

    @SubCommand(value = "add", permission = "wlogger.admin")
    public void add(CommandSender sender, @Arg("parameter") String parameter, @Arg("value") String value) {
        updateDefaultValue(sender, "add", parameter, value);
    }

    @SubCommand(value = "rem", permission = "wlogger.admin")
    public void rem(CommandSender sender, @Arg("parameter") String parameter, @Arg("value") String value) {
        updateDefaultValue(sender, "rem", parameter, value);
    }

    private void updateDefaultValue(CommandSender sender, String operation, String parameter, String rawValue) {
        String normalizedParameter = parameter.toLowerCase();

        if (normalizedParameter.equals("backpack")) {
            Integer value = parseInt(rawValue);
            if (value == null) {
                send(sender, msg("command.valueMustBeInteger"));
                return;
            }

            int current = plugin.getDataManager().getBackpack("server-default");
            int updated = applyOperation(current, value, operation);
            if (updated < 1) {
                send(sender, msg("command.backpackMin"));
                return;
            }

            plugin.getDataManager().setBackPack("server-default", updated);
            send(sender, MessageManager.applyTemplate(msg("command.backpackUpdated"), Map.of("value", String.valueOf(updated))));
            return;
        }

        if (normalizedParameter.equals("costmultiplier") || normalizedParameter.equals("cosmultipler")) {
            Double value = parseDouble(rawValue);
            if (value == null) {
                send(sender, msg("command.valueMustBeNumber"));
                return;
            }

            double current = plugin.getDataManager().getCostMultiplier("server-default");
            double updated = applyOperation(current, value, operation);
            if (updated <= 0.0D) {
                send(sender, msg("command.costMultiplierMin"));
                return;
            }

            plugin.getDataManager().setCostMultiplier("server-default", updated);
            send(sender, MessageManager.applyTemplate(msg("command.costMultiplierUpdated"), Map.of("value", String.format("%.2f", updated))));
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
        if (sender instanceof Player player) {
            MessageManager.sendMessage(player, message);
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{message}", "")));
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        return registration != null ? registration.getProvider() : null;
    }

    private int applyOperation(int current, int value, String operation) {
        return switch (operation) {
            case "set" -> value;
            case "add" -> current + value;
            case "rem" -> current - value;
            default -> current;
        };
    }

    private double applyOperation(double current, double value, String operation) {
        return switch (operation) {
            case "set" -> value;
            case "add" -> current + value;
            case "rem" -> current - value;
            default -> current;
        };
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
