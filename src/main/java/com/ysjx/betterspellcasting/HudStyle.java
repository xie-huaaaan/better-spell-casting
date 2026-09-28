package com.ysjx.betterspellcasting;

import java.util.Locale;

public enum HudStyle {
    SINGLE,
    THREE;

    public HudStyle next() {
        return this == SINGLE ? THREE : SINGLE;
    }

    public String translationKey() {
        return "option.better_spellcasting.hud." + name().toLowerCase(Locale.ROOT);
    }
}
