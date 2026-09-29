package com.betterspellcasting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpellcastingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("better-spell-casting.json");
    public boolean shortcutCasting = true;
    /** Enables the optional left-click bow/crossbow state machine. */
    public boolean bowLeftClick = false;
    public HudStyle hudStyle = HudStyle.SINGLE;
    public int hudScale = 100;

    public static SpellcastingConfig load() {
        try {
            if (Files.exists(PATH)) {
                SpellcastingConfig value = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), SpellcastingConfig.class);
                if (value != null) {
                    return value.normalized();
                }
            }
        } catch (Exception ignored) {
            // Defaults keep a damaged config from disabling the client initializer.
        }
        SpellcastingConfig defaults = new SpellcastingConfig();
        defaults.save();
        return defaults;
    }

    public SpellcastingConfig normalized() {
        if (hudStyle == null) hudStyle = HudStyle.SINGLE;
        hudScale = Math.max(50, Math.min(150, hudScale));
        return this;
    }

    public SpellcastingConfig copy() {
        SpellcastingConfig copy = new SpellcastingConfig();
        copy.shortcutCasting = shortcutCasting;
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

