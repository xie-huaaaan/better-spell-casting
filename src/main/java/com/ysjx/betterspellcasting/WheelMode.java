package com.ysjx.betterspellcasting;

import java.util.Locale;

public enum WheelMode {
    ORIGINAL,
    CYCLE,
    WHEEL;

    public WheelMode next() {
        WheelMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String translationKey() {
        return "option.better_spellcasting." + name().toLowerCase(Locale.ROOT);
    }

    public String descriptionKey() {
        return translationKey() + ".description";
    }
}
