package com.betterspellcasting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.spell_engine.client.SpellEngineClient;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.client.util.SpellRender;
import net.spell_engine.internals.casting.SpellCaster;

import java.util.ArrayList;
import java.util.List;

/** Extracts the spell HUD and radial selector using the 26.1 GUI render-state API. */
public final class SpellcastingRenderer {
    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 22;
    private static final int ICON_SIZE = 16;

    private SpellcastingRenderer() { }

    public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isSpectator() || client.screen != null) return;
        List<SpellHotbar.Slot> slots = SpellcastingController.castSlots();
        if (!slots.isEmpty()) renderHud(context, client, slots, tickCounter.getGameTimeDeltaPartialTick(true));
        if (BetterSpellcastingClient.isWheelOpen()) renderWheel(context, client, slots);
    }

    private static void renderHud(GuiGraphicsExtractor context, Minecraft client,
                                  List<SpellHotbar.Slot> slots, float tickDelta) {
        var hotbar = SpellEngineClient.hudConfig.value.hotbar;
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        Vec2 anchor = hotbar.origin.getPoint(width, height).add(hotbar.offset);
        float scale = BetterSpellcastingClient.config().hudScale / 100.0F;
        if (BetterSpellcastingClient.config().hudStyle == HudStyle.SINGLE) {
            int rowWidth = Math.round(slots.size() * SLOT_WIDTH * scale);
            int x = Math.round(anchor.x - rowWidth / 2.0F);
            int y = Math.round(anchor.y - SLOT_HEIGHT * scale);
            drawSlots(context, client, slots, tickDelta, x, y, scale,
                    BetterSpellcastingClient.config().shortcutCasting);
        } else {
            int rows = Math.min(3, Math.max(1, (int) Math.ceil(slots.size() / 5.0)));
            int columns = (int) Math.ceil(slots.size() / (double) rows);
            int rowHeight = Math.round(SLOT_HEIGHT * scale);
            int totalHeight = rows * rowHeight;
            for (int row = 0; row < rows; row++) {
                int from = row * columns;
                int to = Math.min(slots.size(), from + columns);
                int count = to - from;
                int rowWidth = Math.round(count * SLOT_WIDTH * scale);
                int x = Math.round(anchor.x - rowWidth / 2.0F);
                int y = Math.round(anchor.y - totalHeight + row * rowHeight);
                drawSlots(context, client, slots.subList(from, to), tickDelta, x, y, scale, false, from);
            }
        }
    }

    private static void drawSlots(GuiGraphicsExtractor context, Minecraft client, List<SpellHotbar.Slot> slots,
                                  float tickDelta, int x, int y, float scale, boolean showKeys) {
        drawSlots(context, client, slots, tickDelta, x, y, scale, showKeys, 0);
    }

    private static void drawSlots(GuiGraphicsExtractor context, Minecraft client, List<SpellHotbar.Slot> slots,
                                  float tickDelta, int x, int y, float scale, boolean showKeys, int indexOffset) {
        int slotWidth = Math.max(1, Math.round(SLOT_WIDTH * scale));
        int slotHeight = Math.max(1, Math.round(SLOT_HEIGHT * scale));
        for (int i = 0; i < slots.size(); i++) {
            SpellHotbar.Slot slot = slots.get(i);
            int sx = x + i * slotWidth;
            context.fill(sx, y, sx + slotWidth, y + slotHeight, 0xAA161616);
            Identifier icon = SpellRender.iconTexture(SpellcastingController.spellId(slot));
            int iconSize = Math.max(8, Math.round(ICON_SIZE * scale));
            context.blit(RenderPipelines.GUI_TEXTURED, icon, sx + (slotWidth - iconSize) / 2,
                    y + Math.max(1, Math.round(3 * scale)), 0, 0, iconSize, iconSize, iconSize, iconSize);
            if (client.player != null) {
                float cooldown = ((SpellCaster.Client) client.player).getCooldownManager()
                        .getCooldownProgress(slot.spell(), tickDelta);
                if (cooldown > 0) {
                    int top = y + Math.round(3 * scale + iconSize * (1 - cooldown));
                    context.fill(sx + (slotWidth - iconSize) / 2, top,
                            sx + (slotWidth + iconSize) / 2, y + Math.round(3 * scale) + iconSize, 0x99000000);
                }
            }
            if (SpellcastingController.spellId(slot).equals(SpellcastingController.hudSelectedSpell())) {
                context.outline(sx - 1, y - 1, sx + slotWidth + 1, y + slotHeight + 1, 0xFFF0C978);
            }
            if (showKeys && indexOffset + i < 9) {
                KeyMappingLabel.draw(context, client, SpellcastingController.shortcutKey(client, indexOffset + i),
                        sx + slotWidth / 2, y - 10);
            }
        }
    }

    private static void renderWheel(GuiGraphicsExtractor context, Minecraft client, List<SpellHotbar.Slot> slots) {
        if (slots.isEmpty()) return;
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        int cx = width / 2;
        int cy = height / 2;
        int outer = Math.min(118, Math.max(88, Math.min(width, height) / 5));
        int inner = 38;
        int selected = SpellcastingController.selectedIndex();
        context.fill(0, 0, width, height, 0x33000000);
        for (int i = 0; i < slots.size(); i++) {
            int color = i == selected ? 0xC8D9A86C : 0x8C241B16;
            drawSectorApproximation(context, cx, cy, inner, outer, i, slots.size(), color);
            double angle = -Math.PI / 2 + Math.PI * 2 * i / slots.size();
            int radius = outer - 19;
            int x = cx + (int) Math.round(Math.cos(angle) * radius) - 8;
            int y = cy + (int) Math.round(Math.sin(angle) * radius) - 8;
            context.blit(RenderPipelines.GUI_TEXTURED, SpellRender.iconTexture(SpellcastingController.spellId(slots.get(i))),
                    x, y, 0, 0, 16, 16, 16, 16);
            if (i == selected) context.outline(x - 2, y - 2, x + 18, y + 18, 0xFFF0C978);
        }
        context.fill(cx - 24, cy - 24, cx + 24, cy + 24, 0xDD120E0A);
        if (selected >= 0 && selected < slots.size()) {
            renderDetails(context, client, SpellcastingController.spellId(slots.get(selected)),
                    Math.min(width - 260, cx + outer + 24), cy - 90, width, height);
        }
    }

    private static void drawSectorApproximation(GuiGraphicsExtractor context, int cx, int cy,
                                                  int inner, int outer, int index, int count, int color) {
        double start = -Math.PI / 2 - Math.PI / count + index * Math.PI * 2 / count;
        double end = start + Math.PI * 2 / count;
        int steps = Math.max(4, (int) Math.ceil((end - start) * outer / 8));
        for (int s = 0; s <= steps; s++) {
            double angle = start + (end - start) * s / steps;
            int x0 = cx + (int) Math.round(Math.cos(angle) * inner);
            int y0 = cy + (int) Math.round(Math.sin(angle) * inner);
            int x1 = cx + (int) Math.round(Math.cos(angle) * outer);
            int y1 = cy + (int) Math.round(Math.sin(angle) * outer);
            drawLine(context, x0, y0, x1, y1, color);
        }
    }

    private static void drawLine(GuiGraphicsExtractor context, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            context.fill(x0 - 2, y0 - 2, x0 + 3, y0 + 3, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    private static void renderDetails(GuiGraphicsExtractor context, Minecraft client, Identifier spellId,
                                      int x, int y, int screenWidth, int screenHeight) {
        if (client.player == null) return;
        List<Component> source = SpellTooltip.spellDescriptionWithDetails(spellId, client.player, ItemStack.EMPTY, 0);
        int panelWidth = Math.min(250, Math.max(150, screenWidth - x - 8));
        List<net.minecraft.util.FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : source) wrapped.addAll(client.font.split(line, panelWidth - 16));
        int panelHeight = 12 + wrapped.size() * client.font.lineHeight;
        y = Math.max(8, Math.min(y, screenHeight - panelHeight - 8));
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xCC101010);
        context.fill(x, y, x + panelWidth, y + 1, 0xFFF0C978);
        int lineY = y + 6;
        for (var line : wrapped) {
            context.text(client.font, line, x + 8, lineY, 0xFFFFFFFF);
            lineY += client.font.lineHeight;
        }
    }

    private static final class KeyMappingLabel {
        static void draw(GuiGraphicsExtractor context, Minecraft client, net.minecraft.client.KeyMapping key,
                         int x, int y) {
            if (key != null) context.centeredText(client.font, key.getTranslatedKeyMessage(), x, y, 0xFFFFFFFF);
        }
    }
}
