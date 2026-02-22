package me.penguinx13.wLogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WLoggerTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            addIfMatches(suggestions, args[0], "claim");
            if (sender.hasPermission("wlogger.admin")) {
                addIfMatches(suggestions, args[0], "reload");
                addIfMatches(suggestions, args[0], "set");
                addIfMatches(suggestions, args[0], "add");
                addIfMatches(suggestions, args[0], "rem");
            }
            return suggestions;
        }

        if (args.length == 2 && isOperation(args[0])) {
            addIfMatches(suggestions, args[1], "backpack");
            addIfMatches(suggestions, args[1], "costmultiplier");
            addIfMatches(suggestions, args[1], "cosmultipler");
            return suggestions;
        }

        return suggestions;
    }

    private boolean isOperation(String value) {
        return value.equalsIgnoreCase("set") || value.equalsIgnoreCase("add") || value.equalsIgnoreCase("rem");
    }

    private void addIfMatches(List<String> list, String input, String value) {
        if (value.toLowerCase().startsWith(input.toLowerCase())) {
            list.add(value);
        }
    }
}
