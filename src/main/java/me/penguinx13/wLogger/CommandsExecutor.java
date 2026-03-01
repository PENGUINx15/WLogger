package me.penguinx13.wLogger;

import me.penguinx13.wapi.commandframework.annotations.RootCommand;
import me.penguinx13.wapi.commandframework.annotations.SubCommand;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RootCommand(value = "wlogger")
public class CommandsExecutor {
    private final WLogger plugin;
    private final ConfigManager configManager;

    public CommandsExecutor(WLogger plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }
    @SubCommand(value = "reload", permission = "wlogger.reload")
    public void reload(CommandSender sender) {
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
        MessageManager.sendMessage((Player) sender, configManager.getConfig("messages.yml").getString("command.reload.success"));
    }
}