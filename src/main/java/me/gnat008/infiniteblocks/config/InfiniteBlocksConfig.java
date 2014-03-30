package me.gnat008.infiniteblocks.config;

import me.gnat008.infiniteblocks.util.old.Options;
import me.gnat008.infiniteblocks.util.old.YAMLConfig;

public class InfiniteBlocksConfig extends Options {

    public InfiniteBlocksConfig instance;

    public InfiniteBlocksConfig(YAMLConfig config) {
        super(config);
        this.instance = this;
    }

    @Override
    public void setDefaults() {

    }
}
