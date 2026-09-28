package com.ysjx.spellcyclewheel.mixin;

import com.ysjx.spellcyclewheel.SpellCycleWheelClient;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The original cycle mod's prompt is redundant while the radial selector is active. */
@Mixin(com.yuansujuexing.spellcycle.SpellCycleClient.class)
public abstract class SpellCycleHudMixin {
    @Inject(method = "renderHud", at = @At("HEAD"), cancellable = true)
    private static void spellCycleWheel$hideRadialModeHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (SpellCycleWheelClient.isWheelMode()) {
            ci.cancel();
        }
    }
}
