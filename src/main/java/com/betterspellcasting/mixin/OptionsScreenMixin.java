package com.betterspellcasting.mixin;

import com.betterspellcasting.SpellcastingSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Adds spellcasting settings to the 1.20.1 vanilla options grid before its Done button. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;ILnet/minecraft/client/gui/widget/Positioner;)Lnet/minecraft/client/gui/widget/Widget;"))
    private Widget betterSpellcasting$addSettingsEntry(GridWidget.Adder adder, Widget done, int span, Positioner positioner) {
        Screen screen = (Screen) (Object) this;
        ButtonWidget entry = ButtonWidget.builder(Text.translatable("screen.better-spell-casting.entry"),
                        ignored -> MinecraftClient.getInstance().setScreen(new SpellcastingSettingsScreen(screen)))
                .width(150)
                .tooltip(Tooltip.of(Text.translatable("screen.better-spell-casting.entry.tooltip")))
                .build();
        adder.add(entry);
        return adder.add(done, span, positioner);
    }
}
