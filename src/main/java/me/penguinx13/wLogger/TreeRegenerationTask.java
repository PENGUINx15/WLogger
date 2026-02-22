package me.penguinx13.wLogger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;

public class TreeRegenerationTask extends BukkitRunnable {

    private final Map<Block, BlockSnapshot> blocksToRestore;

    public TreeRegenerationTask(Map<Block, BlockSnapshot> blocksToRestore) {
        this.blocksToRestore = new LinkedHashMap<>(blocksToRestore);
    }

    @Override
    public void run() {
        for (Map.Entry<Block, BlockSnapshot> entry : blocksToRestore.entrySet()) {
            Block block = entry.getKey();
            BlockSnapshot snapshot = entry.getValue();
            block.setType(snapshot.type(), false);
            block.setBlockData(snapshot.blockData().clone(), false);
        }
    }

    public static BlockSnapshot snapshot(Block block) {
        return new BlockSnapshot(block.getType(), block.getBlockData().clone());
    }

    public record BlockSnapshot(Material type, BlockData blockData) {}
}