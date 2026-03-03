package me.penguinx13.wLogger.commands;

import me.penguinx13.wLogger.DataManager;
import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@RootCommand("wlogger")
public class Commands {
    private final WLogger plugin;
    private final ConfigManager configManager;

    public Commands(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @SubCommand("")
    public void root(CommandSender sender) {
        sendUsage(sender);
    }

    @SubCommand(value = "claim", playerOnly = true)
    public void claim(Player player) {
        int brokenBlocks = plugin.getDataManager().getBrokenBlocks(player.getName());
        if (brokenBlocks <= 0) {
            MessageManager.sendMessage(player, msg("command.claim.nothingToClaim"));
            return;
        }

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();

        double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
        double costMultiplier = plugin.getDataManager().getCostMultiplier(player.getName());
        double totalReward = brokenBlocks * rewardPerBlock * costMultiplier;

        economy.depositPlayer(player, totalReward);
        plugin.getDataManager().setBrokenBlocks(player.getName(), 0);

        MessageManager.sendMessage(player,msg("command.claim.success"), Map.of(
                "blocks", String.valueOf(brokenBlocks),
                "reward", String.format("%.2f", totalReward) ));
    }

    @SubCommand(value = "reload", permission = "wlogger.admin")
    public void reload(CommandSender sender) {
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
        MessageManager.sendMessage((Player) sender, msg("command.reload.success"));
    }

    @SubCommand(value = "set", permission = "wlogger.admin")
    public void set(CommandSender sender, @Arg("parameter") String parameter, @Arg("target")Player target, @Arg("value") String value) {
        updateDefaultValue(sender, "set", parameter, target, value);
    }

    @SubCommand(value = "add", permission = "wlogger.admin")
    public void add(CommandSender sender, @Arg("parameter") String parameter, @Arg("value") String value) {
        //updateDefaultValue(sender, "add", parameter, value);
    }

    @SubCommand(value = "rem", permission = "wlogger.admin")
    public void rem(CommandSender sender, @Arg("parameter") String parameter, @Arg("value") String value) {
        //updateDefaultValue(sender, "rem", parameter, value);
    }

    private void updateDefaultValue(CommandSender sender, String operation, String parameter, Player target, String rawValue) {
        if (parameter.toLowerCase().equals("backpack")) {
            Integer value = parseInt(rawValue);
            if (value == null) {
                MessageManager.sendMessage((Player) sender, msg("command.valueMustBeInteger"));
                return;
            }

            int current = plugin.getDataManager().getBackpack(target.getName());
            int updated = applyOperation(current, value, operation);
            if (updated < 1) {
                MessageManager.sendMessage((Player) sender, msg("command.backpackMin"));
                return;
            }

            plugin.getDataManager().setBackPack(target.getName(), updated);
            MessageManager.sendMessage((Player) sender, msg("command.backpackUpdated"), Map.of(
                    "value", String.valueOf(updated)));
            return;
        }

        if (parameter.toLowerCase().equals("costmultiplier")) {
            Double value = parseDouble(rawValue);
            if (value == null) {
                MessageManager.sendMessage((Player) sender, msg("command.valueMustBeNumber"));
                return;
            }

            double current = plugin.getDataManager().getCostMultiplier(target.getName());
            double updated = applyOperation(current, value, operation);
            if (updated <= 0.0D) {
                MessageManager.sendMessage((Player) sender, msg("command.costMultiplierMin"));
                return;
            }

            plugin.getDataManager().setCostMultiplier(target.getName(), updated);
            MessageManager.sendMessage((Player) sender, msg("command.costMultiplierUpdated"), Map.of(
                    "value", String.format("%.2f", updated)));
            return;
        }
        MessageManager.sendMessage((Player) sender, msg("command.invalidParameter"));
    }

    private void sendUsage(CommandSender sender) {
        List<String> usageLines = configManager.getConfig("messeges.yml").getStringList("command.usage.main");
        if (usageLines.isEmpty()) {
            MessageManager.sendMessage((Player) sender, msg("command.usage.operation"));
            return;
        }

        for (String line : usageLines) {
            MessageManager.sendMessage((Player) sender, line);
        }
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
