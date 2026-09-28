package com.ysjx.spellcyclewheel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class WheelSettingsScreen extends Screen {
    private final Screen parent;
    private final WheelConfig draft;
    private ButtonWidget modeButton;
    private ButtonWidget shortcutButton;
    private ButtonWidget reverseButton;
    private ButtonWidget bowLeftClickButton;
    private ButtonWidget hudButton;
    private ScaleSlider scaleSlider;
    private int contentX;
    private int contentWidth;
    private int descriptionY;

    public WheelSettingsScreen(Screen parent) {
        super(Text.translatable("screen.spell_cycle.title"));
        this.parent = parent;
        this.draft = SpellCycleWheelClient.config().copy();
    }

    @Override
    protected void init() {
        contentWidth = Math.min(320, width - 24);
        contentX = (width - contentWidth) / 2;
        int y = 44;
        modeButton = addDrawableChild(ButtonWidget.builder(modeText(), button -> {
            draft.mode = draft.mode.next();
            clearAndInit();
        }).dimensions(contentX, y, contentWidth, 20).build());

        descriptionY = y + 28;
        int descriptionHeight = textRenderer.wrapLines(modeDescription(), contentWidth).size() * 10;
        y = descriptionY + descriptionHeight + 8;

        if (draft.mode != WheelMode.ORIGINAL) {
            shortcutButton = addDrawableChild(ButtonWidget.builder(shortcutText(), button -> {
                draft.shortcutCasting = !draft.shortcutCasting;
                updateLabels();
            }).dimensions(contentX, y, contentWidth, 20).build());
            y += 28;
        }
        if (draft.mode == WheelMode.CYCLE) {
            reverseButton = addDrawableChild(ButtonWidget.builder(reverseText(), button -> {
                draft.reverseScroll = !draft.reverseScroll;
                updateLabels();
            }).dimensions(contentX, y, contentWidth, 20).build());
            y += 28;
        }
        if (draft.mode == WheelMode.WHEEL) {
            bowLeftClickButton = addDrawableChild(ButtonWidget.builder(bowLeftClickText(), button -> {
                draft.bowLeftClick = !draft.bowLeftClick;
                updateLabels();
            }).dimensions(contentX, y, contentWidth, 20).build());
            y += 28;
        }
        hudButton = addDrawableChild(ButtonWidget.builder(hudText(), button -> {
            draft.hudStyle = draft.hudStyle.next();
            updateLabels();
        }).dimensions(contentX, y, contentWidth, 20).build());
        y += 28;
        scaleSlider = addDrawableChild(new ScaleSlider(contentX, y, contentWidth, 20, draft.hudScale));

        int actionWidth = (contentWidth - 8) / 2;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.spell_cycle_wheel.save"), button -> saveAndClose())
                .dimensions(contentX, height - 28, actionWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
                .dimensions(contentX + contentWidth - actionWidth, height - 28, actionWidth, 20).build());
        updateLabels();
    }

    private void updateLabels() {
        if (modeButton != null) modeButton.setMessage(modeText());
        if (shortcutButton != null) shortcutButton.setMessage(shortcutText());
        if (reverseButton != null) reverseButton.setMessage(reverseText());
        if (bowLeftClickButton != null) bowLeftClickButton.setMessage(bowLeftClickText());
        if (hudButton != null) hudButton.setMessage(hudText());
    }

    private Text modeText() {
        return Text.translatable("screen.spell_cycle_wheel.mode", Text.translatable(draft.mode.translationKey()));
    }

    private Text modeDescription() {
        return Text.translatable(draft.mode.descriptionKey());
    }

    private Text shortcutText() {
        return Text.translatable("screen.spell_cycle_wheel.shortcut", Text.translatable(draft.shortcutCasting ? "options.on" : "options.off"));
    }

    private Text reverseText() {
        return Text.translatable("screen.spell_cycle_wheel.reverse_scroll", Text.translatable(draft.reverseScroll ? "options.on" : "options.off"));
    }

    private Text bowLeftClickText() {
        return Text.translatable("screen.spell_cycle_wheel.bow_left_click",
                Text.translatable(draft.bowLeftClick ? "options.on" : "options.off"));
    }

    private Text hudText() {
        return Text.translatable("screen.spell_cycle_wheel.hud", Text.translatable(draft.hudStyle.translationKey()));
    }

    private void saveAndClose() {
        draft.hudScale = scaleSlider.scale();
        SpellCycleWheelClient.applyConfig(draft);
        close();
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        int y = descriptionY;
        for (var line : textRenderer.wrapLines(modeDescription(), contentWidth)) {
            context.drawTextWithShadow(textRenderer, line, contentX, y, 0xAAAAAA);
            y += 10;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private final class ScaleSlider extends SliderWidget {
        ScaleSlider(int x, int y, int width, int height, int scale) {
            super(x, y, width, height, Text.empty(), (scale - 50) / 100.0);
            updateMessage();
        }

        int scale() {
            return 50 + (int) Math.round(value * 100.0);
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable("screen.spell_cycle_wheel.scale", scale() + "%"));
        }

        @Override
        protected void applyValue() {
            value = Math.max(0.0, Math.min(1.0, value));
        }
    }
}
