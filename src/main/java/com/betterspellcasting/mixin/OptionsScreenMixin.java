package com.betterspellcasting.mixin;

import com.betterspellcasting.SpellcastingSettingsScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the client-only spellcasting settings entry to the 26.1 options screen. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) { super(title); }

    @Shadow
    protected abstract <T extends AbstractWidget> T addRenderableWidget(T widget);

    @Inject(method = "init", at = @At("TAIL"))
    private void betterSpellcasting$addEntry(CallbackInfo ci) {
        OptionsScreen screen = (OptionsScreen) (Object) this;
        int width = Math.min(310, this.width - 24);
        int x = (this.width - width) / 2;
        int y = this.height - 52;
        addRenderableWidget(Button.builder(Component.translatable("screen.better-spell-casting.entry"),
                ignored -> this.minecraft.setScreen(new SpellcastingSettingsScreen(screen)))
                .bounds(x, y, width, 20).build());
    }
}
