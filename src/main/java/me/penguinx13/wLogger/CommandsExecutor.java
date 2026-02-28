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
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            int brokenBlocks = plugin.getDataManager().getBrokenBlocks(player.getName());
            if (brokenBlocks <= 0) {
                MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Нечего сдавать.");
                return true;
            }

            var economyRegistration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyRegistration == null || economyRegistration.getProvider() == null) {
                MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Экономика недоступна, обратитесь к администрации.");
                return true;
            }

            Economy economy = economyRegistration.getProvider();
            double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
            double costMultiplier = plugin.getDataManager().getCostMultiplier(player.getName());
            double totalReward = brokenBlocks * rewardPerBlock * costMultiplier;

            economy.depositPlayer(player, totalReward);
            plugin.getDataManager().setBrokenBlocks(player.getName(), 0);

            MessageManager.sendMessage(
                    player,
                    "{message}&7[&6&lЛесорубка&7]&f Вы сдали &6" + brokenBlocks + "&f блоков и получили &6" +
                            String.format("%.2f", totalReward)
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("wlogger.admin")) {
                sender.sendMessage("§cУ вас нет прав.");
                return true;
            }

            configManager.registerConfig("config.yml");
            sender.sendMessage("§aКонфиг WLogger перезагружен.");
            return true;
        }

        if (isOperation(args[0])) {
            if (!sender.hasPermission("wlogger.admin")) {
                sender.sendMessage("§cУ вас нет прав.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§eИспользование: /wlogger <set|add|rem> <backpack|costmultiplier> <значение>");
                return true;
            }

            String operation = args[0].toLowerCase();
            String target = args[1].toLowerCase();

            if (target.equals("backpack")) {
                Integer value = parseInt(args[2]);
                if (value == null) {
                    sender.sendMessage("§cЗначение должно быть целым числом.");
                    return true;
                }

                int current = plugin.getDataManager().getBackpack("server-default");
                int updated = applyOperation(current, value, operation);
                if (updated < 1) {
                    sender.sendMessage("§cЗначение backpack не может быть меньше 1.");
                    return true;
                }

                plugin.getDataManager().setBackPack("server-default", updated);
                sender.sendMessage("§aУстановлено server-default backpack: " + updated);
                return true;
            }

            if (target.equals("costmultiplier") || target.equals("cosmultipler")) {
                Double value = parseDouble(args[2]);
                if (value == null) {
                    sender.sendMessage("§cЗначение должно быть числом.");
                    return true;
                }

                double current = plugin.getDataManager().getCostMultiplier("server-default");
                double updated = applyOperation(current, value, operation);
                if (updated <= 0.0D) {
                    sender.sendMessage("§cЗначение costmultiplier должно быть больше 0.");
                    return true;
                }

                plugin.getDataManager().setCostMultiplier("server-default", updated);
                sender.sendMessage("§aУстановлено server-default costmultiplier: " + String.format("%.2f", updated));
                return true;
            }

            sender.sendMessage("§cПараметр должен быть backpack или costmultiplier.");
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда.");
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
        sender.sendMessage("§e/wlogger claim");
        sender.sendMessage("§e/wlogger reload");
        sender.sendMessage("§e/wlogger <set|add|rem> <backpack|costmultiplier> <значение>");
    }
}