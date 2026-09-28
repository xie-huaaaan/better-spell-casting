package com.ysjx.spellcyclewheel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WheelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("spell_cycle_wheel.json");

    public WheelMode mode = WheelMode.ORIGINAL;
    public boolean shortcutCasting = true;
    public boolean reverseScroll = false;
    /** Enables the optional left-click bow/crossbow state machine in radial mode. */
    public boolean bowLeftClick = false;
    public HudStyle hudStyle = HudStyle.SINGLE;
    public int hudScale = 100;

    public static boolean exists() {
        return Files.exists(PATH);
    }

    public static WheelConfig load() {
        try {
            if (Files.exists(PATH)) {
                WheelConfig value = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), WheelConfig.class);
                if (value != null) {
                    return value.normalized();
                }
            }
        } catch (Exception ignored) {
            // Defaults are safer than disabling the client initializer when config is damaged.
        }
        WheelConfig defaults = new WheelConfig();
        defaults.save();
        return defaults;
    }

    public WheelConfig normalized() {
        if (mode == null) mode = WheelMode.ORIGINAL;
        if (hudStyle == null) hudStyle = HudStyle.SINGLE;
        hudScale = Math.max(50, Math.min(150, hudScale));
        return this;
    }

    public WheelConfig copy() {
        WheelConfig copy = new WheelConfig();
        copy.mode = mode;
        copy.shortcutCasting = shortcutCasting;
        copy.reverseScroll = reverseScroll;
        copy.bowLeftClick = bowLeftClick;
        copy.hudStyle = hudStyle;
        copy.hudScale = hudScale;
        return copy;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(normalized()) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // The next launch will retry using the in-memory defaults.
        }
    }
}
