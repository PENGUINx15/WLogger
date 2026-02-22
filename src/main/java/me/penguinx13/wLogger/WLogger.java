package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import me.penguinx13.wapi.Managers.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class WLogger extends JavaPlugin {
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.registerConfig("config.yml");

        sqliteManager = new SQLiteManager(this, "players.db");
        sqliteManager.connect();

        sqliteManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (" +
                        "playerName TEXT PRIMARY KEY," +
                        "backpack INTEGER NOT NULL DEFAULT 50," +
                        "costmultiplier DOUBLE NOT NULL DEFAULT 1.0," +
                        "brokenBlocks INTEGER NOT NULL DEFAULT 0" +
                        ")"
        );

        BlockBreakListener blockBreakListener = new BlockBreakListener(configManager, this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);

        Objects.requireNonNull(getCommand("wlogger"), "Command wlogger is not defined in plugin.yml")
                .setExecutor(new CommandsExecutor(this, configManager));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
            getLogger().info("Плейсхолдеры WLogger зарегистрированы успешно!");
        }
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null) {
            sqliteManager.disconnect();
        }
    }
}
