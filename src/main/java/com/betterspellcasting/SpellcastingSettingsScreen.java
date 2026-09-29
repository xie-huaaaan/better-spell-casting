package com.betterspellcasting;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Provides Better Spellcasting's client-only HUD and input preferences. */
public final class SpellcastingSettingsScreen extends Screen {
    private final Screen parent;
    private final SpellcastingConfig draft;
    private Button shortcutButton;
    private Button bowLeftClickButton;
    private Button hudButton;
    private ScaleSlider scaleSlider;
    private int contentX;
    private int contentWidth;

    public SpellcastingSettingsScreen(Screen parent) {
        super(Component.translatable("screen.better-spell-casting.title"));
        this.parent = parent;
        this.draft = BetterSpellcastingClient.config().copy();
    }

    @Override
    protected void init() {
        contentWidth = Math.min(320, width - 24);
        contentX = (width - contentWidth) / 2;
        int y = 44;
        shortcutButton = addRenderableWidget(Button.builder(shortcutComponent(), button -> {
            draft.shortcutCasting = !draft.shortcutCasting;
            updateLabels();
        }).bounds(contentX, y, contentWidth, 20).build());
        y += 28;
        bowLeftClickButton = addRenderableWidget(Button.builder(bowLeftClickComponent(), button -> {
            draft.bowLeftClick = !draft.bowLeftClick;
            updateLabels();
        }).bounds(contentX, y, contentWidth, 20).build());
        y += 28;
        hudButton = addRenderableWidget(Button.builder(hudComponent(), button -> {
            draft.hudStyle = draft.hudStyle.next();
            updateLabels();
        }).bounds(contentX, y, contentWidth, 20).build());
        y += 28;
        scaleSlider = addRenderableWidget(new ScaleSlider(contentX, y, contentWidth, 20, draft.hudScale));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(contentX, height - 28, contentWidth, 20).build());
        updateLabels();
    }

    private void updateLabels() {
        if (shortcutButton != null) shortcutButton.setMessage(shortcutComponent());
        if (bowLeftClickButton != null) bowLeftClickButton.setMessage(bowLeftClickComponent());
        if (hudButton != null) hudButton.setMessage(hudComponent());
    }

    private Component shortcutComponent() {
        return Component.translatable("screen.better-spell-casting.shortcut",
                Component.translatable(draft.shortcutCasting ? "options.on" : "options.off"));
    }

    private Component bowLeftClickComponent() {
        return Component.translatable("screen.better-spell-casting.bow_left_click",
                Component.translatable(draft.bowLeftClick ? "options.on" : "options.off"));
    }

    private Component hudComponent() {
        return Component.translatable("screen.better-spell-casting.hud",
                Component.translatable(draft.hudStyle.translationKey()));
    }

    private void saveAndClose() {
        if (scaleSlider != null) draft.hudScale = scaleSlider.scale();
        BetterSpellcastingClient.applyConfig(draft);
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private final class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int width, int height, int scale) {
            super(x, y, width, height, Component.empty(), (scale - 50) / 100.0);
            updateMessage();
        }

        int scale() { return 50 + (int) Math.round(value * 100.0); }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.better-spell-casting.scale", scale() + "%"));
        }

        @Override
        protected void applyValue() {
            value = Math.max(0.0, Math.min(1.0, value));
        }
    }
}
