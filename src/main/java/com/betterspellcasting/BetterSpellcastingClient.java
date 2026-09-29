package com.betterspellcasting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Owns Better Spellcasting's client lifecycle, input bindings, and selection state. */
public final class BetterSpellcastingClient implements ClientModInitializer {
    private static SpellcastingConfig config;
    private static KeyBinding selectKey;
    private static KeyBinding castSelectedKey;
    private static boolean selectionHeld;
    private static boolean selectionKeyDown;

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
        ScreenEvents.AFTER_INIT.register(BetterSpellcastingClient::addSettingsEntry);
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
            selectionKeyDown = false;
            selectionHeld = false;
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            selectionKeyDown = true;
            beginSelection(client);
        } else if (action == GLFW.GLFW_RELEASE) {
            selectionKeyDown = false;
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

    /** Polls the binding as a fallback for loaders that do not forward the GLFW callback to every mixin. */
    private static void pollSelectionKey(MinecraftClient client) {
        if (selectKey == null || client.currentScreen != null) {
            selectionKeyDown = false;
            selectionHeld = false;
            return;
        }
        boolean down = selectKey.isPressed();
        if (down && !selectionKeyDown) {
            selectionKeyDown = true;
            beginSelection(client);
        } else if (!down && selectionKeyDown) {
            selectionKeyDown = false;
            endSelection(client);
        }
    }

    public static boolean shouldBlockUse(MinecraftClient client) {
        return selectionHeld && client.currentScreen == null;
    }

    private static void tick(MinecraftClient client) {
        pollSelectionKey(client);
        if (client.player == null || client.world == null || client.currentScreen != null
                || !client.player.isAlive()) {
            selectionHeld = false;
            selectionKeyDown = false;
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

    private static void addSettingsEntry(MinecraftClient client, Screen screen, int width, int height) {
        if (!(screen instanceof OptionsScreen)) return;
        ClickableWidget done = null;
        for (ClickableWidget button : Screens.getButtons(screen)) {
            TextContent content = button.getMessage().getContent();
            if (content instanceof TranslatableTextContent translatable
                    && translatable.getKey().equals("gui.done")) done = button;
        }
        if (done == null) return;
        ClickableWidget doneButton = done;
        var options = Screens.getButtons(screen).stream()
                .filter(button -> button != doneButton && button.visible)
                .sorted(java.util.Comparator.comparingInt(ClickableWidget::getY)
                        .thenComparingInt(ClickableWidget::getX)).toList();
        if (options.isEmpty()) return;
        var rows = options.stream().map(ClickableWidget::getY).distinct().sorted().toList();
        int rowStep = inferRowStep(rows, done.getY() - rows.get(rows.size() - 1));
        int cellWidth = options.stream().mapToInt(ClickableWidget::getWidth).min().orElse(done.getWidth() / 2);
        int x = options.stream().mapToInt(ClickableWidget::getX).min().orElse(done.getX());
        int y = done.getY();
        int buttonWidth = cellWidth;
        if (done.getY() + rowStep + done.getHeight() <= height - 4) {
            done.setY(done.getY() + rowStep);
        } else {
            int rowY = rows.get(rows.size() - 1);
            y = rowY;
            int rowMaxX = options.stream().filter(button -> button.getY() == rowY)
                    .mapToInt(ClickableWidget::getX).max().orElse(x);
            x = rowMaxX + cellWidth + Math.max(0, rowMaxX - x - cellWidth);
            if (x + cellWidth > width) {
                int gridWidth = rowMaxX + cellWidth - options.stream()
                        .mapToInt(ClickableWidget::getX).min().orElse(x);
                x = (width - gridWidth) / 2;
                y = Math.max(0, rows.get(0) - rowStep);
                buttonWidth = gridWidth;
            }
        }
        Screens.getButtons(screen).add(ButtonWidget.builder(Text.translatable("screen.better-spell-casting.entry"),
                        ignored -> client.setScreen(new SpellcastingSettingsScreen(screen)))
                .dimensions(x, y, buttonWidth, done.getHeight())
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                        Text.translatable("screen.better-spell-casting.entry.tooltip")))
                .build());
    }

    private static int inferRowStep(java.util.List<Integer> rows, int doneGap) {
        int step = doneGap;
        for (int i = 1; i < rows.size(); i++) {
            int gap = rows.get(i) - rows.get(i - 1);
            if (gap > 0 && (step <= 0 || gap < step)) step = gap;
        }
        return Math.max(24, step);
    }
}
