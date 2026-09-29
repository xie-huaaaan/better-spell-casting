package com.betterspellcasting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.spell_engine.client.SpellEngineClient;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.client.util.SpellRender;
import net.spell_engine.internals.casting.SpellCaster;

import java.util.ArrayList;
import java.util.List;

/** Client HUD and radial selector renderer. */
public final class SpellcastingRenderer {
    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 22;
    private static final int ICON_SIZE = 16;

    private SpellcastingRenderer() {
    }

    public static void render(GuiGraphics context, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isSpectator() || client.screen != null) {
            return;
        }

        List<SpellHotbar.Slot> slots = SpellcastingController.castSlots();
        if (!slots.isEmpty()) {
            renderHud(context, client, slots, tickDelta);
        }
        if (BetterSpellcastingClient.isWheelOpen()) {
            renderWheel(context, client, slots);
        }
    }

    private static void renderHud(GuiGraphics context, Minecraft client,
                                  List<SpellHotbar.Slot> slots, float tickDelta) {
        SpellcastingConfig config = BetterSpellcastingClient.config();
        var hotbar = SpellEngineClient.hudConfig.value.hotbar;
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        Vec2 anchor = hotbar.origin.getPoint(width, height).add(hotbar.offset);
        float scale = config.hudScale / 100.0F;

        PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(anchor.x, anchor.y, 0);
        matrices.scale(scale, scale, 1.0F);
        if (config.hudStyle == HudStyle.SINGLE) {
            renderSingleRow(context, client, slots, tickDelta);
        } else {
            renderRows(context, client, slots, tickDelta);
        }
        matrices.popPose();
    }

    private static void renderSingleRow(GuiGraphics context, Minecraft client,
                                        List<SpellHotbar.Slot> slots, float tickDelta) {
        float rowWidth = slots.size() * SLOT_WIDTH;
        drawHotbarBackground(context, -rowWidth / 2.0F, -SLOT_HEIGHT / 2.0F, slots.size());
        for (int i = 0; i < slots.size(); i++) {
            drawSlot(context, client, slots.get(i), -rowWidth / 2.0F + i * SLOT_WIDTH + 2,
                    -SLOT_HEIGHT / 2.0F + 3, tickDelta, showShortcutLabels());
        }
    }

    private static void renderRows(GuiGraphics context, Minecraft client,
                                   List<SpellHotbar.Slot> slots, float tickDelta) {
        int rows = Math.min(3, Math.max(1, (int) Math.ceil(slots.size() / 5.0)));
        int columns = (int) Math.ceil(slots.size() / (double) rows);
        int totalHeight = rows * SLOT_HEIGHT;
        for (int row = 0; row < rows; row++) {
            int from = row * columns;
            int to = Math.min(slots.size(), from + columns);
            int count = to - from;
            float rowWidth = count * SLOT_WIDTH;
            // Spell Engine's hotbar origin is on the lower edge. Grow upward so rows never leave the screen.
            float rowY = 11.0F - totalHeight + row * SLOT_HEIGHT;
            drawHotbarBackground(context, -rowWidth / 2.0F, rowY, count);
            for (int i = 0; i < count; i++) {
                drawSlot(context, client, slots.get(from + i), -rowWidth / 2.0F + i * SLOT_WIDTH + 2,
                        rowY + 3, tickDelta, false);
            }
        }
    }

    private static boolean showShortcutLabels() {
        SpellcastingConfig config = BetterSpellcastingClient.config();
        return config.shortcutCasting;
    }

    private static void drawHotbarBackground(GuiGraphics context, float x, float y, int count) {
        if (count <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.setColor(1, 1, 1, 1);
        int ix = Math.round(x);
        int iy = Math.round(y);
        for (int i = 0; i < count; i++) {
            context.fill(RenderType.guiOverlay(), ix + i * SLOT_WIDTH, iy,
                    ix + i * SLOT_WIDTH + SLOT_WIDTH, iy + SLOT_HEIGHT, 0xAA161616);
        }
        RenderSystem.disableBlend();
    }

    private static void drawSlot(GuiGraphics context, Minecraft client, SpellHotbar.Slot slot,
                                 float x, float y, float tickDelta, boolean showKey) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        ResourceLocation icon = SpellRender.iconTexture(SpellcastingController.spellId(slot));
        context.setColor(1, 1, 1, 1);
        context.blit(icon, ix, iy, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        if (client.player != null) {
            float cooldown = ((SpellCaster.Client) client.player).getCooldownManager()
                    .getCooldownProgress(slot.spell(), tickDelta);
            if (cooldown > 0) {
                int top = iy + Mth.floor(16.0F * (1.0F - cooldown));
                context.fill(RenderType.guiOverlay(), ix, top, ix + 16, iy + 16, 0x99000000);
            }
        }
        if (SpellcastingController.spellId(slot).equals(SpellcastingController.hudSelectedSpell())) {
            context.fill(RenderType.guiOverlay(), ix - 2, iy - 2, ix + 18, iy, 0xFFFFFFFF);
            context.fill(RenderType.guiOverlay(), ix - 2, iy + 16, ix + 18, iy + 18, 0xFFFFFFFF);
            context.fill(RenderType.guiOverlay(), ix - 2, iy, ix, iy + 16, 0xFFFFFFFF);
            context.fill(RenderType.guiOverlay(), ix + 16, iy, ix + 18, iy + 16, 0xFFFFFFFF);
        }
        if (showKey && slotIndex(slot) < 9) {
            var key = shortcutKey(client, slot);
            if (key != null) {
                String label = key.getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
                if (label.length() > 4) label = label.substring(0, 4);
                context.drawCenteredString(client.font, label, ix + 8, iy - 10, 0xFFFFFF);
            }
        }
    }

    private static int slotIndex(SpellHotbar.Slot target) {
        List<SpellHotbar.Slot> slots = SpellcastingController.castSlots();
        for (int i = 0; i < slots.size(); i++) {
            if (SpellcastingController.spellId(slots.get(i)).equals(SpellcastingController.spellId(target))) return i;
        }
        return Integer.MAX_VALUE;
    }

    private static net.minecraft.client.KeyMapping shortcutKey(Minecraft client, SpellHotbar.Slot target) {
        int index = slotIndex(target);
        return SpellcastingController.shortcutKey(client, index);
    }

    private static void renderWheel(GuiGraphics context, Minecraft client,
                                    List<SpellHotbar.Slot> slots) {
        if (slots.isEmpty()) return;
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int outerRadius = Math.min(118, Math.max(92, Math.min(width, height) / 5));
        // Keep the 16px icon and its selection frame inside the annular sector.
        int iconRadius = outerRadius - 18;
        int selected = SpellcastingController.selectedIndex();
        context.fill(RenderType.guiOverlay(), 0, 0, width, height, 0x33000000);
        drawRadialSectors(context, centerX, centerY, 38, outerRadius, slots.size(), selected);
        drawCenter(context, centerX, centerY, 38);
        for (int i = 0; i < slots.size(); i++) {
            double angle = -Math.PI / 2 + Math.PI * 2 * i / slots.size();
            int x = centerX + (int) Math.round(Math.cos(angle) * iconRadius) - 8;
            int y = centerY + (int) Math.round(Math.sin(angle) * iconRadius) - 8;
            context.blit(SpellRender.iconTexture(SpellcastingController.spellId(slots.get(i))), x, y,
                    0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            if (i == selected) {
                drawSelectedIconFrame(context, x, y);
            }
        }
        if (selected >= 0 && selected < slots.size()) {
            renderDetails(context, client, SpellcastingController.spellId(slots.get(selected)), centerX + iconRadius + 30, centerY - 90, width, height);
        }
    }

    private static void drawRadialSectors(GuiGraphics context, int centerX, int centerY,
                                          double innerRadius, double outerRadius, int count, int selected) {
        if (count <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose matrix = context.pose().last();
        int subdivisions = 16;
        for (int spell = 0; spell < count; spell++) {
            int color = spell == selected ? 0xC8D9A86C : 0x8C241B16;
            int alpha = color >> 24 & 255;
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            double halfSector = Math.PI / count;
            double start = -Math.PI / 2 - halfSector + spell * Math.PI * 2 / count;
            double end = -Math.PI / 2 - halfSector + (spell + 1) * Math.PI * 2 / count;
            for (int part = 0; part < subdivisions; part++) {
                double a0 = start + (end - start) * part / subdivisions;
                double a1 = start + (end - start) * (part + 1) / subdivisions;
                vertex(buffer, matrix, centerX + Math.cos(a0) * innerRadius, centerY + Math.sin(a0) * innerRadius, red, green, blue, alpha);
                vertex(buffer, matrix, centerX + Math.cos(a1) * innerRadius, centerY + Math.sin(a1) * innerRadius, red, green, blue, alpha);
                vertex(buffer, matrix, centerX + Math.cos(a1) * outerRadius, centerY + Math.sin(a1) * outerRadius, red, green, blue, alpha);
                vertex(buffer, matrix, centerX + Math.cos(a0) * outerRadius, centerY + Math.sin(a0) * outerRadius, red, green, blue, alpha);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        drawRadialDividers(context, centerX, centerY, innerRadius, outerRadius, count);
        RenderSystem.disableBlend();
    }

    private static void drawRadialDividers(GuiGraphics context, int centerX, int centerY,
                                           double innerRadius, double outerRadius, int count) {
        final int gold = 0xFFFFD36B;
        double halfSector = Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 - halfSector + i * Math.PI * 2 / count;
            double ux = Math.cos(angle);
            double uy = Math.sin(angle);
            int steps = (int) Math.ceil(outerRadius - innerRadius);
            for (int step = 0; step <= steps; step++) {
                double radius = innerRadius + step;
                int x = (int) Math.round(centerX + ux * radius);
                int y = (int) Math.round(centerY + uy * radius);
                context.fill(RenderType.guiOverlay(), x - 1, y - 1, x + 2, y + 2, gold);
            }
        }
    }

    private static void drawCenter(GuiGraphics context, int centerX, int centerY, double radius) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose matrix = context.pose().last();
        vertex(buffer, matrix, centerX, centerY, 18, 14, 10, 220);
        for (int i = 0; i <= 32; i++) {
            double angle = Math.PI * 2 * i / 32;
            vertex(buffer, matrix, centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, 18, 14, 10, 220);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
        context.fill(RenderType.guiOverlay(), centerX - 8, centerY, centerX + 8, centerY + 1, 0xD0E7D7C7);
        context.fill(RenderType.guiOverlay(), centerX, centerY - 8, centerX + 1, centerY + 8, 0xD0E7D7C7);
    }

    private static void drawSelectedIconFrame(GuiGraphics context, int x, int y) {
        int color = 0xFFF0C978;
        context.fill(RenderType.guiOverlay(), x - 3, y - 3, x + 19, y - 1, color);
        context.fill(RenderType.guiOverlay(), x - 3, y + 17, x + 19, y + 19, color);
        context.fill(RenderType.guiOverlay(), x - 3, y - 1, x - 1, y + 17, color);
        context.fill(RenderType.guiOverlay(), x + 17, y - 1, x + 19, y + 17, color);
    }

    private static void vertex(BufferBuilder buffer, PoseStack.Pose matrix, double x, double y,
                               int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix.pose(), (float) x, (float) y, 0).setColor(red, green, blue, alpha);
    }

    private static void renderDetails(GuiGraphics context, Minecraft client, ResourceLocation spellId,
                                      int x, int y, int screenWidth, int screenHeight) {
        if (client.player == null) return;
        List<Component> titleEntries = SpellTooltip.spellEntry(spellId, client.player, ItemStack.EMPTY, true, 0);
        List<Component> lines = new ArrayList<>();
        titleEntries.stream().filter(line -> !line.getString().isBlank()).findFirst().ifPresent(lines::add);
        SpellTooltip.spellDescriptionWithDetails(spellId, client.player, ItemStack.EMPTY, 0).stream()
                .filter(line -> !line.getString().isBlank()).forEach(lines::add);
        int panelWidth = Math.min(250, screenWidth - x - 10);
        if (panelWidth < 150) {
            x = Math.max(8, Math.min(x - radiusForPanel(screenWidth), screenWidth - 158));
            panelWidth = Math.min(250, screenWidth - x - 8);
        }
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) wrapped.addAll(client.font.split(line, panelWidth - 16));
        int panelHeight = 12 + wrapped.size() * 10;
        y = Mth.clamp(y, 8, screenHeight - panelHeight - 8);
        context.fill(RenderType.guiOverlay(), x, y, x + panelWidth, y + panelHeight, 0xCC101010);
        context.fill(RenderType.guiOverlay(), x, y, x + panelWidth, y + 1, 0xFFFFFFFF);
        int lineY = y + 6;
        for (FormattedCharSequence line : wrapped) {
            context.drawString(client.font, line, x + 8, lineY, 0xFFFFFF);
            lineY += 10;
        }
    }

    private static int radiusForPanel(int screenWidth) {
        return Math.max(0, Math.min(250, screenWidth / 2 - 12));
    }

}



