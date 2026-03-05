package me.penguinx13.wLogger.service;

import me.penguinx13.wLogger.LeafDecayTask;
import me.penguinx13.wLogger.TreeRegenerationTask;
import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wapi.Tree;
import me.penguinx13.wapi.managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TreeHarvestService {
    private final WLogger plugin;
    private final ConfigManager configManager;
    private final Map<BreakProgressKey, Integer> breakProgress;
    private volatile boolean worldMismatchWarningLogged;

    public TreeHarvestService(WLogger plugin, ConfigManager configManager, PlayerStateService playerStateService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerStateService = playerStateService;
        this.breakProgress = new ConcurrentHashMap<>();
        this.worldMismatchWarningLogged = false;
    }

    public HarvestResult handleBreak(Player player, Block block, boolean sneaking) {
        if (!isLocation(block)) {
            return HarvestResult.ignore();
        }
        if (!Tag.LOGS.isTagged(block.getType())) {
            return HarvestResult.ignore();
        }

        BreakProgressKey key = BreakProgressKey.of(player.getUniqueId(), block);
        if (sneaking) {
            breakProgress.remove(key);
            return HarvestResult.ignore();
        }

        Tree tree = new Tree(block);
        if (!tree.collect()) {
            return HarvestResult.ignore();
        }

        int requiredBreaks = tree.getLogs().size();
        int currentBreaks = breakProgress.getOrDefault(key, 0) + 1;

        if (currentBreaks < requiredBreaks) {
            breakProgress.put(key, currentBreaks);
            return HarvestResult.progress(currentBreaks, requiredBreaks);
        }

        breakProgress.remove(key);

        Map<Block, TreeRegenerationTask.BlockSnapshot> treeState = new HashMap<>();
        for (Block log : tree.getLogs()) {
            treeState.put(log, TreeRegenerationTask.snapshot(log));
            log.setType(Material.AIR);
        }

        Collection<Block> leaves = tree.getLeaves();
        for (Block leaf : leaves) {
            treeState.putIfAbsent(leaf, TreeRegenerationTask.snapshot(leaf));
        }

        int backpack = playerStateService.getBackpack(player);
        int brokenBlocks = playerStateService.getBrokenBlocks(player);
        boolean full = backpack <= (brokenBlocks + requiredBreaks);
        if (full) {
            playerStateService.setBrokenBlocks(player, backpack);
        } else {
            playerStateService.setBrokenBlocks(player, brokenBlocks + requiredBreaks);
        }

        new LeafDecayTask(leaves).runTaskTimer(plugin, 1L, 1L);
        long cooldownSeconds = Math.max(1, configManager.getConfig("config.yml").getLong("tree.cooldown", 15L));
        new TreeRegenerationTask(treeState).runTaskLater(plugin, cooldownSeconds * 20L);

        return HarvestResult.completed(requiredBreaks, full);
    }

    public boolean validateRegionConfiguration() {
        FileConfiguration cfg = configManager.getConfig("config.yml");
        String minWorld = cfg.getString("location.min.world");
        String maxWorld = cfg.getString("location.max.world");
        if (minWorld == null || maxWorld == null) {
            return false;
        }
        return minWorld.equals(maxWorld);
    }

    private boolean isLocation(Block block) {
        FileConfiguration cfg = configManager.getConfig("config.yml");

        String minWorld = cfg.getString("location.min.world");
        String maxWorld = cfg.getString("location.max.world");
        if (minWorld == null || maxWorld == null) {
            return false;
        }

        if (!minWorld.equals(maxWorld)) {
            if (!worldMismatchWarningLogged) {
                plugin.getLogger().warning("Invalid region configuration: location.min.world and location.max.world must match.");
                worldMismatchWarningLogged = true;
            }
            return false;
        }

        if (!block.getWorld().getName().equals(minWorld)) {
            return false;
        }

        int minX = Math.min(cfg.getInt("location.min.x"), cfg.getInt("location.max.x"));
        int maxX = Math.max(cfg.getInt("location.min.x"), cfg.getInt("location.max.x"));
        int minY = Math.min(cfg.getInt("location.min.y"), cfg.getInt("location.max.y"));
        int maxY = Math.max(cfg.getInt("location.min.y"), cfg.getInt("location.max.y"));
        int minZ = Math.min(cfg.getInt("location.min.z"), cfg.getInt("location.max.z"));
        int maxZ = Math.max(cfg.getInt("location.min.z"), cfg.getInt("location.max.z"));

        return block.getX() >= minX && block.getX() <= maxX
                && block.getY() >= minY && block.getY() <= maxY
                && block.getZ() >= minZ && block.getZ() <= maxZ;
    }

    public record HarvestResult(State state, int current, int required, boolean backpackFull) {
        public static HarvestResult ignore() {
            return new HarvestResult(State.IGNORE, 0, 0, false);
        }

        public static HarvestResult progress(int current, int required) {
            return new HarvestResult(State.PROGRESS, current, required, false);
        }

        public static HarvestResult completed(int required, boolean backpackFull) {
            return new HarvestResult(State.COMPLETED, required, required, backpackFull);
        }

        public enum State {
            IGNORE,
            PROGRESS,
            COMPLETED
        }
    }

    private record BreakProgressKey(UUID playerId, String world, int x, int y, int z) {
        private static BreakProgressKey of(UUID playerId, Block block) {
            return new BreakProgressKey(playerId, block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }
    }
}
