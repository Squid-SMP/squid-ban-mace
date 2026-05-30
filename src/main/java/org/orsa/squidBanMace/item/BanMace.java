package org.orsa.squidBanMace.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.orsa.squidBanMace.SquidBanMace;
import org.orsa.squidStaffCommands.TeleportUtils;
import static org.orsa.squidBanMace.SquidBanMace.SERVER;
import static org.orsa.squidStaffCommands.SquidStaffCommands.MOD_PERM;
import org.orsa.squidBanMace.factory.ItemFactory;
import org.orsa.squidBanMace.factory.ManufacturedItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;

import java.util.List;

public class BanMace extends MaceItem implements PolymerItem, ManufacturedItem<BanMace> {

    public enum BanMaceMode {
        Ban,
        Kick,
        Jail,
        TpToBed,
        TpToSpawn;

        public String displayName() {
            return switch (this) {
                case Ban -> "Ban";
                case Kick -> "Kick";
                case Jail -> "Jail";
                case TpToBed -> "Teleport to Bed";
                case TpToSpawn -> "Teleport to Spawn";
            };
        }
    }

    private static Holder<Enchantment> windBurstHolder;

    public BanMace(Properties properties) {
        super(properties);
    }

    public static BanMace register() {
        var properties = new Properties();
        properties.component(DataComponents.TOOL, MaceItem.createToolProperties());
        properties.attributes(MaceItem.createAttributes());
        properties.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        properties.stacksTo(1);

        var factory = new ItemFactory<>(BanMace::new, "ban_mace", properties);
        var item = factory.item;

        item.init(factory);
        factory.register();

        return item;
    }

    @Override
    public void init(ItemFactory<BanMace> factory) {
    }

    private static BanMaceMode getMode(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        var ordinal = tag.getIntOr("mode", 0);
        return BanMaceMode.values()[ordinal];
    }

    private static void setMode(ItemStack stack, BanMaceMode mode) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("mode", mode.ordinal()));
        var loreLine = Component.literal(mode.displayName());
        stack.set(DataComponents.LORE, new ItemLore(List.of(loreLine)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        var serverPlayer = (ServerPlayer) player;

        if (!Permissions.check(serverPlayer, MOD_PERM)) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS_SERVER;
        }

        var stack = player.getItemInHand(hand);
        var currentMode = getMode(stack);
        var modes = BanMaceMode.values();

        var nextMode = modes[(currentMode.ordinal() + 1) % modes.length];
        setMode(stack, nextMode);
        serverPlayer.inventoryMenu.broadcastChanges();

        player.sendOverlayMessage(Component.literal("[" + nextMode.displayName() + "]").withStyle(ChatFormatting.GOLD));
        SquidBanMace.playSoundFor(serverPlayer, SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 2.0f);

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (windBurstHolder == null) {
            var enchantmentRegistry = SERVER.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            windBurstHolder = enchantmentRegistry.getOrThrow(Enchantments.WIND_BURST);
        }

        if (!stack.has(DataComponents.LORE)) {
            setMode(stack, getMode(stack));
        }

        var enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.getLevel(windBurstHolder) != 3) {
            var mutable = new ItemEnchantments.Mutable(enchantments);
            mutable.set(windBurstHolder, 3);
            var updatedEnchantments = mutable.toImmutable();
            stack.set(DataComponents.ENCHANTMENTS, updatedEnchantments);
        }
    }

    public static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.PASS;
        }

        if (!(player.getMainHandItem().getItem() instanceof BanMace)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(entity instanceof ServerPlayer targetPlayer)) {
            return InteractionResult.PASS;
        }

        if (!Permissions.check(serverPlayer, MOD_PERM)) {
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }

        var stack = serverPlayer.getMainHandItem();
        var mode = getMode(stack);
        var targetName = targetPlayer.getName().getString();

        switch (mode) {
            case Ban -> runCommand(serverPlayer, "ban " + targetName + " The ban mace has spoken!");
            case Kick -> runCommand(serverPlayer, "kick " + targetName + " The ban mace has spoken!");
            case Jail -> runCommand(serverPlayer, "jail " + targetName);
            case TpToBed -> TeleportUtils.tpToBed(targetPlayer);
            case TpToSpawn -> TeleportUtils.tpToSpawn(targetPlayer);
        }

        return InteractionResult.PASS;
    }

    private static void runCommand(ServerPlayer player, String command) {
        SERVER.getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.MACE;
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack serverStack, PacketContext context) {
        tooltip.clear();
        var mode = getMode(serverStack);
        tooltip.add(Component.literal("[" + mode.displayName() + "]").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack serverStack, ItemStack clientStack, PacketContext context, HolderLookup.Provider lookup) {
        var enchantmentRegistry = SERVER.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var windBurst = enchantmentRegistry.getOrThrow(Enchantments.WIND_BURST);
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(windBurst, 3);
        var enchantments = mutable.toImmutable();
        clientStack.set(DataComponents.ENCHANTMENTS, enchantments);

        var tooltipDisplay = TooltipDisplay.DEFAULT;
        tooltipDisplay = tooltipDisplay.withHidden(DataComponents.ENCHANTMENTS, true);
        tooltipDisplay = tooltipDisplay.withHidden(DataComponents.UNBREAKABLE, true);
        clientStack.set(DataComponents.TOOLTIP_DISPLAY, tooltipDisplay);
    }
}
