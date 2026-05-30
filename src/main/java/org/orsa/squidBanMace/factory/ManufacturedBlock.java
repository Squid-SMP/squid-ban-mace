package org.orsa.squidBanMace.factory;

import net.minecraft.world.level.block.Block;

public interface ManufacturedBlock<T extends Block> {
    void init(BlockFactory<T> factory);
}
