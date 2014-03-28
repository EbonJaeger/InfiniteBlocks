package me.gnat008.infiniteblocks;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.ArenaMaster;
import com.sk89q.worldedit.WorldEdit;
import me.gnat008.infiniteblocks.config.InfiniteBlocksConfig;
import me.gnat008.infiniteblocks.listeners.MobArenaListener;
import me.gnat008.infiniteblocks.util.YAMLConfig;
import me.gnat008.infiniteblocks.util.YAMLConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class InfiniteBlocks extends JavaPlugin {

    private static Logger log = Bukkit.getServer().getLogger();
    private static PluginDescriptionFile info;

    private static boolean foundMA = false, foundWE = false;

    public static ArenaMaster am;
    public static WorldEdit wePlugin;

    private PluginManager pm;
    private YAMLConfigManager configManager;
    private YAMLConfig mainConfig;
    private YAMLConfig regionConfig;

    public InfiniteBlocksConfig config;

    @Override
    public void onEnable() {
        configManager = new YAMLConfigManager(this);
        info = getDescription();
        pm = getServer().getPluginManager();

        // Get the WorldEdit plugin
        if (pm.isPluginEnabled("WorldEdit")) {
            wePlugin = (WorldEdit) pm.getPlugin("WorldEdit");
            foundWE = true;
            printToConsole("WorldEdit found!", false);
        } else {
            printToConsole("WorldEdit not found! Disabling InfiniteBlocks.", true);
            pm.disablePlugin(this);
        }

        // Generate the config.yml file
        String[] header1 = {"InfiniteBlocks Configuration File", "---------------------", "Created by Gnat008"};
        try {
            mainConfig = configManager.getNewConfig("config.yml", header1);
        } catch (Exception e) {
            printToConsole("Configuration file 'config.yml' generation failed.", true);
            e.printStackTrace();
        }

        config = new InfiniteBlocksConfig(mainConfig);
        mainConfig.reloadConfig();

        // Generate the region log file
        String[] header2 = {"InfiniteBlocks Region File", "---------------------", "Stores region data."};
        try {
            regionConfig = configManager.getNewConfig("data/regions.yml", header2);
        } catch (Exception e) {
            printToConsole("Configuration file 'regions.yml' generation failed.", true);
            e.printStackTrace();
        }

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

    public static boolean foundWE() {
        return foundWE;
    }

    public YAMLConfig getMainConfig() {
        return mainConfig;
    }

    public YAMLConfig getRegionsConfig() {
        return regionConfig;
    }
}
