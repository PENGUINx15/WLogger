package me.penguinx13.wLogger;

import me.penguinx13.wLogger.command.Commands;
import me.penguinx13.wLogger.data.DataManager;
import me.penguinx13.wLogger.listener.BlockBreakListener;
import me.penguinx13.wLogger.service.TreeHarvestService;
import me.penguinx13.wapi.commands.core.pipeline.*;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.core.resolver.DefaultResolvers;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.runtime.NoopMetricsSink;
import me.penguinx13.wapi.commands.core.validation.ValidationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;
import me.penguinx13.wapi.commands.paper.platform.*;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.orm.Repository;
import me.penguinx13.wapi.orm.SQLiteManager;
import me.penguinx13.wapi.orm.SimpleORM;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
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


        TreeHarvestService treeHarvestService = new TreeHarvestService(this, configManager);
        if (!treeHarvestService.validateRegionConfiguration()) {
            throw new IllegalStateException("Invalid region configuration: location.min.world and location.max.world must match.");
        }

        String progress = configManager.getConfig("messages.yml").getString("break.progress", "break.progress");
        String completed = configManager.getConfig("messages.yml").getString("break.completed", "break.completed");
        String backpackFull = configManager.getConfig("messages.yml").getString("break.backpackFull", "break.backpackFull");
        getServer().getPluginManager().registerEvents(new BlockBreakListener(treeHarvestService, progress, completed, backpackFull), this);

        CommandRegistrationService registrationService = new CommandRegistrationService();
        registrationService.register(new Commands(this, configManager, repository));

        ResolverRegistry resolverRegistry = new ResolverRegistry();
        DefaultResolvers.registerDefaults(resolverRegistry);
        resolverRegistry.register(new PaperPlayerResolver());

        ValidationService validationService = new ValidationService();
        PaperPlatformBridge bridge = new PaperPlatformBridge(new PaperScheduler(this));

        CommandRuntime runtime = new CommandRuntime(
                registrationService.buildTree(),
                new CommandPipeline(List.of(
                        new RoutingStage(),
                        new ArgumentParsingStage(),
                        new ValidationStage(),
                        new AuthorizationStage(),
                        new InvocationStage(),
                        new PostProcessingStage()
                )),
                resolverRegistry,
                validationService,
                new DefaultErrorPresenter(new PaperLogger(getLogger())),
                List.of(),
                bridge,
                new NoopMetricsSink()
        );

        new PaperCommandBinder(this, bridge).bind(runtime);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, repository).register();
            getLogger().info(configManager.getConfig("messages.yml").getString("log.placeholdersRegistered", "log.placeholdersRegistered"));
        }
    }

    @Override
    public void onDisable() {
    }
}
