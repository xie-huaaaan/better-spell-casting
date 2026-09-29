package com.betterspellcasting.mixin;

import com.betterspellcasting.BetterSpellcastingClient;
import net.minecraft.client.gui.GuiGraphics;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudRenderHelper.SpellHotBarWidget.class)
public abstract class SpellHotbarWidgetMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private static void betterSpellcasting$hideOriginal(GuiGraphics context, int screenWidth, int screenHeight,
                                                       HudRenderHelper.SpellHotBarWidget.ViewModel viewModel,
                                                       CallbackInfo ci) {
        if (!com.betterspellcasting.SpellcastingController.castSlots().isEmpty()) {
            ci.cancel();
        }
    }
}

