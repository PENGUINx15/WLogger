package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    private final WLogger plugin;

    public Placeholders(WLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wlogger";
    }

    @Override
    public @NotNull String getAuthor() {
        return "penguin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params) {
            case "money" -> {
                return String.valueOf(plugin.getCurrencyCount(player));
            }
            case "backpack" -> {
                return String.valueOf(plugin.getBackpackSize(player));
            }
            case "cm" -> {
                return String.valueOf(plugin.getCostMultiplier(player));
            }
            case "broken" -> {
                return String.valueOf(plugin.getBlocksBroken(player));
            }
        }
        if (params.startsWith("reward_")) {
        	String blockTypeName = params.substring("reward_".length());
        	Material blockType = Material.matchMaterial(blockTypeName);
            return String.valueOf(plugin.getBlockReward(player, blockType));
        }
        return null;
    }
}