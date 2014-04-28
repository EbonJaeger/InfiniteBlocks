package me.gnat008.infiniteblocks.command;

import com.sk89q.worldedit.BlockVector;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Gnat008 on 4/22/2014.
 */
public class RegionPrintoutBuilder {

    private final BlockRegion region;
    private final StringBuilder builder = new StringBuilder();

    public RegionPrintoutBuilder(BlockRegion region) {
        this.region = region;
    }

    private void newLine() {
        builder.append("\n");
    }

    // Add region name and type.
    public void appendBasics() {
        builder.append(ChatColor.BLUE);
        builder.append("Region: ");
        builder.append(ChatColor.YELLOW);
        builder.append(region.getId());

        newLine();

        builder.append(ChatColor.GRAY);
        builder.append("Type: ");
        builder.append(region.getTypeName());

        newLine();
    }

    // Add information about parents.
    public void appendParents() {
        appendParentTree(true);
    }

    public void appendParentTree(boolean useColors) {
        if (region.getParent() == null) {
            return;
        }

        builder.append("Parents: ");

        List<BlockRegion> inheritance = new ArrayList<BlockRegion>();

        BlockRegion r = region;
        inheritance.add(r);

        while (r.getParent() != null) {
            r = r.getParent();
            inheritance.add(r);
        }

        ListIterator<BlockRegion> it = inheritance.listIterator(inheritance.size());

        int indent = 0;
        while (it.hasPrevious()) {
            BlockRegion cur = it.previous();

            if (useColors) {
                builder.append(ChatColor.GREEN);
            }

            // Put symbol for child.
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    builder.append("  ");
                }

                builder.append("\u2517");
            }

            // Put name.
            builder.append(cur.getId());

            indent++;
            newLine();
        }
    }

    // Add information about owner.
    public void appendOwner() {
        builder.append(ChatColor.BLUE);
        builder.append("Owner: ");

        String owner = region.getOwnerName();
        if (owner != null) {
            builder.append(ChatColor.YELLOW);
            builder.append(owner);
        } else {
            builder.append(ChatColor.RED);
            builder.append("(no owner)");
        }

        newLine();
    }

    // Add information about delay.
    public void appendDelay() {
        builder.append(ChatColor.BLUE);
        builder.append("Delay: ");

        String delay = String.valueOf(region.getDelay());
        builder.append(ChatColor.YELLOW);
        builder.append(delay);

        newLine();
    }

    // Add information about priority.
    public void appendPriority() {
        builder.append(ChatColor.BLUE);
        builder.append("Priority: ");

        String priority = String.valueOf(region.getPriority());
        builder.append(ChatColor.YELLOW);
        builder.append(priority);

        newLine();
    }

    // Add information about coords.
    public void appendBounds() {
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        builder.append(ChatColor.BLUE);
        builder.append("Bounds: ");
        builder.append(ChatColor.YELLOW);
        builder.append(" (").append(min.getBlockX()).append(",").append(min.getBlockY()).append(",").append(min.getBlockZ()).append(")");
        builder.append(" -> (").append(max.getBlockX()).append(",").append(max.getBlockY()).append(",").append(max.getBlockZ()).append(")");

        newLine();
    }

    // Append all the default fields used in /ib info.
    public void appendRegionInfo() {
        builder.append(ChatColor.GRAY);
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        builder.append(" Region Info ");
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");

        newLine();

        appendBasics();
        appendParents();
        appendOwner();
        appendDelay();
        appendPriority();
        appendBounds();
    }

    public void send(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public StringBuilder append(boolean b) {
        return builder.append(b);
    }

    public StringBuilder append(char c) {
        return builder.append(c);
    }

    public StringBuilder append(char[] str, int offset, int len) {
        return builder.append(str, offset, len);
    }

    public StringBuilder append(char[] str) {
        return builder.append(str);
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        return builder.append(s, start, end);
    }

    public StringBuilder append(CharSequence s) {
        return builder.append(s);
    }

    public StringBuilder append(double d) {
        return builder.append(d);
    }

    public StringBuilder append(float f) {
        return builder.append(f);
    }

    public StringBuilder append(int i) {
        return builder.append(i);
    }

    public StringBuilder append(long lng) {
        return builder.append(lng);
    }

    public StringBuilder append(Object obj) {
        return builder.append(obj);
    }

    public StringBuilder append(String str) {
        return builder.append(str);
    }

    public StringBuilder append(StringBuffer sb) {
        return builder.append(sb);
    }

    public StringBuilder appendCodePoint(int codePoint) {
        return builder.appendCodePoint(codePoint);
    }

    @Override
    public String toString() {
        return builder.toString().trim();
    }
}
