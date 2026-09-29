package com.betterspellcasting.mixin;

import com.betterspellcasting.BetterSpellcastingClient;
import com.betterspellcasting.BowInputController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the client-only input takeover at the two vanilla action entry points. */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void betterSpellcasting$keepBowUse(MultiPlayerGameMode gameMode, Player player) {
        if (!BowInputController.suppressVanillaStop((Minecraft) (Object) this)) {
            gameMode.releaseUsingItem(player);
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockBowAttack(CallbackInfoReturnable<Boolean> cir) {
        if (BowInputController.blocksAttack((Minecraft) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockBowMining(boolean breaking, CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (BowInputController.blocksAttack(client)) {
            if (client.gameMode != null) client.gameMode.stopDestroyBlock();
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockUseWhileOpen(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (BetterSpellcastingClient.shouldBlockUse(client)) {
            ci.cancel();
        }
    }
}
