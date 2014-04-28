package me.gnat008.infiniteblocks.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.exceptions.RegionDatabaseException;
import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.ApplicableRegionSet;
import me.gnat008.infiniteblocks.regions.BlockCuboidRegion;
import me.gnat008.infiniteblocks.regions.BlockPolygonalRegion;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InfiniteBlocksCommand implements CommandExecutor {

    private InfiniteBlocks plugin;
    private WorldEditPlugin we;

    private enum Action {DEFINE, HELP, INFO, LIST, LOAD, REDEFINE, RELOAD, REMOVE, SETDELAY, SETPARENT, SETPRIORITY}

    public InfiniteBlocksCommand(InfiniteBlocks plugin) {
        this.plugin = plugin;
        we = InfiniteBlocks.wePlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Cancels the command if sent from console
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                try {
                    reload(null);
                } catch (CommandException e) {
                    InfiniteBlocks.printToConsole(e.getMessage(), true);
                }

                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
                if (args.length == 2) {
                    try {
                        load(null, args[1]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToConsole(e.getMessage(), true);
                    }

                    return true;
                } else if (args.length == 1) {
                    try {
                        load(null, null);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToConsole(e.getMessage(), true);
                    }

                    return true;
                }
            }
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
            InfiniteBlocks.printToPlayer(player, "That is not a valid command. Type " + ChatColor.WHITE +
                    "/infiniteblocks" + ChatColor.RED + "for help.", true);
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
                    try {
                        info(player, args[1], true);
                        return true;
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                        return true;
                    }
                } else {
                    try {
                        info(player, null, false);
                        return true;
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                        return true;
                    }
                }

            case LIST:
                if (args.length == 1) {
                    try {
                        list(0, player);
                        return true;
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                        return true;
                    }
                } else if (args.length == 2) {
                    int page;

                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        InfiniteBlocks.printToPlayer(player, "Argument must be a number!", true);
                        return true;
                    }

                    try {
                        list(page, player);
                        return true;
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                        return true;
                    }
                } else {
                    displayHelp(player);
                    return true;
                }

            case LOAD:
                if (args.length == 1) {
                    try {
                        load(player, null);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else if (args.length == 2) {
                    try {
                        load(player, args[1]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }

            case RELOAD:
                if (args.length == 1) {
                    try {
                        reload(player);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }

            case REMOVE:
                if (args.length == 2) {
                    try {
                        remove(player, args[1]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                } else {
                    displayHelp(player);
                    return true;
                }

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

            case SETDELAY:
                if (args.length == 3) {
                    try {
                        int delay;
                        try {
                            delay = Integer.parseInt(args[2]);
                        } catch (ClassCastException e) {
                            displayHelp(player);
                            return true;
                        }

                        setDelay(player, args[1], delay);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                }

            case SETPARENT:
                if (args.length > 3 || args.length < 3) {
                    displayHelp(player);
                    return true;
                } else {
                    try {
                        setParent(player, args[1], args[2]);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                }

            case SETPRIORITY:
                if (args.length == 3) {
                    try {
                        int priority;
                        try {
                            priority = Integer.parseInt(args[2]);
                        } catch (ClassCastException e) {
                            displayHelp(player);
                            return true;
                        }

                        setPriority(player, args[1], priority);
                    } catch (CommandException e) {
                        InfiniteBlocks.printToPlayer(player, e.getMessage(), true);
                    }

                    return true;
                }
        }

        return false;
    }

    private String validateRegionId(String id, boolean allowGlobal) throws CommandException {
        if (!BlockRegion.isValidId(id)) {
            throw new CommandException("The name '" + id + "' contains characters that are not allowed!");
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) {
            throw new CommandException("Sorry, you cant use '__global__' here.");
        }

        return id;
    }

    private BlockRegion findExistingRegion(RegionManager regionManager, String id, boolean allowGlobal) throws CommandException {
        // Validate the ID.
        validateRegionId(id, allowGlobal);

        BlockRegion region = regionManager.getRegionExact(id);

        // No region found?
        if (region == null) {
            throw new CommandException("No region could be found with a name of '" + id + "'.");
        }

        return region;
    }

    private BlockRegion findRegionStandingIn(RegionManager regionManager, Player player) throws CommandException {
        return findRegionStandingIn(regionManager, player, false);
    }

    private BlockRegion findRegionStandingIn(RegionManager regionManager, Player player, boolean allowGlobal) throws CommandException {
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());

        if (set.size() == 0) {
            throw new CommandException("You are not standing in a region. Specify an ID if you want to select a specific region.");
        } else if (set.size() > 1) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;

            for (BlockRegion region : set) {
                if (!first) {
                    builder.append(", ");
                }

                first = false;

                builder.append(region.getId());
            }

            throw new CommandException("You are standing in multiple regions. InfiniteBlocks is not sure what one you want.\nYou're in: " +
                    builder.toString());
        }

        return set.iterator().next();
    }

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

        String help = "";
        help += "\n";
        help += ChatColor.GOLD;
        help += "\n---------------------";
        help += "\n     InfiniteBlocks Help Page:";
        help += "\n---------------------";
        help += "\n" + ChatColor.GOLD + "Version: " + ChatColor.GREEN + version;
        help += "\n" + ChatColor.GOLD + "Author: " + ChatColor.GREEN + authors;
        help += "\n" + ChatColor.GOLD + "---------------------";
        help += "\nCommands Usage: {/infiniteblocks, /ib} <cmd> [args]";
        help += "\nKey: cmd <required> [optional]";
        help += ChatColor.WHITE;
        help += "\ndefine <id>, help, info [id], list, load [world], redefine <id>, reload, remove <id>, " +
                "setdelay <id> <delay-seconds>, setparent <parent_id> <child_id>, setpriority <id> <priority>";
        help += "\n";

        player.sendMessage(help);
    }

    // Define a new region.
    public void define(Player player, String id) throws CommandException {
        // Get and validate the region ID.
        String validID = validateRegionId(id, false);

        // Can't replace regions with this command.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(player.getWorld());
        if (regionManager.hasRegion(validID)) {
            throw new CommandException("Region with that name is already defined! To change it's shape, use /region redefine " + validID);
        }

        // Make a region from the user's selection.
        BlockRegion region = createRegionFromSelection(player, validID);

        // Set the region's owner.
        region.setOwner(player);

        // Set the region's delay.
        region.setDelay(plugin.getGlobalStateManager().get(player.getWorld()).defaultDelay);

        // Set the region's priority.
        region.setPriority(0);

        regionManager.addRegion(region);
        commitChanges(player, regionManager);

        // Tell the user.
        InfiniteBlocks.printToPlayer(player, "A new region had been created: " + ChatColor.WHITE + validID, false);
    }

    // Redefine a region with a new selection.
    public void redefine(Player player, String id) throws CommandException {
        World world = player.getWorld();

        // Get and validate the region ID.
        String validID = validateRegionId(id, false);

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion existing = findExistingRegion(regionManager, validID, false);

        // Make a region from the user's selection.
        BlockRegion region = createRegionFromSelection(player, validID);

        // Copy details from the old region to the new one.
        region.setOwner(existing.getOwnerUUID());
        region.setDelay(existing.getDelay());
        region.setPriority(existing.getPriority());
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
    public void info(Player player, String id, boolean fromID) throws CommandException {
        World world = player.getWorld();

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion existing;

        if (!fromID) {
            existing = findRegionStandingIn(regionManager, player, true);
        } else {
            existing = findExistingRegion(regionManager, id, true);
        }

        // Print the region's information.
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing);
        printout.appendRegionInfo();
        printout.send(player);
    }

    // List the regions.
    public void list(int page, Player player) throws CommandException {
        World world = player.getWorld();

        // Get page.
        if (page < 0) {
            page = 0;
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        Map<String, BlockRegion> regions = mgr.getRegions();

        // Build a list of regions to show.
        List<RegionListEntry> entries = new ArrayList<RegionListEntry>();

        int index = 0;
        for (String id : regions.keySet()) {
            RegionListEntry entry = new RegionListEntry(id, index++);

            entries.add(entry);
        }

        Collections.sort(entries);

        final int totalSize = entries.size();
        final int pageSize = 10;
        final int pages = (int) Math.ceil(totalSize / (float) pageSize);

        player.sendMessage(ChatColor.RED + "Regions (page " + (page + 1) + " of " + pages + "):");

        if (page < pages) {
            // Print.
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (i >= totalSize) {
                    break;
                }

                player.sendMessage(ChatColor.YELLOW.toString() + entries.get(i));
            }
        }
    }

    // Set the parent of a region.
    public void setParent(Player player, String parent, String child) throws CommandException {
        World world = player.getWorld();

        BlockRegion parentRegion;
        BlockRegion childRegion;

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);

        // Get parent and child.
        childRegion = findExistingRegion(regionManager, child, false);
        if (parent != null) {
            parentRegion = findExistingRegion(regionManager, parent, false);
        } else {
            parentRegion = null;
        }

        try {
            childRegion.setParent(parentRegion);
            childRegion.setPriority(parentRegion.getPriority() + 1);
        } catch (BlockRegion.CircularInheritanceException e) {
            // Tell the user what's wrong.
            RegionPrintoutBuilder printout = new RegionPrintoutBuilder(parentRegion);
            printout.append(ChatColor.RED);
            printout.append("Uh oh! Setting '" + parentRegion.getId() + "' to be the parent " +
                    "of '" + childRegion.getId() + "' would cause circular inheritance.\n");
            printout.append(ChatColor.GRAY);
            printout.append("(Current inheritance on '" + parentRegion.getId() + "':\n");
            printout.appendParentTree(true);
            printout.append(ChatColor.GRAY);
            printout.append(")");
            printout.send(player);
            return;
        } catch (NullPointerException e) {
            throw new CommandException("Region '" + parent + "' not found!");
        }

        // Save to disk.
        commitChanges(player, regionManager);

        // Tell the user the current inheritance.
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(childRegion);
        printout.append(ChatColor.YELLOW);
        printout.append("Inheritance set for region '" + childRegion.getId() + "'.\n");
        if (parent != null) {
            printout.append(ChatColor.GRAY);
            printout.append("(Current inheritance:\n");
            printout.appendParentTree(true);
            printout.append(ChatColor.GRAY);
            printout.append(")");
        }

        printout.send(player);
    }

    // Remove a region.
    public void remove(Player player, String id) throws CommandException {
        World world = player.getWorld();

        // Look up the existing region.
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion existing = findExistingRegion(regionManager, id, true);

        regionManager.removeRegion(id);
        commitChanges(player, regionManager);

        InfiniteBlocks.printToPlayer(player, "Region '" + existing.getId() + "' removed.", false);
    }

    // Reload the region database.
    public void load(CommandSender sender, String worldName) throws CommandException {
        World world = null;
        try {
            world = plugin.getServer().getWorld(worldName);
        } catch (Exception e) {
            // Assume the user wants to reload all worlds.
        }

        if (world != null) {
            RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
            if (regionManager == null) {
                throw new CommandException("No region manager exists for world '" + world.getName() + "'!");
            }

            reloadChanges(sender, regionManager);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Loading all region databases. This may take a while.");
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager regionManager = plugin.getGlobalRegionManager().get(w);
                if (regionManager == null) {
                    continue;
                }

                reloadChanges(sender, regionManager, true);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Region databases reloaded!");
    }

    // Reload the configuration.
    public void reload(CommandSender sender) throws CommandException {
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;

        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);

            minecraftLogger = plugin.getLogger();
            minecraftLogger.addHandler(handler);
        }

        try {
            plugin.getGlobalStateManager().unload();
            plugin.getGlobalRegionManager().unload();

            plugin.getGlobalStateManager().load();
            plugin.getGlobalRegionManager().preload();

            InfiniteBlocks.printToConsole("InfiniteBlocks configuration reloaded.", false);
        } catch (Throwable t) {
            InfiniteBlocks.printToConsole("Error while reloading: " + t.getMessage(), true);
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }

    // Set the replace delay for a region.
    public void setDelay(Player player, String id, int seconds) throws CommandException {
        World world = player.getWorld();

        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion region = regionManager.getRegion(id);

        if (region != null) {
            region.setDelay(seconds);
            commitChanges(player, regionManager);

            InfiniteBlocks.printToPlayer(player, "New delay set for region: " + ChatColor.WHITE + id, false);
        } else {
            throw new CommandException("Could not find region '" + id + "'!");
        }
    }

    // Set the priority for a region.
    public void setPriority(Player player, String id, int priority) throws CommandException {
        World world = player.getWorld();
        String uuid = player.getUniqueId().toString();

        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        BlockRegion region = regionManager.getRegion(id);

        if (region != null) {
            if (region.getOwnerUUID().equals(uuid)) {
                region.setPriority(priority);

                InfiniteBlocks.printToPlayer(player, "New priority set for region: " + ChatColor.WHITE + id, false);
            } else {
                throw new CommandException("You are not the owner of this region!");
            }
        } else {
            throw new CommandException("Could not find region '" + id + "'!");
        }
    }
}
