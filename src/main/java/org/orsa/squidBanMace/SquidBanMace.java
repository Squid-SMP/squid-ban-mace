package org.orsa.squidBanMace;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.orsa.squidBanMace.command.BanMaceCommand;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.orsa.squidBanMace.item.BanMace;

public class SquidBanMace implements ModInitializer {

    public static MinecraftServer SERVER;

    public static final String MOD_ID = "squid_ban_mace";
    public static BanMace BAN_MACE;

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        AttackEntityCallback.EVENT.register(BanMace::onAttackEntity);
        CommandRegistrationCallback.EVENT.register(SquidBanMace::registerCommands);

        BAN_MACE = BanMace.register();

        registerItemGroup();
    }

    private static void registerItemGroup() {
        var builder = PolymerCreativeModeTabUtils.builder();
        builder.title(Component.translatable("itemgroup.squid_ban_mace"));
        builder.icon(() -> new ItemStack(BAN_MACE));
        builder.displayItems(SquidBanMace::populateGroup);
        var itemGroup = builder.build();
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(id("main"), itemGroup);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        BanMaceCommand.register(dispatcher);
    }

    private void onServerStarted(MinecraftServer server) {
        SERVER = server;
    }

    private static void populateGroup(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(BAN_MACE);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void playSoundFor(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        var packet = new ClientboundSoundEntityPacket(
                Holder.direct(sound),
                SoundSource.MASTER,
                player,
                volume,
                pitch,
                player.level().getRandom().nextLong()
        );
        player.connection.send(packet);
    }
}
