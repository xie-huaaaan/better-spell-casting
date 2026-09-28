package com.ysjx.betterspellcasting.mixin;

import com.ysjx.betterspellcasting.BetterSpellcastingClient;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("RETURN"))
    private void spellCycleWheel$onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        BetterSpellcastingClient.onKey(window, key, scanCode, action);
    }
}
