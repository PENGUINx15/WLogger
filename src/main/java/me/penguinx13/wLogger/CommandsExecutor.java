package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import me.penguinx13.wapi.Managers.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandsExecutor implements CommandExecutor {
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

            Economy economy = plugin.getServer().getServicesManager().getRegistration(Economy.class).getProvider();

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

        if (args[0].equalsIgnoreCase("backpack") || args[0].equalsIgnoreCase("costmultiplier") || args[0].equalsIgnoreCase("cosmultipler")) {
            if (!sender.hasPermission("wlogger.admin")) {
                sender.sendMessage("§cУ вас нет прав.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§eИспользование: /wlogger " + args[0].toLowerCase() + " <set|add|rem> <значение>");
                return true;
            }

            String target = args[0].toLowerCase();
            String operation = args[1].toLowerCase();

            if (target.equals("backpack")) {
                Integer value = parseInt(args[2]);
                if (value == null) {
                    sender.sendMessage("§cЗначение должно быть целым числом.");
                    return true;
                }

                int current = plugin.getDataManager().getBackpack("server-default");
                int updated = applyOperation(current, value, operation);
                if (updated == Integer.MIN_VALUE) {
                    sender.sendMessage("§cОперация должна быть set, add или rem.");
                    return true;
                }

                if (updated < 1) {
                    sender.sendMessage("§cЗначение backpack не может быть меньше 1.");
                    return true;
                }

                plugin.getDataManager().setBackPack("server-default", updated);
                sender.sendMessage("§aУстановлено server-default backpack: " + updated);
                return true;
            }

            Double value = parseDouble(args[2]);
            if (value == null) {
                sender.sendMessage("§cЗначение должно быть числом.");
                return true;
            }

            double current = plugin.getDataManager().getCostMultiplier("server-default");
            double updated = applyOperation(current, value, operation);
            if (Double.isNaN(updated)) {
                sender.sendMessage("§cОперация должна быть set, add или rem.");
                return true;
            }

            if (updated <= 0.0D) {
                sender.sendMessage("§cЗначение costmultiplier должно быть больше 0.");
                return true;
            }

            plugin.getDataManager().setCostMultiplier("server-default", updated);
            sender.sendMessage("§aУстановлено server-default costmultiplier: " + String.format("%.2f", updated));
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда.");
        return true;
    }

    private int applyOperation(int current, int value, String operation) {
        return switch (operation) {
            case "set" -> value;
            case "add" -> current + value;
            case "rem" -> current - value;
            default -> Integer.MIN_VALUE;
        };
    }

    private double applyOperation(double current, double value, String operation) {
        return switch (operation) {
            case "set" -> value;
            case "add" -> current + value;
            case "rem" -> current - value;
            default -> Double.NaN;
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
        sender.sendMessage("§e/wlogger backpack <set|add|rem> <значение>");
        sender.sendMessage("§e/wlogger costmultiplier <set|add|rem> <значение>");
    }
}
