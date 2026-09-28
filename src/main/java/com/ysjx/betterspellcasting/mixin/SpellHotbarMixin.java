package com.ysjx.betterspellcasting.mixin;

import com.ysjx.betterspellcasting.BowInputController;
import com.ysjx.betterspellcasting.WheelController;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
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
    private void betterSpellcasting$route(ClientPlayerEntity player, GameOptions options,
                                          CallbackInfoReturnable<Boolean> cir) {
        WheelController.routeHotbar(net.minecraft.client.MinecraftClient.getInstance());
    }

    @Inject(method = "handleAll", at = @At("HEAD"), remap = false)
    private void betterSpellcasting$beginAll(ClientPlayerEntity player, GameOptions options,
                                              List<KeyBinding> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.beginHandle(SpellHotbar.INSTANCE.slots);
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleAll", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishAll(ClientPlayerEntity player, GameOptions options,
                                               List<KeyBinding> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.finishHandle(cir.getReturnValue());
        WheelController.endHandle();
    }

    @Inject(method = "handleUseKey", at = @At("HEAD"), remap = false)
    private void betterSpellcasting$beginUseKey(ClientPlayerEntity player, GameOptions options,
                                                 CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        SpellHotbar.Slot selected = SpellHotbar.INSTANCE.structuredSlots.onUseKey();
        WheelController.beginHandle(selected == null ? List.of() : List.of(selected));
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleUseKey", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishUseKey(ClientPlayerEntity player, GameOptions options,
                                                  CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.finishHandle(cir.getReturnValue());
        WheelController.endHandle();
    }

    @Inject(method = "handleOther", at = @At("HEAD"), remap = false)
    private void betterSpellcasting$beginOther(ClientPlayerEntity player, GameOptions options,
                                                List<KeyBinding> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.beginHandle(SpellHotbar.INSTANCE.structuredSlots.other());
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleOther", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishOther(ClientPlayerEntity player, GameOptions options,
                                                 List<KeyBinding> pressed, CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.finishHandle(cir.getReturnValue());
        WheelController.endHandle();
    }

    @Inject(method = "handleSome", at = @At("HEAD"), remap = false)
    private void betterSpellcasting$beginSome(ClientPlayerEntity player, SpellHotbar.Slot slot,
                                               GameOptions options, List<KeyBinding> pressed,
                                               CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.beginHandle(slot == null ? List.of() : List.of(slot));
        if (BowInputController.bypassSpellInput()) cir.setReturnValue(null);
    }

    @Inject(method = "handleSome", at = @At("RETURN"), remap = false)
    private void betterSpellcasting$finishSome(ClientPlayerEntity player, SpellHotbar.Slot slot,
                                                GameOptions options, List<KeyBinding> pressed,
                                                CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.finishHandle(cir.getReturnValue());
        WheelController.endHandle();
    }
}
