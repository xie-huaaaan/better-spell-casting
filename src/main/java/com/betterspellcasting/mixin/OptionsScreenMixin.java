package com.betterspellcasting.mixin;

import com.betterspellcasting.SpellcastingSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Inserts the DLC settings into the vanilla options grid before its Done button. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private LayoutElement betterSpellcasting$addSettingsEntry(GridLayout.RowHelper row, LayoutElement done,
                                                               int span, LayoutSettings settings) {
        Screen screen = (Screen) (Object) this;
        row.addChild(Button.builder(Component.translatable("screen.better-spell-casting.entry"),
                        ignored -> Minecraft.getInstance().setScreen(new SpellcastingSettingsScreen(screen)))
                .width(200).build());
        return row.addChild(done, span, settings);
    }
}
