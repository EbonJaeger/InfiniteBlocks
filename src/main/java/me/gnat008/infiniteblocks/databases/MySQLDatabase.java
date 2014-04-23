package me.gnat008.infiniteblocks.databases;

import me.gnat008.infiniteblocks.config.ConfigurationManager;
import me.gnat008.infiniteblocks.exceptions.InvalidTableFormatException;
import me.gnat008.infiniteblocks.exceptions.RegionDatabaseException;
import me.gnat008.infiniteblocks.regions.BlockRegion;
import org.yaml.snakeyaml.Yaml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Gnat008 on 4/23/2014.
 */
public class MySQLDatabase extends AbstractRegionDatabase {

    private final Logger logger;

    private Yaml yaml;

    private Map<String, BlockRegion> regions;

    private Map<String, BlockRegion> cuboidRegions;
    private Map<String, BlockRegion> poly2dRegions;
    private Map<String, BlockRegion> globalRegions;
    private Map<BlockRegion, String> parentSets;

    private final ConfigurationManager config;

    private Connection conn;
    private int worldDbId = -1; // The database will never have an id of '-1'.

    public MySQLDatabase(ConfigurationManager config, String world, Logger logger) throws RegionDatabaseException {
        this.config = config;
        String world1 = world;
        this.logger = logger;

        try {
            connect();

            try {
                // Test if the database is up to date, if it isn't, throw an error.
                PreparedStatement verTest = this.conn.prepareStatement("SELECT `world_id` FROM `region_cuboid` LIMIT 0,1;");
                verTest.execute();
            } catch (SQLException ex) {
                throw new InvalidTableFormatException("region_storage_update_20110325.sql");
            }

            PreparedStatement
        }
    }
}
