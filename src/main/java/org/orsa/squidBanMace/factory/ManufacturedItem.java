package org.orsa.squidBanMace.factory;

import net.minecraft.world.item.Item;

public interface ManufacturedItem<T extends Item> {
    void init(ItemFactory<T> factory);
}
