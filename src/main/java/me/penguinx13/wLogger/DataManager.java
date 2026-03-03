package me.penguinx13.wLogger;

import me.penguinx13.wLogger.service.PlayerStateService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated
public final class DataManager {
    private final PlayerStateService playerStateService;

    public DataManager(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    public int getBrokenBlocks(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            return 0;
        }
        return playerStateService.getBrokenBlocks(player);
    }

    public void setBrokenBlocks(String playerName, int blocksCount) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            playerStateService.setBrokenBlocks(player, blocksCount);
        }
    }

    public void setBackPack(String playerName, int blocksCount) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            playerStateService.setBackpack(player, blocksCount);
        }
    }

    public int getBackpack(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            return 0;
        }
        return playerStateService.getBackpack(player);
    }

    public double getCostMultiplier(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            return 1.0D;
        }
        return playerStateService.getCostMultiplier(player);
    }

    public void setCostMultiplier(String playerName, double value) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            playerStateService.setCostMultiplier(player, value);
        }
    }

    public void disconnect() {
    }
}
