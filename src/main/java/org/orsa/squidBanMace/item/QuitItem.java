package org.orsa.squidBanMace.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.orsa.squidBanMace.factory.ItemFactory;
import org.orsa.squidBanMace.factory.ManufacturedItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class QuitItem extends Item implements PolymerItem, ManufacturedItem<QuitItem> {
    Item polymerItem;

    public QuitItem(Properties properties) {
        super(properties);
    }

    public static QuitItem register() {
        var properties = new Properties();

        var factory = new ItemFactory<>(QuitItem::new, "quit", properties);
        var item = factory.item;

        item.init(factory);
        factory.register();

        return item;
    }

    public void init(ItemFactory<QuitItem> factory) {
        polymerItem = Items.CLAY_BALL;
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return polymerItem;
    }
}
