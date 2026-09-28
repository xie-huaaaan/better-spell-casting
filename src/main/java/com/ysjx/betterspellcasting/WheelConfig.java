package com.ysjx.betterspellcasting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WheelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("better_spellcasting.json");
    private static final Path LEGACY_PATH = FabricLoader.getInstance().getConfigDir().resolve("spell_cycle.json");
    private static final Path LEGACY_DLC_PATH = FabricLoader.getInstance().getConfigDir().resolve("spell_cycle_wheel.json");

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
            Path legacyPath = Files.exists(LEGACY_DLC_PATH) ? LEGACY_DLC_PATH : LEGACY_PATH;
            if (Files.exists(legacyPath)) {
                WheelConfig migrated = migrateLegacy(Files.readString(legacyPath, StandardCharsets.UTF_8));
                migrated.save();
                return migrated;
            }
        } catch (Exception ignored) {
            // Defaults are safer than disabling the client initializer when config is damaged.
        }
        WheelConfig defaults = new WheelConfig();
        defaults.save();
        return defaults;
    }

    private static WheelConfig migrateLegacy(String json) {
        WheelConfig migrated = new WheelConfig();
        try {
            JsonObject legacy = GSON.fromJson(json, JsonObject.class);
            String operationMode = legacy.has("operationMode")
                    ? legacy.get("operationMode").getAsString()
                    : legacy.has("mode") ? legacy.get("mode").getAsString() : "ORIGINAL";
            migrated.mode = switch (operationMode) {
                case "CYCLE", "HYBRID" -> WheelMode.CYCLE;
                case "WHEEL" -> WheelMode.WHEEL;
                default -> WheelMode.ORIGINAL;
            };
            migrated.shortcutCasting = legacy.has("shortcutCasting")
                    ? legacy.get("shortcutCasting").getAsBoolean()
                    : "HYBRID".equals(operationMode);
            migrated.reverseScroll = legacy.has("reverseScroll")
                    && legacy.get("reverseScroll").getAsBoolean();
            if (legacy.has("bowLeftClick")) migrated.bowLeftClick = legacy.get("bowLeftClick").getAsBoolean();
            if (legacy.has("hudScale")) migrated.hudScale = legacy.get("hudScale").getAsInt();
            if (legacy.has("hudStyle")) {
                try {
                    migrated.hudStyle = HudStyle.valueOf(legacy.get("hudStyle").getAsString());
                } catch (IllegalArgumentException ignored) {
                    migrated.hudStyle = HudStyle.SINGLE;
                }
            }
        } catch (Exception ignored) {
            // A malformed legacy file must not prevent the new client from starting.
        }
        return migrated.normalized();
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
