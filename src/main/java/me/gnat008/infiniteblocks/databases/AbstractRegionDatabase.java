package me.gnat008.infiniteblocks.databases;

import me.gnat008.infiniteblocks.managers.RegionManager;

public abstract class AbstractRegionDatabase implements RegionDatabase {

    // Load the list of regions into a region manager
    public void load(RegionManager manager) throws RegionDatabaseException {
        load();
        manager.setRegions(getRegions());
    }

    // Save the list of regions from a region manager
    public void save(RegionManager manager) throws RegionDatabaseException {
        setRegions(manager.getRegions());
        save();
    }
}
