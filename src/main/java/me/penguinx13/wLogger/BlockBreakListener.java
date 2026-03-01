package me.penguinx13.wLogger;

import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import me.penguinx13.wapi.Tree;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockBreakListener implements Listener {
    private final ConfigManager config;
    private final WLogger plugin;
    private final Map<BreakProgressKey, Integer> breakProgress = new HashMap<>();
    private boolean worldMismatchWarningLogged = false;

    public BlockBreakListener(ConfigManager config, WLogger plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!isLocation(block)) {
            return;
        }

        if (!Tag.LOGS.isTagged(block.getType())) {
            return;
        }

        BreakProgressKey key = BreakProgressKey.of(player.getUniqueId(), block);
        if (player.isSneaking()) {
            breakProgress.remove(key);
            return;
        }

        Tree tree = new Tree(block);
        if (!tree.collect()) {
            return;
        }

        int requiredBreaks = tree.getLogs().size();
        int currentBreaks = breakProgress.getOrDefault(key, 0) + 1;

        if (currentBreaks < requiredBreaks) {
            breakProgress.put(key, currentBreaks);
            MessageManager.sendMessage(player, formatMessage("break.progress", Map.of(
                    "current", String.valueOf(currentBreaks),
                    "required", String.valueOf(requiredBreaks)
            )));
            event.setCancelled(true);
            return;
        }

        breakProgress.remove(key);
        event.setCancelled(true);

        Map<Block, TreeRegenerationTask.BlockSnapshot> treeState = new HashMap<>();
        for (Block log : tree.getLogs()) {
            treeState.put(log, TreeRegenerationTask.snapshot(log));
            log.setType(Material.AIR);
        }

        Collection<Block> leaves = tree.getLeaves();
        for (Block leaf : leaves) {
            treeState.putIfAbsent(leaf, TreeRegenerationTask.snapshot(leaf));
        }

        MessageManager.sendMessage(player, formatMessage("break.completed", Map.of("required", String.valueOf(requiredBreaks))));

        if (plugin.getDataManager().getBackpack(player.getName()) <= (plugin.getDataManager().getBrokenBlocks(player.getName()) + requiredBreaks)) {
            MessageManager.sendMessage(player, msg("break.backpackFull"));
            plugin.getDataManager().setBrokenBlocks(player.getName(), plugin.getDataManager().getBackpack(player.getName()));
        } else {
            plugin.getDataManager().setBrokenBlocks(player.getName(), plugin.getDataManager().getBrokenBlocks(player.getName()) + requiredBreaks);
        }
        new LeafDecayTask(leaves).runTaskTimer(plugin, 1L, 1L);

        long cooldownSeconds = Math.max(1, config.getConfig("config.yml").getLong("tree.cooldown", 15L));
        new TreeRegenerationTask(treeState).runTaskLater(plugin, cooldownSeconds * 20L);
    }

    private boolean isLocation(Block block) {
        FileConfiguration cfg = config.getConfig("config.yml");

        String minWorld = cfg.getString("location.min.world");
        String maxWorld = cfg.getString("location.max.world");
        if (minWorld == null || maxWorld == null) {
            return false;
        }

        if (!minWorld.equals(maxWorld)) {
            if (!worldMismatchWarningLogged) {
                plugin.getLogger().warning(msg("log.regionConfigMismatch"));
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

    private String msg(String path) {
        return config.getConfig("messeges.yml").getString(path, path);
    }
    private String formatMessage(String path, Map<String, String> placeholders) {
        String message = msg(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    private record BreakProgressKey(UUID playerId, String world, int x, int y, int z) {
        private static BreakProgressKey of(UUID playerId, Block block) {
            return new BreakProgressKey(
                    playerId,
                    block.getWorld().getName(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
        }
    }
}
