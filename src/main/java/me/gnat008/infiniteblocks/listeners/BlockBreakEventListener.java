package me.gnat008.infiniteblocks.listeners;

import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.ApplicableRegionSet;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

/**
 * Created by Gnat008 on 4/27/2014.
 */
public class BlockBreakEventListener implements Listener {

    private InfiniteBlocks plugin;

    public BlockBreakEventListener(InfiniteBlocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock() != null) {
            Block block = event.getBlock();
            if (checkBlockType(block)) {
                BlockRegion region;
                try {
                    region = checkBlockLocation(block);
                } catch (NullPointerException e) {
                    // Block is not in any region.
                    return;
                }

                if (region != null) {
                    int delay = region.getDelay();
                    final Location location = block.getLocation();
                    final Material material = block.getType();
                    final World world = block.getWorld();

                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            world.getBlockAt(location).setType(material);
                        }
                    }, delay * 20);
                }
            }
        }
    }

    private BlockRegion checkBlockLocation(Block block) {
        World world = block.getWorld();
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);

        Location loc = block.getLocation();
        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(loc);

        if (regionSet == null || regionSet.size() > 0) {
            return null;
        } else if (regionSet.size() == 1) {
            return regionSet.iterator().next();
        } else if (regionSet.size() > 1) {
            // TODO: Check priority of regions; highest priority gets returned.
        }

        return null;
    }

    private boolean checkBlockType(Block block) {
        Material material = block.getType();

        World world = block.getWorld();
        List<String> blockTypes = plugin.getGlobalStateManager().get(world).getBlocksToReplace();

        for (String type : blockTypes) {
            if (material.toString().equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }
}
