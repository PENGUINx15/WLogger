package me.penguinx13.wLogger.command;

import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wLogger.data.DataManager;
import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import me.penguinx13.wapi.orm.Repository;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RootCommand("wlogger")
public final class Commands {
    private final WLogger plugin;
    private final ConfigManager configManager;
    private final Repository<DataManager, UUID> repository;

    public Commands(
            WLogger plugin,
            ConfigManager configManager,
            Repository<DataManager, UUID> repository
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.repository = repository;
    }

    @SubCommand("")
    public void root(CommandSender sender) {
        sendUsage(sender);
    }

    @SubCommand(value = "claim", playerOnly = true)
    public void claim(Player player) {

        repository.findByIdAsync(player.getUniqueId())
                .thenCompose(existing -> {
                    DataManager data = existing.orElseGet(() -> new DataManager(player.getUniqueId()));
                    if (data.getBrokenBlocks() == 0) {
                        return repository.saveAsync(data)
                                .thenRun(() -> runSync(() ->
                                        MessageManager.sendMessage(player, msg("command.claim.nothingToClaim"))
                                ));
                    }
                    double reward = data.getBrokenBlocks() * data.getCostMultiplier() * configManager.getConfig("config.yml").getInt("tree.reward");
                    Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
                    economy.depositPlayer(player, reward);
                    return repository.saveAsync(data)
                            .thenRun(() -> runSync(() ->
                                    MessageManager.sendMessage(player, msg("command.claim.success"), Map.of(
                                            "blocks", String.valueOf(data.getBrokenBlocks()),
                                            "reward", String.format("%.2f", reward)
                                    ))));

                }
                );



    }

    @SubCommand(value = "set", permission = "wlogger.admin")
    public void set(CommandSender sender,
                    @Arg(value = "parameter", optional = true) String parameter,
                    @Arg("target") Player target,
                    @Arg("value") Integer value)
    {
        switch (parameter){
            case "backpack":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setBackpack(value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->
                                            MessageManager.sendMessage((Player) sender,msg("command.set"),
                                                    Map.of(
                                                    "parameter", parameter,
                                                    "target", target,
                                                    "value", value))
                                    ));});
            case "costmultiplier":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setBackpack(value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->MessageManager.sendMessage((Player) sender,msg("command.set"),
                                            Map.of(
                                                    "parameter", parameter,
                                                    "target", target,
                                                    "value", value))
                                    ));});
        }
    }

    @SubCommand(value = "add", permission = "wlogger.admin")
    public void add(CommandSender sender,
                    @Arg(value = "parameter", optional = true) String parameter,
                    @Arg("target") Player target,
                    @Arg("value") Integer value)
    {
        switch (parameter){
            case "backpack":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setBackpack(data.getBackpack() + value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->
                                            MessageManager.sendMessage((Player) sender,msg("command.add"),
                                                    Map.of(
                                                            "parameter", parameter,
                                                            "target", target,
                                                            "value", value))
                                    ));});
            case "costmultiplier":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setCostMultiplier(data.getCostMultiplier() + value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->MessageManager.sendMessage((Player) sender,msg("command.set"),
                                            Map.of(
                                                    "parameter", parameter,
                                                    "target", target,
                                                    "value", value))
                                    ));});
        }
    }

    @SubCommand(value = "rem", permission = "wlogger.admin")
    public void rem(CommandSender sender,
                    @Arg(value = "parameter", optional = true) String parameter,
                    @Arg("target") Player target,
                    @Arg("value") Integer value)
    {
        switch (parameter){
            case "backpack":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setBackpack(data.getBackpack() - value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->
                                            MessageManager.sendMessage((Player) sender,msg("command.add"),
                                                    Map.of(
                                                            "parameter", parameter,
                                                            "target", target,
                                                            "value", value))
                                    ));});
            case "costmultiplier":
                repository.findByIdAsync(target.getUniqueId())
                        .thenCompose(existing -> {
                            DataManager data = existing.orElseGet(() -> new DataManager(target.getUniqueId()));
                            data.setCostMultiplier(data.getCostMultiplier() - value);
                            return repository.saveAsync(data)
                                    .thenRun(() -> runSync(() ->MessageManager.sendMessage((Player) sender,msg("command.set"),
                                            Map.of(
                                                    "parameter", parameter,
                                                    "target", target,
                                                    "value", value))
                                    ));});
        }
    }

    private void sendUsage(CommandSender sender) {
        List<String> usageLines = configManager.getConfig("messages.yml").getStringList("command.usage.main");
        if (usageLines.isEmpty()) {

            return;
        }

        for (String line : usageLines) {
            MessageManager.sendMessage((Player) sender, line);
        }
    }


    private String msg(String path) {
        return configManager.getConfig("messages.yml").getString(path, path);
    }
    private void runSync(Runnable action) {
        if (Bukkit.isPrimaryThread()) {
            action.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, action);
    }
}
