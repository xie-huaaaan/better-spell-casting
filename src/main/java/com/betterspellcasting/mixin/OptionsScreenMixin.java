package com.betterspellcasting.mixin;

import com.betterspellcasting.SpellcastingSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Adds spellcasting settings as the next cell of the vanilla options grid. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addBody(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"))
    private Widget betterSpellcasting$addSettingsEntry(ThreePartsLayoutWidget layout, Widget body) {
        if (body instanceof GridWidget grid) {
            int[] count = {0};
            grid.forEachElement(widget -> count[0]++);
            Screen screen = (Screen) (Object) this;
            ButtonWidget entry = ButtonWidget.builder(Text.translatable("screen.better-spell-casting.entry"),
                            ignored -> MinecraftClient.getInstance().setScreen(new SpellcastingSettingsScreen(screen)))
                    .width(150)
                    .tooltip(Tooltip.of(Text.translatable("screen.better-spell-casting.entry.tooltip")))
                    .build();
            grid.add(entry, count[0] / 2, count[0] % 2);
        }
        return layout.addBody(body);
    }
}
