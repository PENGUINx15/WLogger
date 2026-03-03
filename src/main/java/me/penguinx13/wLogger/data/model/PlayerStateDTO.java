package me.penguinx13.wLogger.data.model;

import java.util.UUID;

public final class PlayerStateDTO {
    private final UUID playerId;
    private final String playerName;
    private int backpack;
    private double costMultiplier;
    private int brokenBlocks;

    public PlayerStateDTO(UUID playerId, String playerName, int backpack, double costMultiplier, int brokenBlocks) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.backpack = backpack;
        this.costMultiplier = costMultiplier;
        this.brokenBlocks = brokenBlocks;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getBackpack() {
        return backpack;
    }

    public void setBackpack(int backpack) {
        this.backpack = backpack;
    }

    public double getCostMultiplier() {
        return costMultiplier;
    }

    public void setCostMultiplier(double costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public int getBrokenBlocks() {
        return brokenBlocks;
    }

    public void setBrokenBlocks(int brokenBlocks) {
        this.brokenBlocks = brokenBlocks;
    }

    public PlayerStateDTO copy() {
        return new PlayerStateDTO(playerId, playerName, backpack, costMultiplier, brokenBlocks);
    }
}
