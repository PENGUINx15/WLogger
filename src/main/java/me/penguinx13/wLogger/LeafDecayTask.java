package me.penguinx13.wLogger;

import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

public class LeafDecayTask extends BukkitRunnable {

    private final Queue<Block> leaves;

    public LeafDecayTask(Collection<Block> leaves) {
        this.leaves = new ArrayDeque<>(leaves);
    }

    @Override
    public void run() {
        if (leaves.isEmpty()) {
            cancel();
            return;
        }

        Block leaf = leaves.poll();

        if (leaf == null || leaf.getType().isAir()) {
            return;
        }

        if (leaf.getBlockData() instanceof Leaves) {
            leaf.breakNaturally();
        }
    }
}
