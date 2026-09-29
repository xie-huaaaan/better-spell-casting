package com.betterspellcasting;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModLoadingContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Owns the Forge client lifecycle, input bindings, and shared selection state. */
@Mod(BetterSpellcastingClient.MOD_ID)
public final class BetterSpellcastingClient {
    public static final String MOD_ID = "better_spell_casting";
    private static SpellcastingConfig config;
    private static KeyMapping selectKey;
    private static KeyMapping castSelectedKey;
    private static boolean selectionHeld;

    public BetterSpellcastingClient() {
        config = SpellcastingConfig.load();
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(BetterSpellcastingClient::registerKeys);
        NeoForge.EVENT_BUS.addListener(BetterSpellcastingClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(BetterSpellcastingClient::onOverlay);
        NeoForge.EVENT_BUS.addListener(BetterSpellcastingClient::onInteraction);
        NeoForge.EVENT_BUS.addListener(BetterSpellcastingClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(BetterSpellcastingClient::onKeyInput);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        selectKey = new KeyMapping("key.better-spell-casting.select_spell", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X, "key.categories.better-spell-casting");
        castSelectedKey = new KeyMapping("key.better-spell-casting.cast_selected", InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.categories.better-spell-casting");
        event.register(selectKey);
        event.register(castSelectedKey);
    }

    public static SpellcastingConfig config() { return config == null ? new SpellcastingConfig() : config; }
    public static boolean isShortcutCastingEnabled() { return config().shortcutCasting; }
    public static boolean isWheelOpen() { return selectionHeld; }
    public static ResourceLocation selectedSpell() { return SpellSelectionState.selected(); }
    public static KeyMapping castSelectedKey() { return castSelectedKey; }

    public static void onKey(long window, int key, int scanCode, int action) {
        Minecraft client = Minecraft.getInstance();
        if (selectKey == null || window != client.getWindow().getWindow()
                || !selectKey.matches(key, scanCode)) return;
        if (client.screen != null) {
            selectionHeld = false;
            return;
        }
        if (action == GLFW.GLFW_PRESS) beginSelection(client);
        else if (action == GLFW.GLFW_RELEASE) endSelection(client);
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

    public static boolean shouldBlockUse(Minecraft client) {
        return selectionHeld && client.screen == null;
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        tick(Minecraft.getInstance());
    }

    private static void onOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            SpellcastingRenderer.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    private static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft client = Minecraft.getInstance();
        if (event.isAttack() && BowInputController.blocksAttack(client)) {
            event.setCanceled(true);
        } else if (event.isUseItem() && shouldBlockUse(client)) {
            event.setCanceled(true);
        }
    }

    private static void onKeyInput(InputEvent.Key event) {
        onKey(Minecraft.getInstance().getWindow().getWindow(), event.getKey(), event.getScanCode(), event.getAction());
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof OptionsScreen screen)) return;
        Button entry = Button.builder(Component.translatable("screen.better-spell-casting.entry"),
                        ignored -> Minecraft.getInstance().setScreen(new SpellcastingSettingsScreen(screen)))
                .bounds(screen.width / 2 - 100, screen.height - 52, 200, 20)
                .build();
        screen.renderables.add(entry);
        event.addListener(entry);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null || !client.player.isAlive()) {
            selectionHeld = false;
            BowInputController.stop(client);
        }
        SpellcastingController.refresh(client);
        if (selectionHeld) SpellcastingController.updateSelection(client);
        SpellcastingController.routeHotbar(client);
    }

    public static void setSelectedSpell(ResourceLocation id) {
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

