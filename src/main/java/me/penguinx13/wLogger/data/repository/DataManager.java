package me.penguinx13.wLogger.data.repository;

import me.penguinx13.wLogger.data.model.PlayerStateDTO;
import me.penguinx13.wLogger.data.model.ServerSettingsDTO;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataManager {
    CompletableFuture<Void> initialize();

    CompletableFuture<ServerSettingsDTO> loadServerSettings();

    CompletableFuture<Void> saveServerSettings(ServerSettingsDTO settingsDTO);

    CompletableFuture<PlayerStateDTO> loadPlayerState(UUID playerId, String playerName);

    CompletableFuture<Void> savePlayerState(PlayerStateDTO playerStateDTO);

    CompletableFuture<Void> shutdown();
}
