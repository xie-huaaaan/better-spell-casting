package com.ysjx.betterspellcasting.mixin;

import com.ysjx.betterspellcasting.WheelController;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.spell_engine.client.input.SpellHotbar;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellHotbar.class)
public abstract class SpellHotbarMixin {
    @Inject(method = "handle(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/spell_engine/client/input/SpellHotbar$Slot;Lnet/minecraft/client/option/GameOptions;)Lnet/spell_engine/client/input/SpellHotbar$Handle;",
            at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$bypassBowUse(ClientPlayerEntity player, SpellHotbar.Slot slot,
                                               GameOptions options,
                                               CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        if (com.ysjx.betterspellcasting.BowInputController.bypassSpellInput()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void betterSpellcasting$route(ClientPlayerEntity player, GameOptions options, CallbackInfoReturnable<Boolean> cir) {
        WheelController.routeHotbar(net.minecraft.client.MinecraftClient.getInstance());
    }

    @ModifyVariable(method = "handle(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/spell_engine/client/input/SpellHotbar$Slot;Lnet/minecraft/client/option/GameOptions;)Lnet/spell_engine/client/input/SpellHotbar$Handle;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private SpellHotbar.Slot betterSpellcasting$routeFocusedSlot(SpellHotbar.Slot requested) {
        if (com.ysjx.betterspellcasting.BetterSpellcastingClient.config().mode == com.ysjx.betterspellcasting.WheelMode.ORIGINAL) return requested;
        SpellHotbar.Slot canonical = SpellHotbar.INSTANCE.structuredSlots.onUseKey();
        return canonical == null ? requested : canonical;
    }

    @Inject(method = "handle(Lnet/minecraft/client/network/ClientPlayerEntity;Ljava/util/List;Lnet/minecraft/client/option/GameOptions;)Lnet/spell_engine/client/input/SpellHotbar$Handle;",
            at = @At("HEAD"))
    private void betterSpellcasting$beginHandleList(ClientPlayerEntity player, List<SpellHotbar.Slot> slots,
                                                  GameOptions options,
                                                  CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.beginHandle(slots);
    }

    @Inject(method = "handle(Lnet/minecraft/client/network/ClientPlayerEntity;Ljava/util/List;Lnet/minecraft/client/option/GameOptions;)Lnet/spell_engine/client/input/SpellHotbar$Handle;",
            at = @At("RETURN"))
    private void betterSpellcasting$finishHandleList(ClientPlayerEntity player, List<SpellHotbar.Slot> slots,
                                                        GameOptions options,
                                                        CallbackInfoReturnable<SpellHotbar.Handle> cir) {
        WheelController.finishHandle(cir.getReturnValue());
        WheelController.endHandle();
    }

    @Inject(method = "handle(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/spell_engine/client/input/SpellHotbar$Slot;Lnet/minecraft/client/option/GameOptions;)Lnet/spell_engine/client/input/SpellHotbar$Handle;",
            at = @At("RETURN"))
    private void betterSpellcasting$finishHandleSlot(ClientPlayerEntity player, SpellHotbar.Slot slot,
                                                        GameOptions options,
                                                        CallbackInfoReturnable<SpellHotbar.Handle> cir) {
    }
}
