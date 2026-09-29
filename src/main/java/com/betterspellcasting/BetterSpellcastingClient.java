package com.betterspellcasting;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BetterSpellcastingClient::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(BetterSpellcastingClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(BetterSpellcastingClient::onOverlay);
        MinecraftForge.EVENT_BUS.addListener(BetterSpellcastingClient::onInteraction);
        MinecraftForge.EVENT_BUS.addListener(BetterSpellcastingClient::onKey);
        MinecraftForge.EVENT_BUS.addListener(BetterSpellcastingClient::onScreenInit);
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

    private static void onKey(InputEvent.Key event) {
        Minecraft client = Minecraft.getInstance();
        if (selectKey == null || !selectKey.matches(event.getKey(), event.getScanCode())) return;
        if (client.screen != null) {
            selectionHeld = false;
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS) beginSelection(client);
        else if (event.getAction() == GLFW.GLFW_RELEASE) endSelection(client);
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof OptionsScreen optionsScreen)) return;
        Button done = null;
        Button lastLeft = null;
        int lastBottom = Integer.MIN_VALUE;
        for (var listener : event.getListenersList()) {
            if (!(listener instanceof Button button)) continue;
            if (button.getMessage().getString().equals(Component.translatable("gui.done").getString())) {
                done = button;
                continue;
            }
            if (button.getX() < optionsScreen.width / 2 && button.getY() + button.getHeight() > lastBottom) {
                lastLeft = button;
                lastBottom = button.getY() + button.getHeight();
            }
        }
        if (lastLeft == null) return;
        int y = lastBottom + 4;
        if (done != null) y = Math.min(y, done.getY() - lastLeft.getHeight() - 4);
        Button entry = Button.builder(Component.translatable("screen.better-spell-casting.entry"),
                        ignored -> Minecraft.getInstance().setScreen(new SpellcastingSettingsScreen(optionsScreen)))
                .bounds(lastLeft.getX(), y, lastLeft.getWidth(), lastLeft.getHeight()).build();
        event.addListener(entry);
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

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tick(Minecraft.getInstance());
    }

    private static void onOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            SpellcastingRenderer.render(event.getGuiGraphics(), event.getPartialTick());
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

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null || !client.player.isAlive()) {
            selectionHeld = false;
            BowInputController.stop(client);
        }
        BowInputController.tick(client);
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
