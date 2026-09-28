package com.ysjx.spellcyclewheel;

import com.yuansujuexing.spellcycle.OperationMode;
import com.yuansujuexing.spellcycle.SpellCycleClient;
import com.yuansujuexing.spellcycle.SpellCycleConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.spell_engine.SpellEngineMod;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.internals.casting.SpellCasterClient;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class SpellCycleWheelClient implements ClientModInitializer {
    public static final String MOD_ID = "spell_cycle_wheel";
    private static WheelConfig config;
    private static KeyBinding wheelKey;
    private static KeyBinding castSelectedKey;
    private static boolean wheelOpen;
    /** Spell Cycle touches MinecraftClient.options while applying its runtime config. */
    private static boolean legacyModePending;

    @Override
    public void onInitializeClient() {
        boolean hadConfig = WheelConfig.exists();
        config = WheelConfig.load();
        if (!hadConfig) {
            SpellCycleConfig legacy = SpellCycleClient.configCopy();
            config.mode = switch (legacy.operationMode) {
                case ORIGINAL -> WheelMode.ORIGINAL;
                case CYCLE, HYBRID -> WheelMode.CYCLE;
                default -> WheelMode.ORIGINAL;
            };
            config.shortcutCasting = legacy.operationMode == OperationMode.HYBRID;
            config.reverseScroll = legacy.reverseScroll;
            config.save();
        }
        wheelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spell_cycle_wheel.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "key.categories.spell_cycle"));
        castSelectedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spell_cycle_wheel.cast_selected",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                SpellEngineMod.modName()));
        // Connector can invoke Fabric client entrypoints from Minecraft's early
        // constructor, before GameOptions has been created. Spell Cycle's
        // applyConfig() assumes options is already available, so defer this
        // one-time runtime mapping to the first client tick.
        legacyModePending = true;
        ClientTickEvents.END_CLIENT_TICK.register(SpellCycleWheelClient::tick);
        HudRenderCallback.EVENT.register(WheelRenderer::render);
        ScreenEvents.AFTER_INIT.register(SpellCycleWheelClient::addSettingsEntry);
    }

    public static WheelConfig config() {
        return config == null ? new WheelConfig() : config;
    }

    public static boolean isWheelMode() {
        return config != null && config.mode == WheelMode.WHEEL;
    }

    public static boolean isShortcutCastingEnabled() {
        return config != null && (config.mode == WheelMode.ORIGINAL || config.shortcutCasting);
    }

    public static boolean isWheelOpen() {
        return wheelOpen;
    }

    public static Identifier selectedSpell() {
        return SpellCycleSelectionBridge.selected();
    }

    public static KeyBinding castSelectedKey() {
        return castSelectedKey;
    }

    public static void onKey(long window, int key, int scanCode, int action) {
        if (!isWheelMode() || wheelKey == null || window != MinecraftClient.getInstance().getWindow().getHandle()
                || !wheelKey.matchesKey(key, scanCode)) {
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            openWheel();
        } else if (action == GLFW.GLFW_RELEASE) {
            closeWheel(true);
        }
    }

    public static void onMouseButton(long window, int button, int action) {
        // The use key is blocked by MinecraftClientMixin while the wheel is open.
    }

    public static boolean shouldBlockUse(MinecraftClient client) {
        return isWheelOpen() && client.currentScreen == null;
    }

    private static void tick(MinecraftClient client) {
        if (legacyModePending) {
            applyLegacyMode(client);
        }
        if (!isWheelMode()) {
            wheelOpen = false;
            return;
        }
        if (wheelOpen && (client.currentScreen != null || client.player == null || client.world == null
                || client.player.isSpectator() || !client.player.isAlive())) {
            closeWheel(false);
        }
        if (wheelOpen) {
            WheelController.updateSelection(client);
        }
    }

    private static void openWheel() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null || client.player == null || client.world == null || client.player.isSpectator()) {
            return;
        }
        if (!WheelController.hasSpells()) {
            return;
        }
        wheelOpen = true;
        WheelController.ensureSelection();
        client.mouse.unlockCursor();
    }

    private static void closeWheel(boolean confirm) {
        MinecraftClient client = MinecraftClient.getInstance();
        wheelOpen = false;
        if (confirm) {
            WheelController.ensureSelection();
            WheelController.routeHotbar(client);
        }
        if (client.currentScreen == null && client.player != null) {
            client.mouse.lockCursor();
        }
    }

    public static void setSelectedSpell(Identifier id) {
        if (id != null && SpellCycleSelectionBridge.select(id)) {
            WheelController.routeHotbar(MinecraftClient.getInstance());
        }
    }

    public static void applyConfig(WheelConfig updated) {
        BowInputController.stop(MinecraftClient.getInstance());
        config = updated.copy().normalized();
        config.save();
        wheelOpen = false;
        legacyModePending = true;
        applyLegacyMode(MinecraftClient.getInstance());
        WheelController.routeHotbar(MinecraftClient.getInstance());
    }

    private static void applyLegacyMode(MinecraftClient client) {
        if (config == null) return;
        if (client == null || client.options == null) {
            legacyModePending = true;
            return;
        }
        SpellCycleConfig legacy = SpellCycleClient.configCopy();
        legacy.operationMode = switch (config.mode) {
            case ORIGINAL -> OperationMode.ORIGINAL;
            case CYCLE -> config.shortcutCasting ? OperationMode.HYBRID : OperationMode.CYCLE;
            case WHEEL -> OperationMode.ORIGINAL;
        };
        legacy.reverseScroll = config.reverseScroll;
        try {
            legacy.save();
        } catch (Exception ignored) {
            // The original mod still receives the in-memory mapping below.
        }
        SpellCycleClient.applyConfig(legacy);
        legacyModePending = false;
    }

    private static void addSettingsEntry(MinecraftClient client, Screen screen, int width, int height) {
        if (!(screen instanceof ControlsOptionsScreen)) return;
        int buttonWidth = Math.min(200, width - 16);
        int x = (width - buttonWidth) / 2;
        int y = height / 6 + 84;
        ClickableWidget original = null;
        ClickableWidget done = null;
        for (var button : Screens.getButtons(screen)) {
            TextContent content = button.getMessage().getContent();
            if (content instanceof TranslatableTextContent translatable
                    && translatable.getKey().equals("screen.spell_cycle.entry")) {
                original = button;
            }
            if (content instanceof TranslatableTextContent translatable
                    && translatable.getKey().equals("gui.done")) {
                done = button;
            }
        }
        if (original != null) {
            original.visible = false;
            original.active = false;
        }
        if (done != null) {
            x = done.getX();
            y = Math.max(0, done.getY() - 24);
            buttonWidth = done.getWidth();
        } else if (original != null) {
            x = original.getX();
            y = original.getY();
            buttonWidth = original.getWidth();
        } else {
            y = Math.min(y, height - 24);
        }
        while (y > 0 && overlapsVisibleButton(screen, original, x, y, buttonWidth, 20)) {
            y--;
        }
        Screens.getButtons(screen).add(net.minecraft.client.gui.widget.ButtonWidget.builder(
                Text.translatable("screen.spell_cycle.entry"),
                ignored -> client.setScreen(new WheelSettingsScreen(screen)))
                .dimensions(x, y, buttonWidth, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("screen.spell_cycle.entry.tooltip")))
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
