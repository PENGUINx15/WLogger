package me.penguinx13.wLogger.data;

import me.penguinx13.wapi.orm.annotations.Column;
import me.penguinx13.wapi.orm.annotations.Id;
import me.penguinx13.wapi.orm.annotations.Table;

import java.util.UUID;

@Table("players")
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

    public void setBackpack(int amount) {
        this.backpack = amount;
    }

    public double getCostMultiplier() {
        return costmultiplier;
    }


    public void setCostMultiplier(double amount) {
        this.costmultiplier = amount;
    }

    public int getBrokenBlocks() {
        return brokenblocks;
    }

    public void setBrokenBlocks(int amount) {
        this.brokenblocks = amount;
    }
}
