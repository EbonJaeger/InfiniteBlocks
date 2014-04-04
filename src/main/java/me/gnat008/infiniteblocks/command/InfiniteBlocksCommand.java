package me.gnat008.infiniteblocks.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import me.gnat008.infiniteblocks.InfiniteBlocks;
import me.gnat008.infiniteblocks.managers.RegionManager;
import me.gnat008.infiniteblocks.regions.BlockCuboidRegion;
import me.gnat008.infiniteblocks.regions.BlockPolygonalRegion;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import me.gnat008.infiniteblocks.util.old.YAMLConfig;
import org.bukkit.ChatColor;
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

    private enum Action {HELP, INFO, LIST, RELOAD, REMOVE, SET}

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

            case SET:
                if (args.length == 2) {
                    if (we.getSelection(player) != null) {
                        Selection selection = we.getSelection(player);


                        return true;
                    } else {
                        InfiniteBlocks.printToPlayer(player, "You must have a selection first!", true);
                        return true;
                    }
                } else {
                    displayHelp(player);
                    return true;
                }
        }

        return false;
    }

    private void displayHelp(Player player) {
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

    private void reload(Player player) {
        mainConfig.reloadConfig();

        InfiniteBlocks.printToConsole("Config file has been reloaded!", false);

        if (player != null) {
            InfiniteBlocks.printToPlayer(player, "Config file has been reloaded!", false);
        }
    }

    private void setRegion(Player player, String id, BlockVector pt1, BlockVector pt2) {
        /*if (!regionsConfig.contains("regions." + id)) {
            BlockRegion region = new BlockRegion(id, pt1, pt2);

            if (!(region.isValidId(id))) {
                InfiniteBlocks.printToPlayer(player, "That is not a valid id!", true);
                return;
            }

            String min = region.getMin().toString();
            String max = region.getMax().toString();
            String owner = region.getOwner();

            regionsConfig.set("regions." + id + ".min", min);
            regionsConfig.set("regions." + id + ".max", max);
            regionsConfig.set("regions." + id + ".owner", owner);
            regionsConfig.saveConfig();
            regionsConfig.reloadConfig();

            InfiniteBlocks.printToPlayer(player, "Region created successfully!", false);
        } else {
            InfiniteBlocks.printToPlayer(player, "A region with that name already exists!", true);
        }*/
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

}
