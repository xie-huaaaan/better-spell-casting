package com.betterspellcasting;

import com.betterspellcasting.mixin.MinecraftUseDelayAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

/** Keeps vanilla bow and crossbow use semantics while moving their button to attack. */
public final class BowInputController {
    private static boolean leftButtonActive;
    private static boolean bypassSpellInput;

    private BowInputController() {
    }

    public static boolean enabled(MinecraftClient client) {
        return BetterSpellcastingClient.config().bowLeftClick
                && client != null
                && client.currentScreen == null
                && client.world != null
                && client.player != null
                && client.player.isAlive()
                && isRangedWeapon(client.player.getMainHandStack().getItem());
    }

    public static boolean isRangedWeapon(Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    public static void tick(MinecraftClient client) {
        if (!enabled(client)) {
            stop(client);
            return;
        }

        if (client.interactionManager == null) {
            stop(client);
            return;
        }
        if (!client.options.attackKey.isPressed()) {
            stop(client);
            return;
        }

        if (!leftButtonActive) {
            leftButtonActive = true;
        }
        if (client.player.isUsingItem()) return;
        MinecraftUseDelayAccessor useDelay = (MinecraftUseDelayAccessor) client;
        if (useDelay.betterSpellcasting$getUseDelay() > 0) return;
        useDelay.betterSpellcasting$setUseDelay(4);
        interactVanillaItem(client);
    }

    public static void stop(MinecraftClient client) {
        if (leftButtonActive && client != null && client.interactionManager != null && client.player != null
                && client.player.isUsingItem()) {
            client.interactionManager.stopUsingItem(client.player);
        }
        leftButtonActive = false;
    }

    public static boolean blocksAttack(MinecraftClient client) {
        return enabled(client);
    }

    public static boolean suppressVanillaStop(MinecraftClient client) {
        return leftButtonActive && enabled(client) && client.options.attackKey.isPressed();
    }

    /** Prevents Spell Engine's right-click arbitration from consuming the bow use call. */
    private static void interactVanillaItem(MinecraftClient client) {
        bypassSpellInput = true;
        try {
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        } finally {
            bypassSpellInput = false;
        }
    }

    public static boolean bypassSpellInput() {
        return bypassSpellInput;
    }
}
