package com.ysjx.spellcyclewheel;

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
        return "option.spell_cycle_wheel." + name().toLowerCase(Locale.ROOT);
    }

    public String descriptionKey() {
        return translationKey() + ".description";
    }
}
