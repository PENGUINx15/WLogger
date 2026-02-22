package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandsExecutor implements CommandExecutor {
    public CommandsExecutor(WLogger wLogger, ConfigManager configManager) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }
}
