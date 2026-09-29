package com.betterspellcasting.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes vanilla's item-use delay so repeated left-click bow use follows vanilla timing. */
@Mixin(Minecraft.class)
public interface MinecraftUseDelayAccessor {
    @Accessor("rightClickDelay")
    int betterSpellcasting$getUseDelay();

    @Accessor("rightClickDelay")
    void betterSpellcasting$setUseDelay(int ticks);
}
