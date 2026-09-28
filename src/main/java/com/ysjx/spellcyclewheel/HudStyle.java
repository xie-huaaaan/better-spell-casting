package com.ysjx.spellcyclewheel;

import java.util.Locale;

public enum HudStyle {
    SINGLE,
    THREE;

    public HudStyle next() {
        return this == SINGLE ? THREE : SINGLE;
    }

    public String translationKey() {
        return "option.spell_cycle_wheel.hud." + name().toLowerCase(Locale.ROOT);
    }
}
