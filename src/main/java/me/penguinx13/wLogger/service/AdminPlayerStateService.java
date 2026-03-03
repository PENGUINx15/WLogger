package me.penguinx13.wLogger.service;

import org.bukkit.entity.Player;

public final class AdminPlayerStateService {
    private final PlayerStateService playerStateService;

    public AdminPlayerStateService(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    public int updateBackpack(Player target, Operation operation, int value) {
        int current = playerStateService.getBackpack(target);
        int updated = operation.apply(current, value);
        if (updated < 1) {
            throw new IllegalArgumentException("Backpack value must be >= 1");
        }
        playerStateService.setBackpack(target, updated);
        return updated;
    }

    public double updateCostMultiplier(Player target, Operation operation, double value) {
        double current = playerStateService.getCostMultiplier(target);
        double updated = operation.apply(current, value);
        if (updated <= 0.0D) {
            throw new IllegalArgumentException("Cost multiplier value must be > 0");
        }
        playerStateService.setCostMultiplier(target, updated);
        return updated;
    }

    public enum Operation {
        SET,
        ADD,
        REM;

        public int apply(int current, int value) {
            return switch (this) {
                case SET -> value;
                case ADD -> current + value;
                case REM -> current - value;
            };
        }

        public double apply(double current, double value) {
            return switch (this) {
                case SET -> value;
                case ADD -> current + value;
                case REM -> current - value;
            };
        }

        public static Operation fromSubcommand(String raw) {
            if (raw.equalsIgnoreCase("set")) {
                return SET;
            }
            if (raw.equalsIgnoreCase("add")) {
                return ADD;
            }
            if (raw.equalsIgnoreCase("rem")) {
                return REM;
            }
            throw new IllegalArgumentException("Unknown operation " + raw);
        }
    }
}
