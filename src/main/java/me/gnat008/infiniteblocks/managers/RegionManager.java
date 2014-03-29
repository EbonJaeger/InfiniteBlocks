package me.gnat008.infiniteblocks.managers;

import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.databases.RegionDatabase;
import me.gnat008.infiniteblocks.databases.RegionDatabaseException;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public abstract class RegionManager {

    protected RegionDatabase loader;

    public RegionManager(RegionDatabase loader) {
        this.loader = loader;
    }

    // Load the list of regions. If the regions do not load properly, then
    // the existing list should be used (as stored previously).
    public void load() throws RegionDatabaseException {
        loader.load(this);
    }

    // Save the list of regions
    public void save() throws RegionDatabaseException {
        loader.save(this);
    }

    // Get a map of protected regions. Use one of the region manager methods
    // if possible if working with regions.
    public abstract Map<String, BlockRegion> getRegions();

    // Set a list of protected regions. Keys should be lowercase in the given
    // map of regions.
    public abstract void setRegions(Map<String, BlockRegion> regions);

    // Adds a region. If a region by the given name already exists, then
    // the existing region will be replaced.
    public abstract void addRegion(BlockRegion region);

    // Return whether a region exists by ID.
    public abstract boolean hasRegion(String id);

    // Get a region by its ID.
    public BlockRegion getRegion(String id) {
        if (id.startsWith("#")) {
            int index;

            try {
                index = Integer.parseInt(id.substring(1)) - 1;
            } catch (NumberFormatException e) {
                return null;
            }

            for (BlockRegion region : getRegions().values()) {
                if (index == 0) {
                    return region;
                }

                --index;
            }

            return null;
        }

        return getRegionExact(id);
    }

    // Gets a region by its ID.
    public BlockRegion getRegionExact(String id) {
        return getRegions().get(id.toLowerCase());
    }

    // Remove a region.
    public abstract void removeRegion(String id);

    // Get a list of region ID's that contain a given point.
    public abstract List<String> getApplicableRegionsIDs(Vector pt);

    // Returns true if the provided region overlaps with any other region that
    // is not owned by the player.
    public abstract boolean overlapsUnownedRegion(BlockRegion region, Player player);

    // Get the number of regions.
    public abstract int size();

    // Get the number of regions for a player.
    public abstract int getRegionCountOfPlayer(Player player);
}
