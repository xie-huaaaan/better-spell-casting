package com.ysjx.spellcyclewheel.mixin;

import com.ysjx.spellcyclewheel.SpellCycleWheelClient;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudRenderHelper.SpellHotBarWidget.class)
public abstract class SpellHotbarWidgetMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void spellCycleWheel$hideOriginal(DrawContext context, int screenWidth, int screenHeight,
                                                       HudRenderHelper.SpellHotBarWidget.ViewModel viewModel,
                                                       CallbackInfo ci) {
        if (!com.ysjx.spellcyclewheel.WheelController.castSlots().isEmpty()) {
            ci.cancel();
        }
    }
}
