package com.betterspellcasting.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes vanilla's item-use delay so left-click bow use follows right-click timing. */
@Mixin(MinecraftClient.class)
public interface MinecraftUseDelayAccessor {
    @Accessor("itemUseCooldown")
    int betterSpellcasting$getUseDelay();

    @Accessor("itemUseCooldown")
    void betterSpellcasting$setUseDelay(int ticks);
}
