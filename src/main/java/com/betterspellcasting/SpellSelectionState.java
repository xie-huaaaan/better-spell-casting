package com.betterspellcasting;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

/** Stores the selected spell shared by the radial selector, HUD, and casting route. */
public final class SpellSelectionState {
    private static List<Identifier> candidates = List.of();
    private static Identifier selected;

    private SpellSelectionState() { }

    public static void syncCandidates(List<Identifier> values) {
        candidates = List.copyOf(values);
        // Java 25's immutable List implementation rejects null in contains.
        if (selected == null || !candidates.contains(selected)) {
            selected = candidates.isEmpty() ? null : candidates.get(0);
        }
    }

    public static boolean select(Identifier id) {
        if (id == null || !candidates.contains(id) || Objects.equals(selected, id)) return false;
        selected = id;
        return true;
    }

    public static Identifier selected() { return selected; }
}
