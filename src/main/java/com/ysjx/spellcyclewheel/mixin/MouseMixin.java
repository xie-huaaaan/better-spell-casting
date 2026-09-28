package com.ysjx.spellcyclewheel.mixin;

import com.ysjx.spellcyclewheel.SpellCycleWheelClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("RETURN"))
    private void spellCycleWheel$onButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        SpellCycleWheelClient.onMouseButton(window, button, action);
    }
}
