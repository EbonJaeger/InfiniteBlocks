package me.gnat008.infiniteblocks.regions;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ApplicableRegionSet implements Iterable<BlockRegion> {

    private Collection<BlockRegion> applicable;
    private BlockRegion globalRegion;

    public ApplicableRegionSet(Collection<BlockRegion> applicable, BlockRegion globalRegion) {
        this.applicable = applicable;
        this.globalRegion = globalRegion;
    }

    public boolean isOwnerOfAll(Player player) {
        for (BlockRegion region : applicable) {
            if (!region.isOwner(player)) {
                return false;
            }
        }

        return true;
    }

    private void clearParents(Set<BlockRegion> needsClear, Set<BlockRegion> hasCleared, BlockRegion region) {
        BlockRegion parent = region.getParent();

        while (parent != null) {
            if (!needsClear.remove(parent)) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }

    public int size() {
        return applicable.size();
    }

    public Iterator<BlockRegion> iterator() {
        return applicable.iterator();
    }
}
