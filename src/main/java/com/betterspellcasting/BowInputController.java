package com.betterspellcasting;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;

/** Keeps vanilla bow and crossbow use semantics while moving their button to attack. */
public final class BowInputController {
    private static boolean leftButtonActive;
    private static boolean bypassSpellInput;
    private static boolean observedUsingItem;
    private static int restartDelay;

    private BowInputController() {
    }

    public static boolean enabled(Minecraft client) {
        return BetterSpellcastingClient.config().bowLeftClick
                && client != null
                && client.screen == null
                && client.level != null
                && client.player != null
                && client.player.isAlive()
                && isRangedWeapon(client.player.getMainHandItem().getItem());
    }

    public static boolean isRangedWeapon(Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    public static void tick(Minecraft client) {
        if (!enabled(client)) {
            stop(client);
            return;
        }

        if (client.gameMode == null) {
            stop(client);
            return;
        }
        if (!client.options.keyAttack.isDown()) {
            stop(client);
            return;
        }

        if (!leftButtonActive) {
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
        if (result.consumesAction() && client.player.isUsingItem()) {
            observedUsingItem = true;
        } else {
            // Archers' auto-fire hook sets a two-tick item-use cooldown after releasing a shot.
            restartDelay = 2;
        }
    }

    public static void stop(Minecraft client) {
        if (leftButtonActive && client != null && client.gameMode != null && client.player != null
                && client.player.isUsingItem()) {
            client.gameMode.releaseUsingItem(client.player);
        }
        leftButtonActive = false;
        observedUsingItem = false;
        restartDelay = 0;
    }

    public static boolean blocksAttack(Minecraft client) {
        return enabled(client);
    }

    public static boolean suppressVanillaStop(Minecraft client) {
        return leftButtonActive && enabled(client) && client.options.keyAttack.isDown();
    }

    /** Prevents Spell Engine's right-click arbitration from consuming the bow use call. */
    private static net.minecraft.world.InteractionResult interactVanillaItem(Minecraft client) {
        bypassSpellInput = true;
        try {
            return client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        } finally {
            bypassSpellInput = false;
        }
    }

    public static boolean bypassSpellInput() {
        return bypassSpellInput;
    }
}
