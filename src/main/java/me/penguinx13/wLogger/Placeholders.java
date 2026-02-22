package me.penguinx13.wLogger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "wlogger";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PENGUINx13";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
}
