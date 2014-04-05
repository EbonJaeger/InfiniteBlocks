package me.gnat008.infiniteblocks;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.ArenaMaster;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.gnat008.infiniteblocks.command.InfiniteBlocksCommand;
import me.gnat008.infiniteblocks.config.ConfigurationManager;
import me.gnat008.infiniteblocks.listeners.MobArenaListener;
import me.gnat008.infiniteblocks.managers.GlobalRegionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class InfiniteBlocks extends JavaPlugin {

    private static InfiniteBlocks plugin;
    private static Logger log = Bukkit.getServer().getLogger();

    private static boolean foundMA = false;

    public static ArenaMaster am;
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

        // Get the WorldEdit plugin
        if (pm.isPluginEnabled("WorldEdit")) {
            wePlugin = (WorldEditPlugin) pm.getPlugin("WorldEdit");
            printToConsole("WorldEdit found!", false);
        } else {
            printToConsole("WorldEdit not found! Disabling InfiniteBlocks.", true);
            pm.disablePlugin(this);
        }

        // Generate the config.yml file
        String[] header1 = {"InfiniteBlocks Configuration File", "---------------------", "Created by Gnat008"};

        // Set the Command Executor
        getCommand("infiniteblocks").setExecutor(new InfiniteBlocksCommand(this));

        setupListeners();
        printToConsole("v" + info.getVersion() + ": Successfully enabled!", false);
    }

    private void setupListeners() {
        MobArena maPlugin = (MobArena) pm.getPlugin("MobArena");

        if (maPlugin != null && maPlugin.isEnabled()) {
            am = maPlugin.getArenaMaster();
            pm.registerEvents(new MobArenaListener(this), this);
            foundMA = true;
            printToConsole("MobArena found!", false);
        }
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
                int length = 0;

                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                printToConsole("Default configuration written: " + actual.getAbsolutePath(), false);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
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
}
