package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.penguinx13.wLogger.data.DataManager;
import me.penguinx13.wapi.orm.Repository;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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

        if (player != null && params.equalsIgnoreCase("brokenblocks")) {
            return String.valueOf(
                    repository.findByIdAsync(player.getUniqueId())
                            .thenCompose(existing -> {
                                DataManager data = existing.orElseGet(() -> new DataManager(player.getUniqueId()));
                                return data.getBrokenBlocks();
                            })
            );
        }

        if (player != null && params.equals("claim")) {
            return String.valueOf(
                    repository.findByIdAsync(player.getUniqueId())
                            .thenCompose(existing -> {
                                DataManager data = existing.orElseGet(() -> new DataManager(player.getUniqueId()));
                                return data.getBrokenBlocks()*data.getCostMultiplier()*plugin.getConfigManager().getConfig("config.yml").getDouble("tree.reward", 3.0D);
                            })
            );
        }

        if (player != null && params.equals("backpack")) {
            return String.valueOf(
                    repository.findByIdAsync(player.getUniqueId())
                            .thenCompose(existing -> {
                                DataManager data = existing.orElseGet(() -> new DataManager(player.getUniqueId()));
                                return data.getBackpack();
                            })
            );
        }

        if (player != null && params.equals("cm")) {
            return String.valueOf(
                    repository.findByIdAsync(player.getUniqueId())
                            .thenCompose(existing -> {
                                DataManager data = existing.orElseGet(() -> new DataManager(player.getUniqueId()));
                                return data.getCostMultiplier();
                            })
            );
        }

        if (params.startsWith("reward")) {
            return String.valueOf(plugin.getConfigManager().getConfig("config.yml").getDouble("tree.reward", 3.0D));
        }

        return null;
    }
}
