package org.orsa.squidBanMace.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.orsa.squidBanMace.SquidBanMace;

import static net.minecraft.commands.Commands.literal;
import static org.orsa.squidStaffCommands.SquidStaffCommands.MOD_PERM;

public class BanMaceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("banmace")
                .requires(source -> Permissions.check(source, MOD_PERM))
                .executes(BanMaceCommand::giveBanMace));
    }

    private static int giveBanMace(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        var player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        var stack = new ItemStack(SquidBanMace.BAN_MACE);

        if (!player.getInventory().add(stack)) {
            source.sendFailure(Component.literal("Not enough room in inventory."));
            return 0;
        }

        return 1;
    }
}
