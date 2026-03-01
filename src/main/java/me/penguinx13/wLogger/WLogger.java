package me.penguinx13.wLogger;

import me.penguinx13.wapi.commandframework.core.CommandRegistry;
import me.penguinx13.wapi.managers.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

import static org.bukkit.Bukkit.getServicesManager;

public final class WLogger extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;

    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.registerConfig("config.yml");
        configManager.registerConfig("messeges.yml");

        if (getServicesManager().getRegistration(Economy.class) == null) {
            getLogger().severe(configManager.getConfig("messeges.yml").getString("log.economyMissing", "log.economyMissing"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        dataManager = new DataManager(this, configManager);
        dataManager.createTable();

        int defaultBackpack = configManager.getConfig("config.yml").getInt("defaultValues.backpack", 50);
        double defaultCostMultiplier = configManager.getConfig("config.yml").getDouble("defaultValues.costmultiplier", 1.0D);
        dataManager.ensureServerDefault(defaultBackpack, defaultCostMultiplier);

        BlockBreakListener blockBreakListener = new BlockBreakListener(configManager, this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);

        CommandRegistry commandRegistry = new CommandRegistry(this);
        commandRegistry.registerCommand(new CommandsExecutor(this, configManager));

//        CommandsExecutor commandsExecutor = new CommandsExecutor(this, configManager);
//        Objects.requireNonNull(getCommand("wlogger")).setExecutor(commandsExecutor);
//        Objects.requireNonNull(getCommand("wlogger")).setTabCompleter(commandsExecutor);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, configManager).register();
            getLogger().info(configManager.getConfig("messeges.yml").getString("log.placeholdersRegistered", "log.placeholdersRegistered"));
        }
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.disconnect();
        }
    }

}
