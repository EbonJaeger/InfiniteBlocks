package me.gnat008.infiniteblocks.config;

import me.gnat008.infiniteblocks.util.Options;
import me.gnat008.infiniteblocks.util.YAMLConfig;

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
