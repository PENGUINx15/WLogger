package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.penguinx13.wapi.Managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholders extends PlaceholderExpansion {

    private final WLogger plugin;
    private final ConfigManager configManager;

    public Placeholders(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wlogger";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PENGUINx13";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player != null && params.equalsIgnoreCase("broken")) {
            return String.valueOf(plugin.getDataManager().getBrokenBlocks(player.getName()));
        }
        if (player != null && params.equalsIgnoreCase("cm")) {
            return String.valueOf(plugin.getDataManager().getCostMultiplier(player.getName()));
        }
        if (player != null && params.equals("money")) {
            double rewardPerBlock = configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D);
            double costMultiplier = plugin.getDataManager().getCostMultiplier(player.getName());
            double totalReward = plugin.getDataManager().getBrokenBlocks(player.getName()) * rewardPerBlock * costMultiplier;
            return String.valueOf(totalReward);
        }

        if (player != null && params.equals("backpack")) {
            return String.valueOf(plugin.getDataManager().getBackpack(player.getName()));
        }

        if (player != null && params.equals("cm")) {
            return String.valueOf(plugin.getDataManager().getCostMultiplier(player.getName()));
        }

        if (params.startsWith("reward_")) {
            return String.valueOf(configManager.getConfig("config.yml").getDouble("tree.reward", 3.0D));
        }

        return null;
    }
}
