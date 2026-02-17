package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class WLogger extends JavaPlugin {
    private ConfigManager configManager;
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.registerConfig("config.yml");
        configManager.registerConfig("data.yml");

        BlockBreakListener blockBreakListener = new BlockBreakListener(configManager, this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);

        Objects.requireNonNull(getCommand("wlogger")).setExecutor(new CommandsExecutor(this, configManager));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
            getLogger().info("Плейсхолдеры WMine зареестрированы успешно!");
        }
    }

    @Override
    public void onDisable() {

    }
}
