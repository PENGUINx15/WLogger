package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            return String.valueOf(plugin.getDataManager().getBrokenBlocks(player.getName()));
        }

        return null;
    }
}
