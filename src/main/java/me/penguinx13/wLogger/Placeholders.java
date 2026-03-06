package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.penguinx13.wLogger.data.DataManager;
import me.penguinx13.wapi.orm.Repository;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletionException;

public class Placeholders extends PlaceholderExpansion {

    private final WLogger plugin;
    private final Repository<DataManager, UUID> repository;

    public Placeholders(WLogger plugin, Repository<DataManager, UUID> repository) {
        this.plugin = plugin;
        this.repository = repository;
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
        if (params.equalsIgnoreCase("status")) {
            return "enabled";
        }

        if (player == null) {
            return null;
        }

        DataManager data;
        try {
            data = repository.findByIdAsync(player.getUniqueId())
                    .thenApply(existing -> existing.orElseGet(() -> new DataManager(player.getUniqueId())))
                    .join();
        } catch (CompletionException exception) {
            plugin.getLogger().warning("Failed to load placeholder data for " + player.getName() + ": " + exception.getMessage());
            return "0";
        }

        if (params.equalsIgnoreCase("brokenblocks")) {
            return String.valueOf(data.getBrokenBlocks());
        }

        if (params.equalsIgnoreCase("claim")) {
            return String.valueOf(data.getBrokenBlocks() * data.getCostMultiplier() * plugin.getConfigManager().getConfig("config.yml").getDouble("tree.reward", 3.0D));
        }

        if (params.equalsIgnoreCase("backpack")) {
            return String.valueOf(data.getBackpack());
        }

        if (params.equalsIgnoreCase("cm")) {
            return String.valueOf(data.getCostMultiplier());
        }

        if (params.equalsIgnoreCase("reward")) {
            return String.valueOf(plugin.getConfigManager().getConfig("config.yml").getDouble("tree.reward", 3.0D));
        }

        return null;
    }
}
