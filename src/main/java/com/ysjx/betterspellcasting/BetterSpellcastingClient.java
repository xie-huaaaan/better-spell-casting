package com.ysjx.betterspellcasting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Owns Better Spellcasting's client lifecycle, input bindings, and mode state. */
public final class BetterSpellcastingClient implements ClientModInitializer {
    public static final String MOD_ID = "better_spellcasting";
    private static WheelConfig config;
    private static KeyBinding selectKey;
    private static KeyBinding castSelectedKey;
    private static boolean selectionHeld;

    @Override
    public void onInitializeClient() {
        config = WheelConfig.load();
        selectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.better_spellcasting.select_spell", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X, "key.categories.better_spellcasting"));
        castSelectedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.better_spellcasting.cast_selected", InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.categories.better_spellcasting"));
        ClientTickEvents.END_CLIENT_TICK.register(BetterSpellcastingClient::tick);
        HudRenderCallback.EVENT.register(WheelRenderer::render);
        ScreenEvents.AFTER_INIT.register(BetterSpellcastingClient::addSettingsEntry);
    }

    public static WheelConfig config() { return config == null ? new WheelConfig() : config; }
    public static boolean isWheelMode() { return config().mode == WheelMode.WHEEL; }
    public static boolean isCycleMode() { return config().mode == WheelMode.CYCLE; }
    public static boolean isShortcutCastingEnabled() {
        return config().mode == WheelMode.ORIGINAL || config().shortcutCasting;
    }
    public static boolean isWheelOpen() { return selectionHeld && isWheelMode(); }
    public static boolean isSelectionHeld() { return selectionHeld; }
    public static Identifier selectedSpell() { return SpellSelectionState.selected(); }
    public static KeyBinding castSelectedKey() { return castSelectedKey; }

    public static void onKey(long window, int key, int scanCode, int action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (selectKey == null || window != client.getWindow().getHandle()
                || !selectKey.matchesKey(key, scanCode) || client.currentScreen != null) return;
        if (action == GLFW.GLFW_PRESS) {
            selectionHeld = true;
            WheelController.ensureSelection();
            if (isWheelMode()) client.mouse.unlockCursor();
        } else if (action == GLFW.GLFW_RELEASE) {
            selectionHeld = false;
            WheelController.confirmSelection(client);
            if (client.player != null) client.mouse.lockCursor();
        }
    }

    public static void onScroll(double horizontal, double vertical) {
        if (!isCycleMode() || !selectionHeld || vertical == 0) return;
        int direction = vertical < 0 ? 1 : -1;
        if (config().reverseScroll) direction = -direction;
        WheelController.stepSelection(direction);
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
        WheelController.refresh(client);
        if (isWheelMode() && selectionHeld) WheelController.updateSelection(client);
        WheelController.routeHotbar(client);
    }

    public static void setSelectedSpell(Identifier id) {
        if (id != null && SpellSelectionState.select(id)) {
            WheelController.routeHotbar(MinecraftClient.getInstance());
        }
    }

    public static void applyConfig(WheelConfig updated) {
        BowInputController.stop(MinecraftClient.getInstance());
        config = updated.copy().normalized();
        config.save();
        selectionHeld = false;
        WheelController.refresh(MinecraftClient.getInstance());
    }

    private static void addSettingsEntry(MinecraftClient client, Screen screen, int width, int height) {
        if (!(screen instanceof ControlsOptionsScreen)) return;
        ClickableWidget old = null;
        ClickableWidget done = null;
        for (ClickableWidget button : Screens.getButtons(screen)) {
            TextContent content = button.getMessage().getContent();
            if (content instanceof TranslatableTextContent translatable
                    && translatable.getKey().equals("screen.spell_cycle.entry")) old = button;
            if (content instanceof TranslatableTextContent translatable
                    && translatable.getKey().equals("gui.done")) done = button;
        }
        if (old != null) { old.visible = false; old.active = false; }
        if (done == null && old == null) return;
        ClickableWidget anchor = done != null ? done : old;
        int x = anchor.getX();
        int y = done != null ? Math.max(0, done.getY() - 24) : old.getY();
        int w = anchor.getWidth();
        while (y > 0 && overlapsVisibleButton(screen, old, x, y, w, 20)) y--;
        Screens.getButtons(screen).add(ButtonWidget.builder(Text.translatable("screen.better_spellcasting.entry"),
                        ignored -> client.setScreen(new WheelSettingsScreen(screen)))
                .dimensions(x, y, w, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                        Text.translatable("screen.better_spellcasting.entry.tooltip")))
                .build());
    }

    private static boolean overlapsVisibleButton(Screen screen, ClickableWidget ignored,
                                                   int x, int y, int width, int height) {
        for (ClickableWidget button : Screens.getButtons(screen)) {
            if (button == ignored || !button.visible) continue;
            boolean separated = x + width <= button.getX() || button.getX() + button.getWidth() <= x
                    || y + height <= button.getY() || button.getY() + button.getHeight() <= y;
            if (!separated) return true;
        }
        return false;
    }
}
