package com.ysjx.betterspellcasting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

/** Keeps vanilla bow and crossbow use semantics while moving their button to attack. */
public final class BowInputController {
    private static boolean leftButtonActive;
    private static boolean bypassSpellInput;
    private static boolean observedUsingItem;
    private static int restartDelay;

    private BowInputController() {
    }

    public static boolean enabled(MinecraftClient client) {
        return (BetterSpellcastingClient.isWheelMode() || BetterSpellcastingClient.isCycleMode())
                && BetterSpellcastingClient.config().bowLeftClick
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

        if (!leftButtonActive && client.options.attackKey.wasPressed()) {
            leftButtonActive = true;
            restartDelay = 0;
        }
        if (!leftButtonActive) return;

        if (client.player.isUsingItem()) {
            observedUsingItem = true;
            restartDelay = 0;
            return;
        }
        if (observedUsingItem) {
            observedUsingItem = false;
            restartDelay = 2;
            return;
        }
        if (restartDelay > 0) {
            restartDelay--;
            return;
        }

        var result = interactVanillaItem(client);
        if (result.isAccepted() && client.player.isUsingItem()) {
            observedUsingItem = true;
        } else {
            // Archers' auto-fire hook sets a two-tick item-use cooldown after releasing a shot.
            restartDelay = 2;
        }
    }

    public static void stop(MinecraftClient client) {
        if (leftButtonActive && client != null && client.interactionManager != null && client.player != null
                && client.player.isUsingItem()) {
            client.interactionManager.stopUsingItem(client.player);
        }
        leftButtonActive = false;
        observedUsingItem = false;
        restartDelay = 0;
    }

    public static boolean blocksAttack(MinecraftClient client) {
        return enabled(client);
    }

    public static boolean suppressVanillaStop(MinecraftClient client) {
        return leftButtonActive && enabled(client) && client.options.attackKey.isPressed();
    }

    /** Prevents Spell Engine's right-click arbitration from consuming the bow use call. */
    private static net.minecraft.util.ActionResult interactVanillaItem(MinecraftClient client) {
        bypassSpellInput = true;
        try {
            return client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        } finally {
            bypassSpellInput = false;
        }
    }

    public static boolean bypassSpellInput() {
        return bypassSpellInput;
    }
}
