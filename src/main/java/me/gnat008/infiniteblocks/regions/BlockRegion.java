package me.gnat008.infiniteblocks.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.exceptions.UnsupportedIntersectionException;
import org.bukkit.entity.Player;

import java.awt.geom.Line2D;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BlockRegion implements Comparable<BlockRegion> {

    protected BlockVector min;
    protected BlockVector max;

    private static final Pattern idPattern = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");

    private String id;
    private String owner;
    private BlockRegion parent;
    private int priority;

    public BlockRegion(String id) {
        this.id = id;
    }

    protected void setMinMaxPoints(List<Vector> points) {
        int minX = points.get(0).getBlockX();
        int minY = points.get(0).getBlockY();
        int minZ = points.get(0).getBlockZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;

        for (Vector v : points) {
            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    public String getId() {
        return id;
    }

    public BlockVector getMaximumPoint() {
        return max;
    }

    public BlockVector getMinimumPoint() {
        return min;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BlockRegion getParent() {
        return parent;
    }

    public void setParent(BlockRegion parent) throws CircularInheritanceException {
        if (parent == null) {
            this.parent = null;
            return;
        }

        if (parent == this) {
            throw new CircularInheritanceException();
        }

        BlockRegion p = parent.getParent();
        while (p != null) {
            if (p == this) {
                throw new CircularInheritanceException();
            }

            p = p.getParent();
        }

        this.parent = parent;
    }

    public String getOwner() {
        return owner;
    }

    public BlockVector getMin() {
        return min;
    }

    public BlockVector getMax() {
        return max;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOwner(Player player) {
        if (owner.equalsIgnoreCase(player.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public abstract List<BlockVector2D> getPoints();

    public abstract int volume();

    public abstract boolean contains(Vector pt);

    public boolean contains(BlockVector2D pt) {
        return contains(new Vector(pt.getBlockX(), min.getBlockY(), pt.getBlockZ()));
    }

    public boolean contains(int x, int y, int z) {
        return contains(new Vector(x, y, z));
    }

    public boolean containsAny(List<BlockVector2D> pts) {
        for (BlockVector2D pt : pts) {
            if (contains(pt)) {
                return true;
            }
        }

        return false;
    }

    public int compareTo(BlockRegion other) {
        if (priority > other.priority) {
            return -1;
        } else if (priority < other.priority) {
            return 1;
        }

        return id.compareTo(other.id);
    }

    public abstract String getTypeName();

    public abstract List<BlockRegion> getIntersectingRegions(List<BlockRegion> regions) throws UnsupportedIntersectionException;

    protected boolean intersectsBoundingBox(BlockRegion region) {
        BlockVector rMaxPoint = region.getMaximumPoint();
        BlockVector min = getMinimumPoint();

        if (rMaxPoint.getBlockX() < min.getBlockX()) return false;
        if (rMaxPoint.getBlockY() < min.getBlockY()) return false;
        if (rMaxPoint.getBlockZ() < min.getBlockZ()) return false;

        BlockVector rMinPoint = region.getMinimumPoint();
        BlockVector max = getMaximumPoint();

        if (rMinPoint.getBlockX() > max.getBlockX()) return false;
        if (rMinPoint.getBlockY() > max.getBlockY()) return false;
        if (rMinPoint.getBlockZ() > max.getBlockZ()) return false;

        return true;
    }

    protected boolean intersectsEdges(BlockRegion region) {
        List<BlockVector2D> pts1 = getPoints();
        List<BlockVector2D> pts2 = region.getPoints();

        BlockVector2D lastPt1 = pts1.get(pts1.size() - 1);
        BlockVector2D lastPt2 = pts2.get(pts2.size() - 1);

        for (BlockVector2D aPts1 : pts1) {
            for (BlockVector2D aPts2 : pts2) {
                Line2D line1 = new Line2D.Double(
                        lastPt1.getBlockX(),
                        lastPt1.getBlockZ(),
                        aPts1.getBlockX(),
                        aPts1.getBlockZ());

                if (line1.intersectsLine(
                        lastPt2.getBlockX(),
                        lastPt2.getBlockZ(),
                        aPts2.getBlockX(),
                        aPts2.getBlockZ())) {
                    return true;
                }

                lastPt2 = aPts2;
            }

            lastPt1 = aPts1;
        }

        return false;
    }

    public static boolean isValidId(String id) {
        return idPattern.matcher(id).matches();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BlockRegion)) {
            return false;
        }

        BlockRegion other = (BlockRegion) obj;
        return other.getId().equals(getId());
    }

    public static class CircularInheritanceException extends Exception {
        private static final long serialVersionUID = 7479673488496776022L;
    }
}
