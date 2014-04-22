package me.gnat008.infiniteblocks.command;

/**
 * Created by Gnat008 on 4/22/2014.
 */
class RegionListEntry implements Comparable<RegionListEntry> {

    private final String id;
    private final int index;
    boolean isOwner;

    public RegionListEntry(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public int compareTo(RegionListEntry o) {
        if (isOwner != o.isOwner) {
            return isOwner ? 1 : -1;
        }

        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        if (isOwner) {
            return (index + 1) + ". +" + id;
        } else {
            return (index + 1) + ". " + id;
        }
    }
}
