package me.penguinx13.wLogger;

import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.SQLiteManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DataManager {
    private final SQLiteManager sqliteManager;
    private final String jdbcUrl;
    private final ConfigManager configManager;

    public DataManager(WLogger plugin, ConfigManager configManager) {
        this.configManager = configManager;
        this.sqliteManager = new SQLiteManager(plugin, "players.db");
        this.sqliteManager.connect();

        File databaseFile = new File(plugin.getDataFolder(), "players.db");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    public void createTable() {
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

    public void setBrokenBlocks(String playerName, int blocksCount) {
        ensurePlayerExists(playerName);

        sqliteManager.executeUpdate(
                "UPDATE players SET brokenBlocks = ? WHERE playerName = ?",
                blocksCount,
                playerName
        );
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
            throw new RuntimeException(formatMessage("error.getBrokenBlocks", Map.of("player", playerName)), exception);
        }

        return 0;
    }

    public void setBackPack(String playerName, int blocksCount) {
        ensurePlayerExists(playerName);

        sqliteManager.executeUpdate(
                "UPDATE players SET backpack = ? WHERE playerName = ?",
                blocksCount,
                playerName
        );
    }

    public int getBackpack(String playerName) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT backpack FROM players WHERE playerName = ?"
             )) {
            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("backpack");
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(formatMessage("error.getBackpack", Map.of("player", playerName)), exception);
        }

        return 0;
    }

    public double getCostMultiplier(String playerName) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT costmultiplier FROM players WHERE playerName = ?"
             )) {
            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("costmultiplier");
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(formatMessage("error.getCostMultiplier", Map.of("player", playerName)), exception);
        }

        return 1.0D;
    }

    public void setCostMultiplier(String playerName, double value) {
        ensurePlayerExists(playerName);

        sqliteManager.executeUpdate(
                "UPDATE players SET costmultiplier = ? WHERE playerName = ?",
                value,
                playerName
        );
    }

    public void disconnect() {
        sqliteManager.disconnect();
    }

    private String msg(String path) {
        return configManager.getConfig("messeges.yml").getString(path, path);
    }

    private String formatMessage(String path, Map<String, String> placeholders) {
        String message = msg(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
}
