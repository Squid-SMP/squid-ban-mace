package org.orsa.squidBanMace.factory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

import static org.orsa.squidBanMace.SquidBanMace.id;

public class ItemFactory<T extends Item> {

    private boolean hasRegistered = false;

    public String name;
    public T item;
    public Identifier id;
    public ResourceKey<Item> resourceKey;

    public ItemFactory(Function<Item.Properties, T> constructor, String name, Item.Properties properties) {
        this.name = name;

        id = id(name);
        resourceKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);

        properties.setId(resourceKey);

        item = constructor.apply(properties);
    }

    public void register() {
        if (hasRegistered) {
            return;
        }

        Registry.register(BuiltInRegistries.ITEM, resourceKey, item);

        hasRegistered = true;
    }
}
