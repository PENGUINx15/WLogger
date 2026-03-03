package me.penguinx13.wLogger.listener;

import me.penguinx13.wLogger.service.TreeHarvestService;
import me.penguinx13.wapi.managers.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public final class BlockBreakListener implements Listener {
    private final TreeHarvestService treeHarvestService;
    private final String progressTemplate;
    private final String completedTemplate;
    private final String backpackFullMessage;

    public BlockBreakListener(TreeHarvestService treeHarvestService, String progressTemplate, String completedTemplate, String backpackFullMessage) {
        this.treeHarvestService = treeHarvestService;
        this.progressTemplate = progressTemplate;
        this.completedTemplate = completedTemplate;
        this.backpackFullMessage = backpackFullMessage;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        TreeHarvestService.HarvestResult result = treeHarvestService.handleBreak(player, event.getBlock(), player.isSneaking());

        if (result.state() == TreeHarvestService.HarvestResult.State.PROGRESS) {
            event.setCancelled(true);
            MessageManager.sendMessage(player, format(progressTemplate, Map.of(
                    "current", String.valueOf(result.current()),
                    "required", String.valueOf(result.required())
            )));
            return;
        }

        if (result.state() == TreeHarvestService.HarvestResult.State.COMPLETED) {
            event.setCancelled(true);
            MessageManager.sendMessage(player, format(completedTemplate, Map.of("required", String.valueOf(result.required()))));
            if (result.backpackFull()) {
                MessageManager.sendMessage(player, backpackFullMessage);
            }
        }
    }

    private String format(String message, Map<String, String> placeholders) {
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
