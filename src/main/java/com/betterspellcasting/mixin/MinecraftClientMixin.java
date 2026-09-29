package com.betterspellcasting.mixin;

import com.betterspellcasting.BetterSpellcastingClient;
import com.betterspellcasting.BowInputController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the client-only input takeover at the two vanilla action entry points. */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockBowAttack(CallbackInfoReturnable<Boolean> cir) {
        if (BowInputController.blocksAttack((Minecraft) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockUseWhileOpen(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (BetterSpellcastingClient.shouldBlockUse(client)) {
            ci.cancel();
        }
    }
}
