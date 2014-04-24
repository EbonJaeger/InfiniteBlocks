package me.gnat008.infiniteblocks.managers;

import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.config.ConfigurationManager;
import me.gnat008.infiniteblocks.databases.RegionDatabase;
import me.gnat008.infiniteblocks.databases.YAMLDatabase;
import me.gnat008.infiniteblocks.exceptions.RegionDatabaseException;
import org.bukkit.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalRegionManager {

    private InfiniteBlocks plugin;
    private ConfigurationManager config;
    private ConcurrentHashMap<String, RegionManager> managers;
    private HashMap<String, Long> lastModified;

    public GlobalRegionManager(InfiniteBlocks plugin) {
        this.plugin = plugin;
        config = plugin.getGlobalStateManager();
        managers = new ConcurrentHashMap<String, RegionManager>();
        lastModified = new HashMap<String, Long>();
    }

    // Unload region information.
    public void unload() {
        managers.clear();
        lastModified.clear();
    }

    // Get the path for a world's regions file.
    protected File getPath(String name) {
        return new File(plugin.getDataFolder(), "worlds" + File.separator + name + File.separator + "regions.yml");
    }

    // Unload region information for a world.
    public void unload(String name) {
        RegionManager manager = managers.remove(name);

        if (manager != null) {
            lastModified.remove(name);
        }
    }

    // Unload all region information.
    public void unloadAll() {
        managers.clear();
        lastModified.clear();
    }

    public RegionManager load(World world) {
        RegionManager manager = create(world);
        managers.put(world.getName(), manager);
        return manager;
    }

    // Load region information for a world.
    public RegionManager create(World world) {
        String name = world.getName();
        RegionDatabase database;
        File file = null;

        try {
            file = getPath(name);
            database = new YAMLDatabase(file, plugin.getLogger());

            // Store the last modified date so we can track changes.
            lastModified.put(name, file.lastModified());

            // Create a manager.
            RegionManager manager = new PRTreeRegionManager(database);
            manager.load();

            if (plugin.getGlobalStateManager().get(world).summaryOnStart) {
                InfiniteBlocks.printToConsole(manager.getRegions().size() + " regions loaded for '" + name + "'.", false);
            }

            return manager;
        } catch (RegionDatabaseException e) {
            String logStr = "Failed to load regions from file \"" + file + "\" ";

            plugin.getLogger().severe(logStr + " : " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            plugin.getLogger().severe("Error loading regions for world \"" + name + "\": " + e.toString() + "\n\t" + e.getMessage());
            e.printStackTrace();
        }

        // THIS CREATES PROBLEMS!!one!!1!!eleven!!1!!!
        return null;
    }

    // Preloads region managers for all worlds.
    public void preload() {
        for (World world : plugin.getServer().getWorlds()) {
            load(world);
        }
    }

    // Reloads regions from file when region databases have been changed.
    public void reloadChanged() {
        if (config.useSqlDatabase) return;

        for (String name : managers.keySet()) {
            File file = getPath(name);

            Long oldDate = lastModified.get(name);

            if (oldDate == null) {
                oldDate = 0L;
            }

            try {
                if (file.lastModified() > oldDate) {
                    World world = plugin.getServer().getWorld(name);

                    if (world != null) {
                        load(world);
                    }
                }
            } catch (Exception ignore) {

            }
        }
    }

    // Get the region manager for a particular world.
    public RegionManager get(World world) {
        RegionManager manager = managers.get(world.getName());
        RegionManager newManager = null;

        while (manager == null) {
            if (newManager == null) {
                newManager = create(world);
            }

            managers.putIfAbsent(world.getName(), newManager);
            manager = managers.get(world.getName());
        }

        return manager;
    }
}
