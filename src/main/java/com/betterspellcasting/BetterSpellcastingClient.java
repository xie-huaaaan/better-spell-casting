package com.betterspellcasting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Owns Better Spellcasting's client lifecycle, input bindings, and selection state. */
public final class BetterSpellcastingClient implements ClientModInitializer {
    private static SpellcastingConfig config;
    private static KeyBinding selectKey;
    private static KeyBinding castSelectedKey;
    private static boolean selectionHeld;

    @Override
    public void onInitializeClient() {
        config = SpellcastingConfig.load();
        selectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.better-spell-casting.select_spell", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X, "key.categories.better-spell-casting"));
        castSelectedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.better-spell-casting.cast_selected", InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.categories.better-spell-casting"));
        ClientTickEvents.END_CLIENT_TICK.register(BetterSpellcastingClient::tick);
        HudRenderCallback.EVENT.register(SpellcastingRenderer::render);
    }

    public static SpellcastingConfig config() { return config == null ? new SpellcastingConfig() : config; }
    public static boolean isShortcutCastingEnabled() { return config().shortcutCasting; }
    public static boolean isWheelOpen() { return selectionHeld; }
    public static Identifier selectedSpell() { return SpellSelectionState.selected(); }
    public static KeyBinding castSelectedKey() { return castSelectedKey; }

    public static void onKey(long window, int key, int scanCode, int action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (selectKey == null || window != client.getWindow().getHandle()
                || !selectKey.matchesKey(key, scanCode)) return;
        if (client.currentScreen != null) {
            selectionHeld = false;
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            beginSelection(client);
        } else if (action == GLFW.GLFW_RELEASE) {
            endSelection(client);
        }
    }

    private static void beginSelection(MinecraftClient client) {
        if (selectionHeld) return;
        selectionHeld = true;
        SpellcastingController.ensureSelection();
        client.mouse.unlockCursor();
    }

    private static void endSelection(MinecraftClient client) {
        if (!selectionHeld) return;
        selectionHeld = false;
        SpellcastingController.confirmSelection(client);
        if (client.player != null) client.mouse.lockCursor();
    }

    public static boolean shouldBlockUse(MinecraftClient client) {
        return selectionHeld && client.currentScreen == null;
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null
                || !client.player.isAlive()) {
            selectionHeld = false;
            BowInputController.stop(client);
        }
        SpellcastingController.refresh(client);
        if (selectionHeld) SpellcastingController.updateSelection(client);
        SpellcastingController.routeHotbar(client);
    }

    public static void setSelectedSpell(Identifier id) {
        if (id != null && SpellSelectionState.select(id)) {
            SpellcastingController.routeHotbar(MinecraftClient.getInstance());
        }
    }

    public static void applyConfig(SpellcastingConfig updated) {
        BowInputController.stop(MinecraftClient.getInstance());
        config = updated.copy().normalized();
        config.save();
        selectionHeld = false;
        SpellcastingController.refresh(MinecraftClient.getInstance());
    }

}
