package me.gnat008.infiniteblocks.config;

import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.util.yaml.YAMLFormat;
import me.gnat008.infiniteblocks.util.yaml.YAMLProcessor;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigurationManager {

    private final String CONFIG_HEADER = "# InfiniteBlock's main configuration file\r\n" +
            "#\r\n" +
            "# This is the global configuration file. Anything placed into here will\r\n" +
            "# be applied to all worlds. However, each world has its own configuration\r\n" +
            "# file to allow you to replace most settings in here for that world only.\r\n" +
            "#\r\n" +
            "# About editing this file:\r\n" +
            "# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n" +
            "#   you use an editor like Notepad++ (recommended for Windows users), you\r\n" +
            "#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n" +
            "#   be changed in Settings > Preferences > Language Menu.\r\n" +
            "# - Don't get rid of the indents. They are indented so some entries are\r\n" +
            "#   in categories.\r\n" +
            "# - If you want to check the format of this file before putting it\r\n" +
            "#   into InfiniteBlocks, paste it into http://yaml-online-parser.appspot.com/\r\n" +
            "#   and see if it gives \"ERROR:\".\r\n" +
            "# - Lines starting with # are comments and so they are ignored.\r\n" +
            "#\r\n";

    private InfiniteBlocks plugin;
    private ConcurrentMap<String, WorldConfiguration> worlds;
    private YAMLProcessor config;

    public Map<String, String> hostKeys = new HashMap<String, String>();

    public boolean useSqlDatabase = false;
    public String sqlDsn;
    public String sqlUsername;
    public String sqlPassword;

    public ConfigurationManager(InfiniteBlocks plugin) {
        this.plugin = plugin;
        this.worlds = new ConcurrentHashMap<String, WorldConfiguration>();
    }

    // Load the configuration.
    @SuppressWarnings("unchecked")
    public void load() {
        // Create the default configuration file.
        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "config.yml"), "config.yml");

        config = new YAMLProcessor(new File(plugin.getDataFolder(), "config.yml"), true, YAMLFormat.EXTENDED);
        try {
            config.load();
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading configuration for global config: ");
            e.printStackTrace();
        }

        hostKeys = new HashMap<String, String>();
        Object hostKeysRaw = config.getProperty("host-keys");
        if (hostKeysRaw == null || !(hostKeysRaw instanceof Map)) {
            config.setProperty("host-keys", new HashMap<String, String>());
        } else {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) hostKeysRaw).entrySet()) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                hostKeys.put(key.toLowerCase(), value);
            }
        }

        useSqlDatabase = config.getBoolean("regions.use-sql", false);
        sqlDsn = config.getString("regions.sql.dsn", "jbdc:mysql://localhost/infiniteblocks");
        sqlUsername = config.getString("regions.sql.username", "username");
        sqlPassword = config.getString("regions.sql.password", "password");

        // Load configurations for each world.
        for (World world : plugin.getServer().getWorlds()) {
            get(world);
        }

        config.setHeader(CONFIG_HEADER);

        if (!config.save()) {
            plugin.getLogger().severe("Error saving configuration!");
        }
    }

    // Unload the configuration.
    public void unload() {
        worlds.clear();
    }

    // Get the configuration for a world.
    public WorldConfiguration get(World world) {
        String worldName = world.getName();
        WorldConfiguration config = worlds.get(worldName);
        WorldConfiguration newConfig = null;

        while (config == null) {
            if (newConfig == null) {
                newConfig = new WorldConfiguration(plugin, worldName, this.config);
            }

            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }
}
