package com.betterspellcasting.mixin;

import com.betterspellcasting.SpellcastingSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Adds spellcasting settings as the next cell of the vanilla options grid. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private LayoutElement betterSpellcasting$addSettingsEntry(HeaderAndFooterLayout layout, LayoutElement body) {
        if (body instanceof GridLayout grid) {
            int[] count = {0};
            grid.visitChildren(element -> count[0]++);
            Screen screen = (Screen) (Object) this;
            Button entry = Button.builder(Component.translatable("screen.better-spell-casting.entry"),
                            ignored -> Minecraft.getInstance().setScreen(new SpellcastingSettingsScreen(screen)))
                    .width(150)
                    .tooltip(Tooltip.create(Component.translatable("screen.better-spell-casting.entry.tooltip")))
                    .build();
            grid.addChild(entry, count[0] / 2, count[0] % 2);
        }
        return layout.addToContents(body);
    }
}
