package me.penguinx13.wLogger.data;

import me.penguinx13.wapi.orm.annotations.Column;
import me.penguinx13.wapi.orm.annotations.Id;
import me.penguinx13.wapi.orm.annotations.Table;

import java.util.UUID;

@Table("example_player_stats")
public final class DataManager {

    @Id
    private UUID uuid;

    @Column
    private int backpack;

    @Column
    private double costmultiplier;

    @Column
    private int brokenblocks;

    public DataManager() {
    }

    public DataManager(UUID uuid) {
        this.uuid = uuid;
        this.backpack = 50;
        this.costmultiplier = 1.0;
        this.brokenblocks = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getBackpack() {
        return backpack;
    }

    public void addBackpack(int amount) {
        this.backpack += amount;
    }

    public void setBackpack(int amount) {
        this.backpack = amount;
    }

    public double getCostMultiplier() {
        return costmultiplier;
    }

    public void addCostMultiplier(double amount) {
        this.costmultiplier += amount;
    }

    public void setCostMultiplier(double amount) {
        this.costmultiplier = amount;
    }

    public int getBrokenBlocks() {
        return brokenblocks;
    }

    public void addBrokenBlocks(int amount) {
        this.brokenblocks += amount;
    }

    public void setBrokenBlocks(int amount) {
        this.brokenblocks = amount;
    }
}
