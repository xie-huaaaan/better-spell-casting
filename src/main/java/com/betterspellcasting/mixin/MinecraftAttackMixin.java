package com.betterspellcasting.mixin;

import com.betterspellcasting.BowInputController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stops vanilla attack and mining paths while the left bow control owns the attack key. */
@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void betterSpellcasting$blockStartAttack(CallbackInfoReturnable<Boolean> callback) {
        if (BowInputController.blocksAttack((Minecraft) (Object) this)) callback.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void betterSpellcasting$blockContinueAttack(boolean leftClick, CallbackInfo callback) {
        if (leftClick && BowInputController.blocksAttack((Minecraft) (Object) this)) callback.cancel();
    }
}
