package me.gnat008.infiniteblocks.regions;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.exceptions.UnsupportedIntersectionException;

import java.util.ArrayList;
import java.util.List;

public class BlockPolygonalRegion extends BlockRegion {

    protected List<BlockVector2D> points;
    protected int minY;
    protected int maxY;

    public BlockPolygonalRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id);
        this.points = points;

        setMinMaxPoints(points, minY, maxY)
        ;
        this.minY = min.getBlockY();
        this.maxY = max.getBlockY();
    }

    private void setMinMaxPoints(List<BlockVector2D> points2D, int minY, int maxY) {
        List<Vector> points = new ArrayList<Vector>();

        int y = minY;
        for (BlockVector2D point2D : points2D) {
            points.add(new Vector(point2D.getBlockX(), y, point2D.getBlockZ()));
            y = maxY;
        }

        setMinMaxPoints(points);
    }

    public List<BlockVector2D> getPoints() {
        return points;
    }

    @Override
    public boolean contains(Vector pt) {
        int targetX = pt.getBlockX(); //wide
        int targetY = pt.getBlockY(); //height
        int targetZ = pt.getBlockZ(); //depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }
        //Quick and dirty check.
        if (targetX < min.getBlockX() || targetX > max.getBlockX() || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
            return false;
        }
        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = points.get(npoints - 1).getBlockX();
        zOld = points.get(npoints - 1).getBlockZ();

        for (i = 0; i < npoints; i++) {
            xNew = points.get(i).getBlockX();
            zNew = points.get(i).getBlockZ();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= targetX && targetX <= x2) {
                crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1)
                        - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossproduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) return true; // on edge
                } else if (crossproduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    @Override
    public List<BlockRegion> getIntersectingRegions(List<BlockRegion> regions) throws UnsupportedIntersectionException {
        List<BlockRegion> intersectingRegions = new ArrayList<BlockRegion>();

        for (BlockRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            if (region instanceof BlockPolygonalRegion || region instanceof BlockCuboidRegion) {
                // If either region contains the points of the other,
                // or if any edges intersect, the regions intersect
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
        return "polygon";
    }

    @Override
    public int volume() {
        int volume = 0;
        return volume;
    }
}
