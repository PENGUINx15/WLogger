package me.penguinx13.wLogger.util;

public final class RewardCalculator {
    private RewardCalculator() {
    }

    public static double calculate(int blocks, double rewardPerBlock, double multiplier) {
        return blocks * rewardPerBlock * multiplier;
    }
}
