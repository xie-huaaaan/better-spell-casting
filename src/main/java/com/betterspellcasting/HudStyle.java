package com.betterspellcasting;

import java.util.Locale;

public enum HudStyle {
    SINGLE,
    THREE;

    public HudStyle next() {
        return this == SINGLE ? THREE : SINGLE;
    }

    public String translationKey() {
        return "option.better-spell-casting.hud." + name().toLowerCase(Locale.ROOT);
    }
}
