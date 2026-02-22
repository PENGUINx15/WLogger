package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import me.penguinx13.wapi.Managers.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Использование: &6/wlogger claim");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            int brokenBlocks = plugin.getDataManager().getBrokenBlocks(player.getName());
            if (brokenBlocks <= 0) {
                MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Нечего сдавать.");
                return true;
            }

            Economy economy = getEconomy();
            if (economy == null) {
                MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Экономика недоступна.");
                return true;
            }

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

        MessageManager.sendMessage(player, "{message}&7[&6&lЛесорубка&7]&f Неизвестная подкоманда.");
        return true;
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        return registration != null ? registration.getProvider() : null;
    }
}
