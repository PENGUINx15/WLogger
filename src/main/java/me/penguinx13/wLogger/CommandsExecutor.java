package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import me.penguinx13.wapi.Managers.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandsExecutor implements CommandExecutor, TabCompleter {
    private final WLogger plugin;
    private final ConfigManager configManager;

    public CommandsExecutor(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(msg("command.onlyPlayers"));
                return true;
            }

            int brokenBlocks = plugin.getDataManager().getBrokenBlocks(player.getName());
            if (brokenBlocks <= 0) {
                MessageManager.sendMessage(player, msg("command.claim.nothingToClaim"));
                return true;
            }

            var economyRegistration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyRegistration == null || economyRegistration.getProvider() == null) {
                MessageManager.sendMessage(player, msg("command.claim.economyUnavailable"));
                return true;
            }

            Economy economy = economyRegistration.getProvider();
            double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
            double costMultiplier = plugin.getDataManager().getCostMultiplier(player.getName());
            double totalReward = brokenBlocks * rewardPerBlock * costMultiplier;

            economy.depositPlayer(player, totalReward);
            plugin.getDataManager().setBrokenBlocks(player.getName(), 0);

            MessageManager.sendMessage(player, formatMessage("command.claim.success", Map.of(
                    "blocks", String.valueOf(brokenBlocks),
                    "reward", String.format("%.2f", totalReward)
            )));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("wlogger.admin")) {
                sender.sendMessage(msg("command.noPermission"));
                return true;
            }

            configManager.registerConfig("config.yml");
            configManager.registerConfig("messeges.yml");
            sender.sendMessage(msg("command.reload.success"));
            return true;
        }

        if (isOperation(args[0])) {
            if (!sender.hasPermission("wlogger.admin")) {
                sender.sendMessage(msg("command.noPermission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(msg("command.usage.operation"));
                return true;
            }

            String operation = args[0].toLowerCase();
            String target = args[1].toLowerCase();

            if (target.equals("backpack")) {
                Integer value = parseInt(args[2]);
                if (value == null) {
                    sender.sendMessage(msg("command.valueMustBeInteger"));
                    return true;
                }

                int current = plugin.getDataManager().getBackpack("server-default");
                int updated = applyOperation(current, value, operation);
                if (updated < 1) {
                    sender.sendMessage(msg("command.backpackMin"));
                    return true;
                }

                plugin.getDataManager().setBackPack("server-default", updated);
                sender.sendMessage(formatMessage("command.backpackUpdated", Map.of("value", String.valueOf(updated))));
                return true;
            }

            if (target.equals("costmultiplier") || target.equals("cosmultipler")) {
                Double value = parseDouble(args[2]);
                if (value == null) {
                    sender.sendMessage(msg("command.valueMustBeNumber"));
                    return true;
                }

                double current = plugin.getDataManager().getCostMultiplier("server-default");
                double updated = applyOperation(current, value, operation);
                if (updated <= 0.0D) {
                    sender.sendMessage(msg("command.costMultiplierMin"));
                    return true;
                }

                plugin.getDataManager().setCostMultiplier("server-default", updated);
                sender.sendMessage(formatMessage("command.costMultiplierUpdated", Map.of("value", String.format("%.2f", updated))));
                return true;
            }

            sender.sendMessage(msg("command.invalidParameter"));
            return true;
        }

        sender.sendMessage(msg("command.unknownSubcommand"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            addIfMatches(suggestions, args[0], "claim");
            if (sender.hasPermission("wlogger.admin")) {
                addIfMatches(suggestions, args[0], "reload");
                addIfMatches(suggestions, args[0], "set");
                addIfMatches(suggestions, args[0], "add");
                addIfMatches(suggestions, args[0], "rem");
            }
            return suggestions;
        }

        if (args.length == 2 && isOperation(args[0])) {
            addIfMatches(suggestions, args[1], "backpack");
            addIfMatches(suggestions, args[1], "costmultiplier");
            addIfMatches(suggestions, args[1], "cosmultipler");
            return suggestions;
        }

        return suggestions;
    }

    private boolean isOperation(String value) {
        return value.equalsIgnoreCase("set") || value.equalsIgnoreCase("add") || value.equalsIgnoreCase("rem");
    }

    private void addIfMatches(List<String> list, String input, String value) {
        if (value.toLowerCase().startsWith(input.toLowerCase())) {
            list.add(value);
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

    private void sendUsage(CommandSender sender) {
        for (String line : configManager.getConfig("messeges.yml").getStringList("command.usage.main")) {
            sender.sendMessage(line);
        }
    }

    private String msg(String path) {
        return configManager.getConfig("messeges.yml").getString(path, path);
    }
    private String formatMessage(String path, Map<String, String> placeholders) {
        String message = msg(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
}
