package me.penguinx13.wLogger;

import me.penguinx13.wLogger.command.Commands;
import me.penguinx13.wLogger.config.PluginLifecycleService;
import me.penguinx13.wLogger.data.repository.DataManager;
import me.penguinx13.wLogger.listener.BlockBreakListener;
import me.penguinx13.wLogger.service.AdminPlayerStateService;
import me.penguinx13.wLogger.service.PlayerStateService;
import me.penguinx13.wLogger.service.RewardService;
import me.penguinx13.wLogger.service.TreeHarvestService;
import me.penguinx13.wapi.commands.integration.CommandFrameworkBootstrap;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.SQLiteManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class WLogger extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private PlayerStateService playerStateService;
    private RewardService rewardService;

    public PlayerStateService getPlayerStateService() {
        return playerStateService;
    }

    public RewardService getRewardService() {
        return rewardService;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.registerConfig("config.yml");
        configManager.registerConfig("messages.yml");

        if (getServer().getServicesManager().getRegistration(Economy.class) == null) {
            getLogger().severe(configManager.getConfig("messages.yml").getString("log.economyMissing", "log.economyMissing"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dataManager = new SQLiteManager(this, "players.db");
        dataManager.initialize().join();

        playerStateService = new PlayerStateService(this, dataManager);
        int defaultBackpack = configManager.getConfig("config.yml").getInt("defaultValues.backpack", 50);
        double defaultCostMultiplier = configManager.getConfig("config.yml").getDouble("defaultValues.costmultiplier", 1.0D);
        playerStateService.initialize(defaultBackpack, defaultCostMultiplier);

        TreeHarvestService treeHarvestService = new TreeHarvestService(this, configManager, playerStateService);
        if (!treeHarvestService.validateRegionConfiguration()) {
            throw new IllegalStateException("Invalid region configuration: location.min.world and location.max.world must match.");
        }

        rewardService = new RewardService(this, configManager, playerStateService);
        AdminPlayerStateService adminPlayerStateService = new AdminPlayerStateService(playerStateService);
        PluginLifecycleService pluginLifecycleService = new PluginLifecycleService(configManager, playerStateService, treeHarvestService);

        String progress = configManager.getConfig("messages.yml").getString("break.progress", "break.progress");
        String completed = configManager.getConfig("messages.yml").getString("break.completed", "break.completed");
        String backpackFull = configManager.getConfig("messages.yml").getString("break.backpackFull", "break.backpackFull");
        getServer().getPluginManager().registerEvents(new BlockBreakListener(treeHarvestService, progress, completed, backpackFull), this);

        new CommandFrameworkBootstrap(this).register(new Commands(configManager, rewardService, adminPlayerStateService, pluginLifecycleService));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
            getLogger().info(configManager.getConfig("messages.yml").getString("log.placeholdersRegistered", "log.placeholdersRegistered"));
        }
    }

    @Override
    public void onDisable() {
        if (playerStateService != null) {
            playerStateService.flushAndShutdown();
        }
    }
}
