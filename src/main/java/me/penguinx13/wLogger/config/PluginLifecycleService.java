package me.penguinx13.wLogger.config;

import me.penguinx13.wLogger.service.TreeHarvestService;
import me.penguinx13.wapi.managers.ConfigManager;

public final class PluginLifecycleService {
    private final ConfigManager configManager;
    private final PlayerStateService playerStateService;
    private final TreeHarvestService treeHarvestService;

    public PluginLifecycleService(ConfigManager configManager, PlayerStateService playerStateService, TreeHarvestService treeHarvestService) {
        this.configManager = configManager;
        this.playerStateService = playerStateService;
        this.treeHarvestService = treeHarvestService;
    }

    public boolean safeReload() {
        configManager.registerConfig("config.yml");
        configManager.registerConfig("messages.yml");

        int defaultBackpack = configManager.getConfig("config.yml").getInt("defaultValues.backpack", 50);
        double defaultCostMultiplier = configManager.getConfig("config.yml").getDouble("defaultValues.costmultiplier", 1.0D);
        playerStateService.setDefaults(new me.penguinx13.wLogger.data.model.ServerSettingsDTO(defaultBackpack, defaultCostMultiplier));

        return treeHarvestService.validateRegionConfiguration();
    }
}
