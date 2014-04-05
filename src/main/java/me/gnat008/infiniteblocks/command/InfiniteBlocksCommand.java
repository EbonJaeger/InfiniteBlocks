package me.gnat008.infiniteblocks.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.databases.RegionDatabaseException;
import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.BlockCuboidRegion;
import me.gnat008.infiniteblocks.regions.BlockPolygonalRegion;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import me.gnat008.infiniteblocks.util.old.YAMLConfig;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InfiniteBlocksCommand implements CommandExecutor {

    private InfiniteBlocks plugin;
    private WorldEditPlugin we;
    private YAMLConfig mainConfig;
    private YAMLConfig regionsConfig;

    private enum Action {DEFINE, HELP, INFO, LIST, REDEFINE, RELOAD, REMOVE}

    public InfiniteBlocksCommand(InfiniteBlocks plugin) {
        this.plugin = plugin;
        we = InfiniteBlocks.wePlugin;
        mainConfig = plugin.getMainConfig();
        regionsConfig = plugin.getRegionsConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Cancels the command if sent from console
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reload(null);
            }

            return true;
        }

        Player player = (Player) sender;

        // Displays the plugin's help message to the player
        if (args.length == 0) {
            displayHelp(player);
            return true;
        }

        Action action;

        try {
            action = Action.valueOf(args[0].toUpperCase());
        } catch (Exception notEnum) {
            InfiniteBlocks.printToPlayer(player, "That is not a valid command. Type " + ChatColor.WHITE + "/infiniteblocks" + ChatColor.RED + "for help.", true);
            return true;
        }

        // Check if the player has permission to do the command; Cancel if false
        if (!plugin.hasPermission(player, args[0]) && !args[0].equalsIgnoreCase("help")) {
            InfiniteBlocks.printToPlayer(player, "You do not have permission to do that!", true);
            return true;
        }

        // Execute the correct command
        switch (action) {
            case HELP:
                displayHelp(player);
                return true;

            case INFO:
                if (args.length == 2) {
                    if (regionsConfig.contains("regions." + args[1])) {
                        //displayInfo(player, args[1]);
                        return true;
                    } else {
                        InfiniteBlocks.printToPlayer(player, "That region does not exist!", true);
                        return true;
                    }
                } else if (args.length == 1) {

                }

            case RELOAD:
                if (args.length == 1) {
                    reload(player);
                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }

            case REMOVE:
                //removeRegion(player, region);
                return true;

            case DEFINE:
                if (args.length == 2) {
                    try {
                        define(player, args[1]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }

            case REDEFINE:
                if (args.length == 2) {
                    try {
                        redefine(player, args[1]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }
        }

        return false;
    }

    private World getWorld()

    private Selection getSelection(Player player) {
        Selection selection = we.getSelection(player);

        if (selection == null) {
            InfiniteBlocks.printToPlayer(player, "You must have a selection first!", true);
            return null;
        }

        return selection;
    }

    // Create s region from a player's selection.
    private BlockRegion createRegionFromSelection(Player player, String id) throws CommandException {
        Selection selection = getSelection(player);

        // Detect the type of region
        if (selection instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) selection;

            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();

            return new BlockPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (selection instanceof CuboidSelection) {
            BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
            BlockVector max = selection.getNativeMaximumPoint().toBlockVector();

            return new BlockCuboidRegion(id, min, max);
        } else {
            throw new CommandException("You can only use cuboids and polygons for InfiniteBlock regions!");
        }
    }

    private void commitChanges(CommandSender sender, RegionManager regionManager) throws CommandException {
        commitChanges(sender, regionManager, false);
    }

    private void reloadChanges(CommandSender sender, RegionManager regionManager) throws CommandException {
        reloadChanges(sender, regionManager, false);
    }

    // Save the region database.
    private void commitChanges(CommandSender sender, RegionManager regionManager, boolean silent) throws CommandException {
        try {
            if (!silent && regionManager.getRegions().size() >= 500) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    InfiniteBlocks.printToPlayer(player, "Saving region list to disk.", false);
                } else {
                    InfiniteBlocks.printToConsole("Saving region list to disk.", false);
                }
            }

            regionManager.save();
        } catch (RegionDatabaseException e) {
            throw new CommandException("Regions did not save: " + e.getMessage());
        }
    }

    // Load the region database.
    private void reloadChanges(CommandSender sender, RegionManager regionManager, boolean silent) throws CommandException {
        try {
            if (!silent && regionManager.getRegions().size() >= 500) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    InfiniteBlocks.printToPlayer(player, "Loading region list from disk.", false);
                } else {
                    InfiniteBlocks.printToConsole("Loading region list from disk.", false);
                }
            }

            regionManager.load();
        } catch (RegionDatabaseException e) {
            throw new CommandException("Regions did not load: " + e.getMessage());
        }
    }

    public void displayHelp(Player player) {
        String version = InfiniteBlocks.info.getVersion();
        List<String> authors = InfiniteBlocks.info.getAuthors();

        String[] msg = {"---------------------",
                ChatColor.GOLD + "     InfiniteBlocks Help Page:",
                "---------------------",
                ChatColor.GOLD + "Version: " + ChatColor.GREEN + version,
                ChatColor.GOLD + "Author: " + ChatColor.GREEN + authors,
                "---------------------",
                ChatColor.WHITE + "/infiniteblocks help" + ChatColor.GOLD + " - Displays this help page.",
                ChatColor.WHITE + "/infiniteblocks reload" + ChatColor.GOLD + " - Reloads the config.",
                ChatColor.WHITE + "/infiniteblocks set <region>" + ChatColor.GOLD + " - Sets the selected region.",
                ChatColor.WHITE + "/infiniteblocks remove <region>" + ChatColor.GOLD + " - Removes the region with that name."
        };

        player.sendMessage(msg);
    }

    public void reload(Player player) {
        mainConfig.reloadConfig();

        InfiniteBlocks.printToConsole("Config file has been reloaded!", false);

        if (player != null) {
            InfiniteBlocks.printToPlayer(player, "Config file has been reloaded!", false);
        }
    }

    // Define a new region.
    public void define(Player player, String id) throws CommandException {
        // Get and validate the region ID.
        String validID = validateRegionID(id, false);

        // Can't replace regions with this command.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(player.getWorld());
        if (regionManager.hasRegion(validID)) {
            throw new CommandException("Region with that name is already defined! To change it's shape, use /region redefine " + validID);
        }

        // Make a region from the user's selection.
        BlockRegion region = createRegionFromSelection(player, validID);

        // Set the region's owner.
        region.setOwner(player.getUniqueId().toString());

        regionManager.addRegion(region);
        commitChanges(player, regionManager);

        // Tell the user.
        InfiniteBlocks.printToPlayer(player, "A new region had been created: " + ChatColor.WHITE + validID, false);
    }

    // Redefine a region with a new selection.
    public void redefine(Player player, String id) throws CommandException {
        World world = player.getWorld();

        // Get and validate the region ID.
        String validID = validateRegionID(id, false);

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion existing = findExistingRegion(regionManager, validID, false);

        // Make a region from the user's selection.
        BlockRegion region = createRegionFromSelection(player, validID);

        // Copy details from the old region to the new one.
        region.setOwner(existing.getOwner());
        try {
            region.setParent(existing.getParent());
        } catch (BlockRegion.CircularInheritanceException ignore) {
            // This should not be thrown.
        }

        regionManager.addRegion(region);
        commitChanges(player, regionManager);

        // Tell the user.
        InfiniteBlocks.printToPlayer(player, "Region updated with new area: " + ChatColor.WHITE + validID, false);
    }

    // Get info about a region.
    public void info(CommandSender sender, String id, boolean fromID) throws CommandException {
        World world = getWorld(sender, 'w');

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion existing;

        if (!fromID) {
            if (!(sender instanceof Player)) {
                throw new CommandException("Please specify the region with /infiniteblocks info <world_name> <region_id>.");
            }

            existing = findRegionStandingIn(regionManager, (Player) sender, true);
        } else {
            existing = findExistingRegion(regionManager, id, true);
        }

        // Print the region's information.
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing);
        printout.appendRegionInfo(existing);
        printout.send(sender);
    }

    // List the regions.
    public void list(CommandSender sender) throws CommandException {

    }
}
