package org.orsa.squidBanMace.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.orsa.squidBanMace.item.BanMace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class BanMaceMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void onHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof BanMace) {
            cir.setReturnValue(true);
        }
    }
}
