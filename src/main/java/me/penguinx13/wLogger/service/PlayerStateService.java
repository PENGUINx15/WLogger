package me.penguinx13.wLogger.service;

import me.penguinx13.wLogger.WLogger;
import me.penguinx13.wLogger.data.model.PlayerStateDTO;
import me.penguinx13.wLogger.data.model.ServerSettingsDTO;
import me.penguinx13.wLogger.data.repository.DataManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateService {
    private final WLogger plugin;
    private final DataManager dataManager;
    private final Map<UUID, CachedState> cache;
    private volatile ServerSettingsDTO defaults;

    public PlayerStateService(WLogger plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.cache = new ConcurrentHashMap<>();
        this.defaults = new ServerSettingsDTO(50, 1.0D);
    }

    public void initialize(int defaultBackpack, double defaultCostMultiplier) {
        this.defaults = new ServerSettingsDTO(defaultBackpack, defaultCostMultiplier);
        dataManager.saveServerSettings(this.defaults);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::flushDirty, 100L, 100L);
    }

    public PlayerStateDTO getOrLoad(Player player) {
        CachedState cached = cache.computeIfAbsent(player.getUniqueId(), uuid -> {
            CachedState newState = new CachedState(
                    new PlayerStateDTO(uuid, player.getName(), defaults.defaultBackpack(), defaults.defaultCostMultiplier(), 0)
            );
            dataManager.loadPlayerState(uuid, player.getName()).thenAccept(dto -> {
                cache.compute(uuid, (ignored, current) -> {
                    if (current == null || !current.dirty) {
                        return new CachedState(dto);
                    }
                    return current;
                });
            });
            return newState;
        });

        return cached.state.copy();
    }

    public int getBrokenBlocks(Player player) {
        return getOrLoad(player).getBrokenBlocks();
    }

    public int getBackpack(Player player) {
        return getOrLoad(player).getBackpack();
    }

    public double getCostMultiplier(Player player) {
        return getOrLoad(player).getCostMultiplier();
    }

    public void setBrokenBlocks(Player player, int brokenBlocks) {
        update(player, state -> state.setBrokenBlocks(Math.max(0, brokenBlocks)));
    }

    public void setBackpack(Player player, int backpack) {
        update(player, state -> state.setBackpack(backpack));
    }

    public void setCostMultiplier(Player player, double costMultiplier) {
        update(player, state -> state.setCostMultiplier(costMultiplier));
    }

    public void flushAndShutdown() {
        flushDirty();
        dataManager.shutdown();
    }

    private void update(Player player, StateMutator mutator) {
        cache.compute(player.getUniqueId(), (uuid, cachedState) -> {
            CachedState current = cachedState;
            if (current == null) {
                current = new CachedState(new PlayerStateDTO(uuid, player.getName(), defaults.defaultBackpack(), defaults.defaultCostMultiplier(), 0));
            }
            mutator.mutate(current.state);
            current.dirty = true;
            return current;
        });
    }

    private void flushDirty() {
        for (CachedState cachedState : cache.values()) {
            if (cachedState.dirty) {
                cachedState.dirty = false;
                dataManager.savePlayerState(cachedState.state.copy());
            }
        }
    }

    public void setDefaults(ServerSettingsDTO defaults) {
        this.defaults = defaults;
        dataManager.saveServerSettings(defaults);
    }

    private interface StateMutator {
        void mutate(PlayerStateDTO state);
    }

    private static final class CachedState {
        private final PlayerStateDTO state;
        private volatile boolean dirty;

        private CachedState(PlayerStateDTO state) {
            this.state = state;
            this.dirty = false;
        }
    }
}
