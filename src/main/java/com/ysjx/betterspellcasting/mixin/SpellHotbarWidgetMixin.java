package com.ysjx.betterspellcasting.mixin;

import com.ysjx.betterspellcasting.BetterSpellcastingClient;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudRenderHelper.SpellHotBarWidget.class)
public abstract class SpellHotbarWidgetMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private static void betterSpellcasting$hideOriginal(DrawContext context, int screenWidth, int screenHeight,
                                                       HudRenderHelper.SpellHotBarWidget.ViewModel viewModel,
                                                       CallbackInfo ci) {
        if (!com.ysjx.betterspellcasting.WheelController.castSlots().isEmpty()) {
            ci.cancel();
        }
    }
}
