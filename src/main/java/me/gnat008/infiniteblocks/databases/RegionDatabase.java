package me.gnat008.infiniteblocks.databases;

import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.BlockRegion;

import java.util.Map;

public interface RegionDatabase {

    public void load() throws RegionDatabaseException;

    public void save() throws RegionDatabaseException;

    public void load(RegionManager manager) throws RegionDatabaseException;

    public void save(RegionManager manager) throws RegionDatabaseException;

    public Map<String, BlockRegion> getRegions();

    public void setRegions(Map<String, BlockRegion> regions);
}
