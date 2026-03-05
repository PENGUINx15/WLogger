package me.penguinx13.wLogger;

import me.penguinx13.wLogger.command.Commands;
import me.penguinx13.wLogger.config.PluginLifecycleService;
import me.penguinx13.wLogger.data.DataManager;
import me.penguinx13.wLogger.listener.BlockBreakListener;
import me.penguinx13.wLogger.service.RewardService;
import me.penguinx13.wLogger.service.TreeHarvestService;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.orm.Repository;
import me.penguinx13.wapi.orm.SQLiteManager;
import me.penguinx13.wapi.orm.SimpleORM;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class WLogger extends JavaPlugin {
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private RewardService rewardService;


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

        sqliteManager = new SQLiteManager(getDataFolder(), "example.db", Bukkit::isPrimaryThread);

        SimpleORM orm = new SimpleORM(sqliteManager);
        orm.registerEntity(DataManager.class);
        Repository<DataManager, UUID> repository = orm.getRepository(DataManager.class);


        TreeHarvestService treeHarvestService = new TreeHarvestService(this, configManager, playerStateService);
        if (!treeHarvestService.validateRegionConfiguration()) {
            throw new IllegalStateException("Invalid region configuration: location.min.world and location.max.world must match.");
        }

        rewardService = new RewardService(this, configManager, playerStateService);
        PluginLifecycleService pluginLifecycleService = new PluginLifecycleService(configManager, playerStateService, treeHarvestService);

        String progress = configManager.getConfig("messages.yml").getString("break.progress", "break.progress");
        String completed = configManager.getConfig("messages.yml").getString("break.completed", "break.completed");
        String backpackFull = configManager.getConfig("messages.yml").getString("break.backpackFull", "break.backpackFull");
        getServer().getPluginManager().registerEvents(new BlockBreakListener(treeHarvestService, progress, completed, backpackFull), this);

        new CommandFrameworkBootstrap(this).register(new Commands(configManager, rewardService, pluginLifecycleService));

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
