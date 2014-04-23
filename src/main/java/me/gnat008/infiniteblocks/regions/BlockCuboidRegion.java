package me.gnat008.infiniteblocks.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.exceptions.UnsupportedIntersectionException;

import java.util.ArrayList;
import java.util.List;

public class BlockCuboidRegion extends BlockRegion {

    public BlockCuboidRegion(String id, BlockVector pt1, BlockVector pt2) {
        super(id);
        setMinMaxPoints(pt1, pt2);
    }

    private void setMinMaxPoints(BlockVector pt1, BlockVector pt2) {
        List<Vector> points = new ArrayList<Vector>();
        points.add(pt1);
        points.add(pt2);
        setMinMaxPoints(points);
    }

    public void setMinimumPoint(BlockVector pt) {
        setMinMaxPoints(pt, max);
    }

    public void setMaximumPoint(BlockVector pt) {
        setMinMaxPoints(min, pt);
    }

    @Override
    public List<BlockVector2D> getPoints() {
        List<BlockVector2D> pts = new ArrayList<BlockVector2D>();
        int x1 = min.getBlockX();
        int x2 = max.getBlockX();
        int z1 = min.getBlockZ();
        int z2 = max.getBlockZ();

        pts.add(new BlockVector2D(x1, z1));
        pts.add(new BlockVector2D(x2, z1));
        pts.add(new BlockVector2D(x2, z2));
        pts.add(new BlockVector2D(x1, z2));

        return pts;
    }

    @Override
    public boolean contains(Vector pt) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();
        return x >= min.getBlockX() && x < max.getBlockX() + 1
                && y >= min.getBlockY() && y < max.getBlockY() + 1
                && z >= min.getBlockZ() && z < max.getBlockZ() + 1;
    }

    @Override
    public List<BlockRegion> getIntersectingRegions(List<BlockRegion> regions) throws UnsupportedIntersectionException {
        List<BlockRegion> intersectingRegions = new ArrayList<BlockRegion>();

        for (BlockRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            // If both regions are cuboids and their bounding boxes intersect, they intersect.
            if (region instanceof BlockCuboidRegion) {
                intersectingRegions.add(region);
                continue;
            } else if (region instanceof BlockPolygonalRegion) {
                // If either region contains the point of the other, or if any edges intersect, they intersect.
                if (containsAny(region.getPoints()) || region.containsAny(getPoints()) || intersectsEdges(region)) {
                    intersectingRegions.add(region);
                    continue;
                }
            } else {
                throw new UnsupportedIntersectionException();
            }
        }

        return intersectingRegions;
    }

    @Override
    public String getTypeName() {
        return "cuboid";
    }

    @Override
    public int volume() {
        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        return xLength * yLength * zLength;
    }
}
