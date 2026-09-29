package com.betterspellcasting;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Owns the client lifecycle, key bindings, and the shared radial selection state. */
public final class BetterSpellcastingClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("better-spell-casting", "controls"));
    private static SpellcastingConfig config;
    private static KeyMapping selectKey;
    private static KeyMapping castSelectedKey;
    private static boolean selectionHeld;
    private static boolean previousSelectDown;

    @Override
    public void onInitializeClient() {
        config = SpellcastingConfig.load();
        selectKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.better-spell-casting.select_spell", InputConstants.Type.KEYSYM,
                InputConstants.KEY_X, CATEGORY));
        castSelectedKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.better-spell-casting.cast_selected", InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_RIGHT, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(BetterSpellcastingClient::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("better-spell-casting", "hud"),
                (context, tickCounter) -> SpellcastingRenderer.render(context, tickCounter));
    }

    public static SpellcastingConfig config() { return config == null ? new SpellcastingConfig() : config; }
    public static boolean isShortcutCastingEnabled() { return config().shortcutCasting; }
    public static boolean isWheelOpen() { return selectionHeld; }
    public static Identifier selectedSpell() { return SpellSelectionState.selected(); }
    public static KeyMapping castSelectedKey() { return castSelectedKey; }

    private static void tick(Minecraft client) {
        boolean selectDown = selectKey != null && selectKey.isDown();
        if (client.screen != null || client.player == null || client.level == null || !client.player.isAlive()) {
            if (selectionHeld) endSelection(client);
            previousSelectDown = selectDown;
            BowInputController.stop(client);
            return;
        }
        if (selectDown && !previousSelectDown) beginSelection(client);
        if (!selectDown && previousSelectDown) endSelection(client);
        previousSelectDown = selectDown;
        SpellcastingController.refresh(client);
        if (selectionHeld) SpellcastingController.updateSelection(client);
        SpellcastingController.routeHotbar(client);
        BowInputController.tick(client);
    }

    private static void beginSelection(Minecraft client) {
        if (selectionHeld) return;
        selectionHeld = true;
        SpellcastingController.ensureSelection();
        client.mouseHandler.releaseMouse();
    }

    private static void endSelection(Minecraft client) {
        if (!selectionHeld) return;
        selectionHeld = false;
        SpellcastingController.confirmSelection(client);
        if (client.player != null) client.mouseHandler.grabMouse();
    }

    public static boolean shouldBlockUse(Minecraft client) { return selectionHeld && client.screen == null; }

    public static void setSelectedSpell(Identifier id) {
        if (id != null && SpellSelectionState.select(id)) {
            SpellcastingController.routeHotbar(Minecraft.getInstance());
        }
    }

    public static void applyConfig(SpellcastingConfig updated) {
        BowInputController.stop(Minecraft.getInstance());
        config = updated.copy().normalized();
        config.save();
        selectionHeld = false;
        SpellcastingController.refresh(Minecraft.getInstance());
    }
}
