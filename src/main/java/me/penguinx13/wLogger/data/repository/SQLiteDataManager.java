package me.penguinx13.wLogger.data.repository;

import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wLogger.data.model.PlayerStateDTO;
import me.penguinx13.wLogger.data.model.ServerSettingsDTO;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SQLiteDataManager implements DataManager {
    private final WLogger plugin;
    private final ExecutorService databaseExecutor;
    private final String jdbcUrl;

    public SQLiteDataManager(WLogger plugin) {
        this.plugin = plugin;
        this.databaseExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "wlogger-db-thread");
            thread.setDaemon(true);
            return thread;
        });

        File databaseFile = new File(plugin.getDataFolder(), "players.db");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 PreparedStatement createPlayers = connection.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS players ("
                                 + "player_id TEXT PRIMARY KEY,"
                                 + "player_name TEXT NOT NULL,"
                                 + "backpack INTEGER NOT NULL DEFAULT 50,"
                                 + "cost_multiplier DOUBLE NOT NULL DEFAULT 1.0,"
                                 + "broken_blocks INTEGER NOT NULL DEFAULT 0"
                                 + ")");
                 PreparedStatement createServer = connection.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS server_settings ("
                                 + "id INTEGER PRIMARY KEY CHECK (id = 1),"
                                 + "default_backpack INTEGER NOT NULL DEFAULT 50,"
                                 + "default_cost_multiplier DOUBLE NOT NULL DEFAULT 1.0"
                                 + ")");
                 PreparedStatement seedServer = connection.prepareStatement(
                         "INSERT INTO server_settings (id, default_backpack, default_cost_multiplier) "
                                 + "VALUES (1, 50, 1.0) ON CONFLICT(id) DO NOTHING")) {
                createPlayers.executeUpdate();
                createServer.executeUpdate();
                seedServer.executeUpdate();
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to initialize database", exception);
            }
        }, databaseExecutor);
    }

    @Override
    public CompletableFuture<ServerSettingsDTO> loadServerSettings() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT default_backpack, default_cost_multiplier FROM server_settings WHERE id = 1");
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ServerSettingsDTO(resultSet.getInt("default_backpack"), resultSet.getDouble("default_cost_multiplier"));
                }
                return new ServerSettingsDTO(50, 1.0D);
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to load server settings", exception);
            }
        }, databaseExecutor);
    }

    @Override
    public CompletableFuture<Void> saveServerSettings(ServerSettingsDTO settingsDTO) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE server_settings SET default_backpack = ?, default_cost_multiplier = ? WHERE id = 1")) {
                statement.setInt(1, settingsDTO.defaultBackpack());
                statement.setDouble(2, settingsDTO.defaultCostMultiplier());
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to save server settings", exception);
            }
        }, databaseExecutor);
    }

    @Override
    public CompletableFuture<PlayerStateDTO> loadPlayerState(UUID playerId, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 PreparedStatement readPlayer = connection.prepareStatement(
                         "SELECT backpack, cost_multiplier, broken_blocks FROM players WHERE player_id = ?")) {
                readPlayer.setString(1, playerId.toString());
                try (ResultSet resultSet = readPlayer.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlayerStateDTO(
                                playerId,
                                playerName,
                                resultSet.getInt("backpack"),
                                resultSet.getDouble("cost_multiplier"),
                                resultSet.getInt("broken_blocks")
                        );
                    }
                }
                ServerSettingsDTO defaults = readDefaults(connection);
                PlayerStateDTO defaultState = new PlayerStateDTO(playerId, playerName, defaults.defaultBackpack(), defaults.defaultCostMultiplier(), 0);
                savePlayer(connection, defaultState);
                return defaultState;
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to load player state for " + playerName, exception);
            }
        }, databaseExecutor);
    }

    @Override
    public CompletableFuture<Void> savePlayerState(PlayerStateDTO playerStateDTO) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO players (player_id, player_name, backpack, cost_multiplier, broken_blocks) VALUES (?, ?, ?, ?, ?) "
                                 + "ON CONFLICT(player_id) DO UPDATE SET "
                                 + "player_name = excluded.player_name, "
                                 + "backpack = excluded.backpack, "
                                 + "cost_multiplier = excluded.cost_multiplier, "
                                 + "broken_blocks = excluded.broken_blocks")) {
                statement.setString(1, playerStateDTO.getPlayerId().toString());
                statement.setString(2, playerStateDTO.getPlayerName());
                statement.setInt(3, playerStateDTO.getBackpack());
                statement.setDouble(4, playerStateDTO.getCostMultiplier());
                statement.setInt(5, playerStateDTO.getBrokenBlocks());
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to save player state for " + playerStateDTO.getPlayerName(), exception);
            }
        }, databaseExecutor);
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
        }, databaseExecutor).whenComplete((ignored, throwable) -> databaseExecutor.shutdown());
    }

    private ServerSettingsDTO readDefaults(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT default_backpack, default_cost_multiplier FROM server_settings WHERE id = 1");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new ServerSettingsDTO(resultSet.getInt("default_backpack"), resultSet.getDouble("default_cost_multiplier"));
            }
            return new ServerSettingsDTO(50, 1.0D);
        }
    }

    private void savePlayer(Connection connection, PlayerStateDTO playerStateDTO) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO players (player_id, player_name, backpack, cost_multiplier, broken_blocks) VALUES (?, ?, ?, ?, ?) "
                        + "ON CONFLICT(player_id) DO UPDATE SET "
                        + "player_name = excluded.player_name, "
                        + "backpack = excluded.backpack, "
                        + "cost_multiplier = excluded.cost_multiplier, "
                        + "broken_blocks = excluded.broken_blocks")) {
            statement.setString(1, playerStateDTO.getPlayerId().toString());
            statement.setString(2, playerStateDTO.getPlayerName());
            statement.setInt(3, playerStateDTO.getBackpack());
            statement.setDouble(4, playerStateDTO.getCostMultiplier());
            statement.setInt(5, playerStateDTO.getBrokenBlocks());
            statement.executeUpdate();
        }
    }
}
