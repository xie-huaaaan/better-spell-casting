package com.betterspellcasting;

import java.util.Locale;

public enum CastingMode {
    ORIGINAL,
    CYCLE,
    WHEEL;

    public CastingMode next() {
        CastingMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String translationKey() {
        return "option.better-spell-casting." + name().toLowerCase(Locale.ROOT);
    }

    public String descriptionKey() {
        return translationKey() + ".description";
    }
}
