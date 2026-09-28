package com.betterspellcasting.mixin;

import com.betterspellcasting.BetterSpellcastingClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (BetterSpellcastingClient.onScroll(horizontal, vertical)) {
            ci.cancel();
        }
    }
}
