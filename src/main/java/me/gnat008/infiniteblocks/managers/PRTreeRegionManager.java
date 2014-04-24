package me.gnat008.infiniteblocks.managers;

import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.databases.RegionDatabase;
import me.gnat008.infiniteblocks.regions.ApplicableRegionSet;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import me.gnat008.infiniteblocks.regions.BlocksRegionMBRConverter;
import org.bukkit.entity.Player;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.*;

public class PRTreeRegionManager extends RegionManager {

    private static final int BRANCH_FACTOR = 30;

    private Map<String, BlockRegion> regions;
    private MBRConverter<BlockRegion> converter = new BlocksRegionMBRConverter();
    private PRTree<BlockRegion> tree;

    public PRTreeRegionManager(RegionDatabase regionLoader) {
        super(regionLoader);
        regions = new TreeMap<String, BlockRegion>();
        tree = new PRTree<BlockRegion>(converter, BRANCH_FACTOR);
    }

    @Override
    public Map<String, BlockRegion> getRegions() {
        return regions;
    }

    @Override
    public void setRegions(Map<String, BlockRegion> regions) {
        this.regions = new TreeMap<String, BlockRegion>(regions);
        tree = new PRTree<BlockRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    @Override
    public void addRegion(BlockRegion region) {
        regions.put(region.getId().toLowerCase(), region);
        tree = new PRTree<BlockRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    @Override
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }

    @Override
    public void removeRegion(String id) {
        BlockRegion region = regions.get(id.toLowerCase());

        regions.remove(id.toLowerCase());

        if (region != null) {
            List<String> removedRegions = new ArrayList<String>();
            for (BlockRegion currentRegion : regions.values()) {
                if (currentRegion.getParent() == region) {
                    removedRegions.add(currentRegion.getId().toLowerCase());
                }
            }

            for (String remID : removedRegions) {
                removeRegion(remID);
            }
        }

        tree = new PRTree<BlockRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        pt = pt.floor();

        List<BlockRegion> appRegions = new ArrayList<BlockRegion>();
        MBR pointsMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (BlockRegion region : tree.find(pointsMBR)) {
            if (region.contains(pt) && !appRegions.contains(region)) {
                appRegions.add(region);

                BlockRegion parent = region.getParent();

                while (parent != null) {
                    if (!appRegions.contains(parent)) {
                        appRegions.add(parent);
                    }

                    parent = parent.getParent();
                }
            }
        }

        Collections.sort(appRegions);

        return new ApplicableRegionSet(appRegions, regions.get("__global__"));
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(BlockRegion checkRegion) {
        List<BlockRegion> appRegions = new ArrayList<BlockRegion>();
        appRegions.addAll(regions.values());

        List<BlockRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<BlockRegion>();
        }

        return new ApplicableRegionSet(intersectRegions, regions.get("__global__"));
    }

    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {
        // Floor the vector to get accurate points.
        pt = pt.floor();

        List<String> applicable = new ArrayList<String>();
        MBR pointMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (BlockRegion region : tree.find(pointMBR)) {
            if (region.contains(pt) && !applicable.contains(region.getId())) {
                applicable.add(region.getId());

                BlockRegion parent = region.getParent();

                while (parent != null) {
                    if (!applicable.contains(parent.getId())) {
                        applicable.add(parent.getId());
                    }

                    parent = parent.getParent();
                }
            }
        }

        return applicable;
    }

    @Override
    public boolean overlapsUnownedRegion(BlockRegion checkRegion, Player player) {
        List<BlockRegion> appRegions = new ArrayList<BlockRegion>();

        for (BlockRegion other : regions.values()) {
            if (other.getOwner().contains(player.getName())) {
                continue;
            }

            appRegions.add(other);
        }

        List<BlockRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<BlockRegion>();
        }

        return intersectRegions.size() > 0;
    }

    @Override
    public int size() {
        return regions.size();
    }

    @Override
    public int getRegionCountOfPlayer(Player player) {
        int count = 0;

        for (Map.Entry<String, BlockRegion> entry : regions.entrySet()) {
            if (entry.getValue().getOwner().contains(player.getName())) {
                count++;
            }
        }

        return count;
    }
}
