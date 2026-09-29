package com.betterspellcasting.mixin;

import com.betterspellcasting.BowInputController;
import com.betterspellcasting.SpellcastingController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.spell_engine.client.input.SpellHotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Routes each Spell Engine input pass through the shared selection state. */
@Mixin(SpellHotbar.class)
public abstract class SpellHotbarMixin {
    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$route(LocalPlayer player, Options options,
                                          CallbackInfoReturnable<Boolean> cir) {
        SpellcastingController.routeHotbar(net.minecraft.client.Minecraft.getInstance());
    }

    @Inject(method = "handleAll", at = @At("HEAD"), remap = false, cancellable = true)
    private void betterSpellcasting$beginAll(LocalPlayer player, Options options,
                                              List<KeyMapping> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.beginHandle(SpellHotbar.INSTANCE.slots);
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleAll", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishAll(LocalPlayer player, Options options,
                                               List<KeyMapping> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.finishHandle(cir.getReturnValue());
        SpellcastingController.endHandle();
    }

    @Inject(method = "handleUseKey", at = @At("HEAD"), remap = false, cancellable = true)
    private void betterSpellcasting$beginUseKey(LocalPlayer player, Options options,
                                                 CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellHotbar.Slot selected = SpellHotbar.INSTANCE.structuredSlots.onUseKey();
        SpellcastingController.beginHandle(selected == null ? List.of() : List.of(selected));
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleUseKey", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishUseKey(LocalPlayer player, Options options,
                                                  CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.finishHandle(cir.getReturnValue());
        SpellcastingController.endHandle();
    }

    @Inject(method = "handleOther", at = @At("HEAD"), remap = false, cancellable = true)
    private void betterSpellcasting$beginOther(LocalPlayer player, Options options,
                                                List<KeyMapping> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.beginHandle(SpellHotbar.INSTANCE.structuredSlots.other());
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleOther", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishOther(LocalPlayer player, Options options,
                                                 List<KeyMapping> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.finishHandle(cir.getReturnValue());
        SpellcastingController.endHandle();
    }

    @Inject(method = "handleSome", at = @At("HEAD"), remap = false, cancellable = true)
    private void betterSpellcasting$beginSome(LocalPlayer player, SpellHotbar.Slot slot,
                                               Options options, List<KeyMapping> pressed,
                                               CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.beginHandle(slot == null ? List.of() : List.of(slot));
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleSome", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishSome(LocalPlayer player, SpellHotbar.Slot slot,
                                                Options options, List<KeyMapping> pressed,
                                                CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellcastingController.finishHandle(cir.getReturnValue());
        SpellcastingController.endHandle();
    }
}

