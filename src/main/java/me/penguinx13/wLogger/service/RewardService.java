package me.penguinx13.wLogger.service;

import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wapi.managers.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.penguinx13.wLogger.util.RewardCalculator;

public final class RewardService {
    private final WLogger plugin;
    private final ConfigManager configManager;

    public RewardService(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public ClaimResult claim(Player player) {
        if (brokenBlocks <= 0) {
            return ClaimResult.nothing();
        }

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
        double totalReward = RewardCalculator.calculate(brokenBlocks, rewardPerBlock, playerStateService.getCostMultiplier(player));

        economy.depositPlayer(player, totalReward);
        return ClaimResult.success(brokenBlocks, totalReward);
    }

    public double calculatePotentialReward(Player player) {
        double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
        return RewardCalculator.calculate(playerStateService.getBrokenBlocks(player), rewardPerBlock, playerStateService.getCostMultiplier(player));
    }

    public record ClaimResult(boolean success, int blocks, double reward) {
        public static ClaimResult nothing() {
            return new ClaimResult(false, 0, 0.0D);
        }

        public static ClaimResult success(int blocks, double reward) {
            return new ClaimResult(true, blocks, reward);
        }
    }
}
