package me.gnat008.infiniteblocks.listeners;

import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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
            if (plugin.replaceBlock(block)) {
                BlockRegion region;
                try {
                    region = plugin.getRegionFromBlock(block);
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
}
