package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.SQLiteManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataManager {
    private final SQLiteManager sqliteManager;
    private final String jdbcUrl;

    public DataManager(WLogger plugin) {
        this.sqliteManager = new SQLiteManager(plugin, "players.db");
        this.sqliteManager.connect();

        File databaseFile = new File(plugin.getDataFolder(), "players.db");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    public void createPlayersTableIfNotExists() {
        sqliteManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (" +
                        "playerName TEXT PRIMARY KEY," +
                        "backpack INTEGER NOT NULL DEFAULT 50," +
                        "costmultiplier DOUBLE NOT NULL DEFAULT 1.0," +
                        "brokenBlocks INTEGER NOT NULL DEFAULT 0" +
                        ")"
        );
    }

    public void ensureServerDefault(int defaultBackpack, double defaultCostMultiplier) {
        sqliteManager.executeUpdate(
                "INSERT INTO players (playerName, backpack, costmultiplier, brokenBlocks) VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(playerName) DO NOTHING",
                "server-default",
                defaultBackpack,
                defaultCostMultiplier,
                0
        );
    }

    public void ensurePlayerExists(String playerName) {
        sqliteManager.executeUpdate(
                "INSERT INTO players (playerName, backpack, costmultiplier, brokenBlocks) " +
                        "SELECT ?, backpack, costmultiplier, 0 FROM players WHERE playerName = ? " +
                        "ON CONFLICT(playerName) DO NOTHING",
                playerName,
                "server-default"
        );
    }

    public void addBrokenBlocks(String playerName, int amount) {
        sqliteManager.executeUpdate(
                "UPDATE players SET brokenBlocks = brokenBlocks + ? WHERE playerName = ?",
                amount,
                playerName
        );
    }

    public void incrementBrokenBlocksForTree(String playerName, int logsCount) {
        ensurePlayerExists(playerName);
        addBrokenBlocks(playerName, logsCount);
    }

    public int getBrokenBlocks(String playerName) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT brokenBlocks FROM players WHERE playerName = ?"
             )) {
            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("brokenBlocks");
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось получить brokenBlocks для игрока " + playerName, exception);
        }

        return 0;
    }
}
