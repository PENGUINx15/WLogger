package me.penguinx13.wLogger;

import me.penguinx13.wapi.Managers.ConfigManager;
import me.penguinx13.wapi.Managers.MessageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BlockBreakListener implements Listener {
    private final ConfigManager config;
    private final WLogger plugin;
    private final Map<BreakProgressKey, Integer> breakProgress = new HashMap<>();

    public BlockBreakListener(ConfigManager config, WLogger plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

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
            MessageManager.sendMessage(player, "{action}&e" + currentBreaks + "&f/&6" + requiredBreaks);
            event.setCancelled(true);
            return;
        }

        breakProgress.remove(key);
        event.setCancelled(true);

        for (Block log : tree.getLogs()) {
            log.setType(Material.AIR);
        }
        MessageManager.sendMessage(player, "{action}&6" + requiredBreaks);
        Collection<Block> leaves = tree.getLeaves();
        new LeafDecayTask(leaves).runTaskTimer(plugin, 1L, 1L);
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