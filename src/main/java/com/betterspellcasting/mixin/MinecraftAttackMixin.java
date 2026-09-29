package com.betterspellcasting.mixin;

import com.betterspellcasting.BowInputController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stops vanilla attack and mining paths while the left bow control owns the attack key. */
@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void betterSpellcasting$handleBowInput(CallbackInfo callback) {
        BowInputController.tick((Minecraft) (Object) this);
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void betterSpellcasting$keepBowUse(net.minecraft.client.multiplayer.MultiPlayerGameMode gameMode,
                                                 net.minecraft.world.entity.player.Player player) {
        Minecraft client = (Minecraft) (Object) this;
        if (!BowInputController.suppressVanillaStop(client)) {
            gameMode.releaseUsingItem(player);
        }
    }
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockStartAttack(CallbackInfoReturnable<Boolean> callback) {
        if (BowInputController.blocksAttack((Minecraft) (Object) this)) callback.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void betterSpellcasting$blockContinueAttack(boolean leftClick, CallbackInfo callback) {
        if (leftClick && BowInputController.blocksAttack((Minecraft) (Object) this)) callback.cancel();
    }
}
