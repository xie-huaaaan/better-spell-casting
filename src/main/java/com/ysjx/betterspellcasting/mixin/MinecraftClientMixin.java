package com.ysjx.betterspellcasting.mixin;

import com.ysjx.betterspellcasting.BetterSpellcastingClient;
import com.ysjx.betterspellcasting.BowInputController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Redirect(method = "handleInputEvents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void spellCycleWheel$keepBowUse(ClientPlayerInteractionManager manager,
                                             net.minecraft.entity.player.PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!BowInputController.suppressVanillaStop(client)) {
            manager.stopUsingItem(player);
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void spellCycleWheel$handleBowInput(CallbackInfo ci) {
        BowInputController.tick((MinecraftClient) (Object) this);
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void spellCycleWheel$blockBowAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (BowInputController.blocksAttack(client)) {
            cir.setReturnValue(false);
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(
            method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"),
            index = 0)
    private boolean spellCycleWheel$blockBowMining(boolean breaking) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        return BowInputController.blocksAttack(client) ? false : breaking;
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void spellCycleWheel$blockUseWhileOpen(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (BetterSpellcastingClient.shouldBlockUse(client)) {
            ci.cancel();
        }
    }
}
