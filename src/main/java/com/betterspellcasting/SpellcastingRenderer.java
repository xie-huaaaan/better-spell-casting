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
        int selectedIndex = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (SpellcastingController.spellId(slots.get(i)).equals(SpellcastingController.hudSelectedSpell())) {
                selectedIndex = i;
                break;
            }
        }
        if (BetterSpellcastingClient.config().hudStyle == HudStyle.SINGLE) {
            int rowWidth = Math.round(slots.size() * SLOT_WIDTH * scale);
            int x = Math.round(anchor.x - rowWidth / 2.0F);
            int y = Math.round(anchor.y - SLOT_HEIGHT * scale);
            drawSlots(context, client, slots, tickDelta, x, y, scale,
                    BetterSpellcastingClient.config().shortcutCasting);
            if (selectedIndex >= 0) drawSelectedBorder(context, x + selectedIndex * Math.max(1, Math.round(SLOT_WIDTH * scale)),
                    y, scale);
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
            if (selectedIndex >= 0) {
                int row = selectedIndex / columns;
                int count = Math.min(columns, slots.size() - row * columns);
                int x = Math.round(anchor.x - Math.round(count * SLOT_WIDTH * scale) / 2.0F);
                int y = Math.round(anchor.y - totalHeight + row * rowHeight);
                drawSelectedBorder(context, x + (selectedIndex % columns) * Math.max(1, Math.round(SLOT_WIDTH * scale)),
                        y, scale);
            }
        }
    }

    private static void drawSelectedBorder(GuiGraphicsExtractor context, int x, int y, float scale) {
        int width = Math.max(1, Math.round(SLOT_WIDTH * scale));
        int height = Math.max(1, Math.round(SLOT_HEIGHT * scale));
        context.fill(x - 2, y - 2, x + width + 2, y, 0xFFFFFFFF);
        context.fill(x - 2, y + height, x + width + 2, y + height + 2, 0xFFFFFFFF);
        context.fill(x - 2, y, x, y + height, 0xFFFFFFFF);
        context.fill(x + width, y, x + width + 2, y + height, 0xFFFFFFFF);
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
        drawRadialSectors(context, cx, cy, inner, outer, slots.size(), selected);
        for (int i = 0; i < slots.size(); i++) {
            double angle = -Math.PI / 2 + Math.PI * 2 * i / slots.size();
            int radius = outer - 19;
            int x = cx + (int) Math.round(Math.cos(angle) * radius) - 8;
            int y = cy + (int) Math.round(Math.sin(angle) * radius) - 8;
            context.blit(RenderPipelines.GUI_TEXTURED, SpellRender.iconTexture(SpellcastingController.spellId(slots.get(i))),
                    x, y, 0, 0, 16, 16, 16, 16);
            if (i == selected) context.outline(x - 2, y - 2, 20, 20, 0xFFF0C978);
        }
        for (int dy = -inner; dy <= inner; dy++) {
            int halfWidth = (int) Math.sqrt(inner * inner - dy * dy);
            context.fill(cx - halfWidth, cy + dy, cx + halfWidth + 1, cy + dy + 1, 0xDD120E0A);
        }
        if (selected >= 0 && selected < slots.size()) {
            renderDetails(context, client, SpellcastingController.spellId(slots.get(selected)),
                    Math.min(width - 260, cx + outer + 24), cy - 90, width, height);
        }
    }

    private static void drawRadialSectors(GuiGraphicsExtractor context, int cx, int cy,
                                          int inner, int outer, int count, int selected) {
        double sectorAngle = Math.PI * 2 / count;
        int innerSquared = inner * inner;
        int outerSquared = outer * outer;
        for (int dy = -outer; dy <= outer; dy++) {
            int extent = (int) Math.sqrt(outerSquared - dy * dy);
            int previousColor = 0;
            int spanStart = -extent;
            for (int dx = -extent; dx <= extent + 1; dx++) {
                int color = 0;
                int distanceSquared = dx * dx + dy * dy;
                if (dx <= extent && distanceSquared >= innerSquared && distanceSquared <= outerSquared) {
                    double angle = Math.atan2(dx, -dy);
                    if (angle < 0) angle += Math.PI * 2;
                    int sector = (int) Math.floor((angle + sectorAngle / 2) / sectorAngle) % count;
                    color = sector == selected ? 0xC8D9A86C : 0x8C241B16;
                }
                if (color != previousColor) {
                    if (previousColor != 0) {
                        context.fill(cx + spanStart, cy + dy, cx + dx, cy + dy + 1, previousColor);
                    }
                    spanStart = dx;
                    previousColor = color;
                }
            }
        }
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 - Math.PI / count + i * sectorAngle;
            int x0 = cx + (int) Math.round(Math.cos(angle) * inner);
            int y0 = cy + (int) Math.round(Math.sin(angle) * inner);
            int x1 = cx + (int) Math.round(Math.cos(angle) * outer);
            int y1 = cy + (int) Math.round(Math.sin(angle) * outer);
            drawDivider(context, x0, y0, x1, y1);
        }
    }

    private static void drawDivider(GuiGraphicsExtractor context, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            context.fill(x0, y0, x0 + 2, y0 + 2, 0xD0E0B76B);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    private static void renderDetails(GuiGraphicsExtractor context, Minecraft client, Identifier spellId,
                                      int x, int y, int screenWidth, int screenHeight) {
        if (client.player == null) return;
        List<Component> titleEntries = SpellTooltip.spellEntry(spellId, client.player, ItemStack.EMPTY, true, 0);
        List<Component> source = new ArrayList<>();
        titleEntries.stream().filter(line -> !line.getString().isBlank()).findFirst().ifPresent(source::add);
        SpellTooltip.spellDescriptionWithDetails(spellId, client.player, ItemStack.EMPTY, 0).stream()
                .filter(line -> !line.getString().isBlank()).forEach(source::add);
        int panelWidth = Math.min(250, Math.max(150, screenWidth - x - 8));
        List<net.minecraft.util.FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : source) {
            if (!line.getString().isBlank()) wrapped.addAll(client.font.split(line, panelWidth - 16));
        }
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
