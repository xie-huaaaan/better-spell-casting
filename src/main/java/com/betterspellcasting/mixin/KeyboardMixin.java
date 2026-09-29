package com.betterspellcasting.mixin;

import com.betterspellcasting.BetterSpellcastingClient;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("RETURN"))
    private void betterSpellcasting$onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        BetterSpellcastingClient.onKey(window, key, scanCode, action);
    }
}

