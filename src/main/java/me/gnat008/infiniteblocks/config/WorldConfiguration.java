package me.gnat008.infiniteblocks.config;

import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.util.yaml.YAMLFormat;
import me.gnat008.infiniteblocks.util.yaml.YAMLProcessor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldConfiguration {

    private final String CONFIG_HEADER = "#\r\n" +
            "# InfiniteBlock's world configuration file\r\n" +
            "#\r\n" +
            "# This is a world configuration file. Anything placed into here will only\r\n" +
            "# affect this world. If you don't put anything in this file, then the\r\n" +
            "# settings will be inherited from the main configuration file.\r\n" +
            "#\r\n" +
            "# If you see {} below, that means that there are NO entries in this file.\r\n" +
            "# Remove the {} and add your own entries.\r\n" +
            "#\r\n";

    private InfiniteBlocks plugin;

    private String worldName;
    private YAMLProcessor parentConfig;
    private YAMLProcessor config;

    // Configuration data start.
    public boolean summaryOnStart;

    public int defaultDelay;
    public int maxRegionCountPerPlayer;

    public List<String> blocksToReplace;

    private Map<String, Integer> maxRegionCounts;
    // Configuration data end.

    public WorldConfiguration(InfiniteBlocks plugin, String worldName, YAMLProcessor parentConfig) {
        File baseFolder = new File(plugin.getDataFolder(), "worlds/" + worldName);
        File configFile = new File(baseFolder, "config.yml");

        this.plugin = plugin;
        this.worldName = worldName;
        this.parentConfig = parentConfig;

        plugin.createDefaultConfiguration(configFile, "config_world.yml");

        config = new YAMLProcessor(configFile, true, YAMLFormat.EXTENDED);
        loadConfiguration();

        if (summaryOnStart) {
            InfiniteBlocks.printToConsole("Loaded configuration file for '" + worldName + "'.", false);
        }
    }

    private boolean getBoolean(String node, boolean def) {
        boolean val = parentConfig.getBoolean(node, def);

        if (config.getProperty(node) != null) {
            return config.getBoolean(node, def);
        } else {
            return val;
        }
    }

    private String getString(String node, String def) {
        String val = parentConfig.getString(node, def);

        if (config.getProperty(node) != null) {
            return config.getString(node, def);
        } else {
            return val;
        }
    }

    private int getInt(String node, int def) {
        int val = parentConfig.getInt(node, def);

        if (config.getProperty(node) != null) {
            return config.getInt(node, def);
        } else {
            return val;
        }
    }

    private double getDouble(String node, double def) {
        double val = parentConfig.getDouble(node, def);

        if (config.getProperty(node) != null) {
            return config.getDouble(node, def);
        } else {
            return val;
        }
    }

    private List<Integer> getIntegerList(String node, List<Integer> def) {
        List<Integer> res = parentConfig.getIntList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<Integer>());
        }

        if (config.getProperty(node) != null) {
            res = config.getIntList(node, def);
        }

        return res;
    }

    private List<String> getStringList(String node, List<String> def) {
        List<String> res = parentConfig.getStringList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<String>());
        }

        if (config.getProperty(node) != null) {
            res = config.getStringList(node, def);
        }

        return res;
    }

    private List<String> getKeys(String node) {
        List<String> res = parentConfig.getKeys(node);

        if (res == null || res.size() == 0) {
            res = config.getKeys(node);
        }

        if (res == null) {
            res = new ArrayList<String>();
        }

        return res;
    }

    private Object getProperty(String node) {
        Object res = parentConfig.getProperty(node);

        if (config.getProperty(node) != null) {
            res = config.getProperty(node);
        }

        return res;
    }

    // Load the configuration.
    private void loadConfiguration() {
        try {
            config.load();
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading configuration file for world '" + worldName + "': ");
            e.printStackTrace();
        }

        summaryOnStart = getBoolean("summary-on-start", true);

        maxRegionCountPerPlayer = getInt("regions.max-region-count-per-player.default", 7);
        maxRegionCounts = new HashMap<String, Integer>();
        maxRegionCounts.put(null, maxRegionCountPerPlayer);

        for (String key : getKeys("regions.max-region-count-per-player")) {
            if (!key.equalsIgnoreCase("default")) {
                Object val = getProperty("regions.max-region-count-per-player." + key);
                if (val != null && val instanceof Number) {
                    maxRegionCounts.put(key, ((Number) val).intValue());
                }
            }
        }

        defaultDelay = getInt("regions.default-delay-seconds", 10);

        List<String> blocksToReplaceDef = new ArrayList<String>();
        blocksToReplaceDef.add("tnt");
        blocksToReplace = getStringList("regions.blocks-to-replace", blocksToReplaceDef);

        config.setHeader(CONFIG_HEADER);

        config.save();
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getMaxRegionCount(Player player) {
        int max = -1;

        for (String group : plugin.getGroups(player)) {
            if (maxRegionCounts.containsKey(group)) {
                int groupMax = maxRegionCounts.get(group);
                if (max < groupMax) {
                    max = groupMax;
                }
            }
        }

        if (max <= -1) {
            max = maxRegionCountPerPlayer;
        }

        return max;
    }

    public int getDefaultDelay() {
        return defaultDelay;
    }

    public List<String> getBlocksToReplace() {
        return blocksToReplace;
    }
}
