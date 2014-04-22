package me.gnat008.infiniteblocks.regions;

import org.khelekore.prtree.MBRConverter;

public class BlocksRegionMBRConverter implements MBRConverter<BlockRegion> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int dimension, BlockRegion region) {
        switch (dimension) {
            case 0:
                return region.getMaximumPoint().getBlockX();
            case 1:
                return region.getMaximumPoint().getBlockY();
            case 2:
                return region.getMaximumPoint().getBlockZ();
        }

        return 0;
    }

    @Override
    public double getMin(int dimension, BlockRegion region) {
        switch (dimension) {
            case 0:
                return region.getMinimumPoint().getBlockX();
            case 1:
                return region.getMinimumPoint().getBlockY();
            case 2:
                return region.getMinimumPoint().getBlockZ();
        }

        return 0;
    }
}
