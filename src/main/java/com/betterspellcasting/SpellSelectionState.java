package com.betterspellcasting;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/** Stores the selected spell shared by the radial selector, HUD, and casting route. */
public final class SpellSelectionState {
    private static List<ResourceLocation> candidates = List.of();
    private static ResourceLocation selected;

    private SpellSelectionState() { }

    public static void syncCandidates(List<ResourceLocation> values) {
        candidates = List.copyOf(values);
        // Java 25's immutable List implementation rejects null in contains.
        if (selected == null || !candidates.contains(selected)) {
            selected = candidates.isEmpty() ? null : candidates.get(0);
        }
    }

    public static boolean select(ResourceLocation id) {
        if (id == null || !candidates.contains(id) || Objects.equals(selected, id)) return false;
        selected = id;
        return true;
    }

    public static ResourceLocation selected() { return selected; }
}

