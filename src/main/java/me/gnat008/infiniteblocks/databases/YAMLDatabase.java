package me.gnat008.infiniteblocks.databases;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import me.gnat008.infiniteblocks.exceptions.RegionDatabaseException;
import me.gnat008.infiniteblocks.regions.BlockCuboidRegion;
import me.gnat008.infiniteblocks.regions.BlockPolygonalRegion;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import me.gnat008.infiniteblocks.util.yaml.YAMLFormat;
import me.gnat008.infiniteblocks.util.yaml.YAMLNode;
import me.gnat008.infiniteblocks.util.yaml.YAMLProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class YAMLDatabase extends AbstractRegionDatabase {

    private YAMLProcessor config;
    private Map<String, BlockRegion> regions;
    private final Logger logger;

    public YAMLDatabase(File file, Logger logger) throws RegionDatabaseException, FileNotFoundException {
        this.logger = logger;

        // Shouldn't be necessary, but check anyways.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }

        config = new YAMLProcessor(file, false, YAMLFormat.COMPACT);
    }

    public void load() throws RegionDatabaseException {
        try {
            config.load();
        } catch (IOException e) {
            throw new RegionDatabaseException(e);
        }

        Map<String, YAMLNode> regionData = config.getNodes("regions");

        // No regions configured:
        if (regionData == null) {
            this.regions = new HashMap<String, BlockRegion>();
            return;
        }

        Map<String, BlockRegion> regions = new HashMap<String, BlockRegion>();
        Map<String, BlockRegion> parentSets = new HashMap<String, BlockRegion>();

        for (Map.Entry<String, YAMLNode> entry : regionData.entrySet()) {
            String id = entry.getKey().toLowerCase().replace(".", "");
            YAMLNode node = entry.getValue();

            String type = node.getString("type");
            BlockRegion region;

            try {
                if (type == null) {
                    logger.warning("Unidentified region type for id '" + id + "'.");
                    continue;
                } else if (type.equals("cuboid")) {
                    Vector pt1 = checkNonNull(node.getVector("min"));
                    Vector pt2 = checkNonNull(node.getVector("max"));

                    BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                    BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();

                    region = new BlockCuboidRegion(id, min, max);
                } else if (type.equals("poly2d")) {
                    Integer minY = checkNonNull(node.getInt("min-y"));
                    Integer maxY = checkNonNull(node.getInt("max-y"));

                    List<BlockVector2D> points = node.getBlockVector2DList("points", null);

                    region = new BlockPolygonalRegion(id, points, minY, maxY);
                } else {
                    logger.warning("Unknown region type for id '" + id + "'.");
                    continue;
                }

                //Integer priority = checkNonNull(node.getInt("priority"));
                //region.setPriority(priority);
                //setFlags(region, node.getNode("flags"));

                region.setOwner(node.getString("owner_uuid"));
                region.setDelay(node.getInt("delay"));

                //region.setMembers(parseDomain(node.getNode("members")));
                regions.put(id, region);

                String parentID = node.getString("parent");
                if (parentID != null) {
                    parentSets.put(parentID, region);
                }
            } catch (NullPointerException e) {
                logger.warning("Missing data for region id '" + id + "'!");
            }
        }

        // Relink parents.
        for (Map.Entry<String, BlockRegion> entry : parentSets.entrySet()) {
            BlockRegion parent = regions.get(entry.getKey());
            if (parent != null) {
                try {
                    entry.getValue().setParent(parent);
                } catch (BlockRegion.CircularInheritanceException e) {
                    logger.warning("Circular inheritance detected with '" + parent.getId() + "' detected as parent!");
                }
            } else {
                logger.warning("Unknown region parent: " + entry.getKey());
            }
        }

        this.regions = regions;
    }

    private <V> V checkNonNull(V val) throws NullPointerException {
        if (val == null) {
            throw new NullPointerException();
        }

        return val;
    }

    public void save() throws RegionDatabaseException {
        config.clear();

        for (Map.Entry<String, BlockRegion> entry : regions.entrySet()) {
            BlockRegion region = entry.getValue();
            YAMLNode node = config.addNode("regions." + entry.getKey());

            if (region instanceof BlockCuboidRegion) {
                BlockCuboidRegion cuboid = (BlockCuboidRegion) region;
                node.setProperty("type", "cuboid");
                node.setProperty("min", cuboid.getMinimumPoint());
                node.setProperty("max", cuboid.getMaximumPoint());
            } else if (region instanceof BlockPolygonalRegion) {
                BlockPolygonalRegion poly = (BlockPolygonalRegion) region;
                node.setProperty("type", "poly2d");
                node.setProperty("min-y", poly.getMinimumPoint().getBlockY());
                node.setProperty("max-y", poly.getMaximumPoint().getBlockY());

                List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
                for (BlockVector2D point : poly.getPoints()) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("x", point.getBlockX());
                    data.put("z", point.getBlockZ());
                    points.add(data);
                }

                node.setProperty("points", points);
            } else {
                node.setProperty("type", region.getClass().getCanonicalName());
            }

            node.setProperty("delay", region.getDelay());
            node.setProperty("owner_uuid", region.getOwnerUUID());

            BlockRegion parent = region.getParent();
            if (parent != null) {
                node.setProperty("parent", parent.getId());
            }
        }

        config.setHeader("#\r\n" +
                "# InfiniteBlocks Region File\r\n" +
                "#\r\n" +
                "# WARNING: THIS FILE IS AUTOMATICALLY GENERATED. If you modify this file by\r\n" +
                "# hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\r\n" +
                "# InfiniteBlocks is unable to parse the file, your regions will FAIL TO LOAD and\r\n" +
                "# the contents of this file will reset.");

        config.save();
    }

    public Map<String, BlockRegion> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, BlockRegion> regions) {
        this.regions = regions;
    }
}
