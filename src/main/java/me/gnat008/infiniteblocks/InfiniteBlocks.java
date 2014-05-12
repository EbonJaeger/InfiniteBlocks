package me.gnat008.infiniteblocks;

import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.gnat008.infiniteblocks.command.InfiniteBlocksCommand;
import me.gnat008.infiniteblocks.config.ConfigurationManager;
import me.gnat008.infiniteblocks.exceptions.FatalConfigurationLoadingException;
import me.gnat008.infiniteblocks.listeners.BlockBreakEventListener;
import me.gnat008.infiniteblocks.listeners.BlockIgniteListener;
import me.gnat008.infiniteblocks.listeners.LeafDecayListener;
import me.gnat008.infiniteblocks.managers.GlobalRegionManager;
import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.ApplicableRegionSet;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class InfiniteBlocks extends JavaPlugin {

    private static InfiniteBlocks plugin;
    private static Logger log = Bukkit.getServer().getLogger();

    private static boolean foundMA = false;

    public static PluginDescriptionFile info;
    public static WorldEditPlugin wePlugin;

    private final ConfigurationManager configuration;
    private final GlobalRegionManager globalRegionManager;

    private PluginManager pm;

    public InfiniteBlocks() {
        configuration = new ConfigurationManager(this);
        globalRegionManager = new GlobalRegionManager(this);
    }

    @Override
    public void onEnable() {
        info = getDescription();
        plugin = this;
        pm = getServer().getPluginManager();

        // Get the WorldEdit plugin.
        if (pm.isPluginEnabled("WorldEdit")) {
            wePlugin = (WorldEditPlugin) pm.getPlugin("WorldEdit");
            printToConsole("WorldEdit found!", false);
        } else {
            printToConsole("WorldEdit not found! Disabling InfiniteBlocks.", true);
            pm.disablePlugin(this);
        }

        // Set the Command Executor.
        getCommand("infiniteblocks").setExecutor(new InfiniteBlocksCommand(this));

        // Set up and register listeners.
        setupListeners();

        // Load the configuration.
        try {
            configuration.load();
            globalRegionManager.preload();
        } catch (FatalConfigurationLoadingException e) {
            e.printStackTrace();
            pm.disablePlugin(this);
        }

        printToConsole("v" + info.getVersion() + ": Successfully enabled!", false);
    }

    @Override
    public void onDisable() {
        globalRegionManager.unload();
        configuration.unload();
        getServer().getScheduler().cancelTasks(this);
    }

    private void setupListeners() {
        pm.registerEvents(new BlockBreakEventListener(this), this);
        pm.registerEvents(new BlockIgniteListener(this), this);
        pm.registerEvents(new LeafDecayListener(this), this);
    }

    public static void printToConsole(String msg, boolean warn) {
        if (warn) {
            log.warning("[" + info.getName() + "] " + msg);
        } else {
            log.info("[" + info.getName() + "] " + msg);
        }
    }

    public static void printToPlayer(Player p, String msg, boolean warn) {
        String color = "";

        if (warn) {
            color += ChatColor.RED + "";
        } else {
            color += ChatColor.GREEN + "";
        }

        color += "[InfiniteBlocks] ";
        p.sendMessage(color + msg);
    }

    public static boolean foundMA() {
        return foundMA;
    }

    public boolean hasPermission(Player p, String node) {
        return p.hasPermission("infiniteblocks." + node);
    }

    public GlobalRegionManager getGlobalRegionManager() {
        return globalRegionManager;
    }

    public ConfigurationManager getGlobalStateManager() {
        return configuration;
    }

    public static InfiniteBlocks getInstance() {
        return plugin;
    }

    public String[] getGroups(Player player) {
        try {
            return PermissionsResolverManager.getInstance().getGroups(player);
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories.
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = null;
        try {
            JarFile file = new JarFile(getFile());
            ZipEntry copy = file.getEntry("defaults/" + defaultName);
            if (copy == null) throw new FileNotFoundException();
            input = file.getInputStream(copy);
        } catch (IOException e) {
            log.severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length;

                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                printToConsole("Default configuration written: " + actual.getAbsolutePath(), false);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException ignore) {

                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignore) {

                }
            }
        }
    }

    /**
     * Checks if the given block is in any region.
     * Returns null if not in any region. If the block is in
     * multiple regions, the region with the highest priority
     * is returned.
     *
     * @param block The block that we are checking.
     * @return A BlockRegion if the block is in one, or null.
     */
    public BlockRegion getRegionFromBlock(Block block) {
        World world = block.getWorld();
        RegionManager regionManager = getGlobalRegionManager().get(world);

        Location loc = block.getLocation();
        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(loc);

        if (regionSet == null || regionSet.size() == 0) {
            return null;
        } else if (regionSet.size() == 1) {
            return regionSet.iterator().next();
        } else if (regionSet.size() > 1) {
            int highestPriority = regionSet.iterator().next().getPriority();
            BlockRegion highestRegion = regionSet.iterator().next();

            for (BlockRegion region : regionSet) {
                if (highestPriority < region.getPriority()) {
                    highestPriority = region.getPriority();
                    highestRegion = region;
                }
            }

            return highestRegion;
        }

        return null;
    }

    /**
     * Checks if the given block type is in the list of blocks to
     * replace in the configuration files.
     *
     * @param block The block that we are checking.
     * @return If the block is listed in the configuration file.
     */
    public boolean replaceBlock(Block block) {
        Material material = block.getType();

        World world = block.getWorld();
        List<String> blockTypesReplace = getGlobalStateManager().get(world).getBlocksToReplace();

        for (String type : blockTypesReplace) {
            if (material.toString().equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }
}
