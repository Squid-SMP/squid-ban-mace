package org.orsa.squidBanMace.factory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

import static org.orsa.squidBanMace.SquidBanMace.id;

public class BlockFactory<T extends Block> {

    private Boolean hasRegistered = false;

    public String name;
    public T block;
    public Identifier id;
    public ResourceKey<Block> resourceKey;

    public BlockFactory(Function<BlockBehaviour.Properties, T> constructor, String name, BlockBehaviour.Properties properties) {
        this.name = name;

        id = id(name);
        resourceKey = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);

        properties.setId(resourceKey);

        block = constructor.apply(properties);
    }

    public void register() {
        if (hasRegistered) {
            return;
        }

        Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);

        hasRegistered = true;
    }
}