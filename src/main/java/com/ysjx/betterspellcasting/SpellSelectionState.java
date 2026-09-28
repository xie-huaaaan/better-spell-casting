package com.ysjx.betterspellcasting;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

/** Stores one selection shared by the cycle selector, radial selector, HUD, and casting route. */
public final class SpellSelectionState {
    private static List<Identifier> candidates = List.of();
    private static Identifier selected;

    private SpellSelectionState() { }

    public static void syncCandidates(List<Identifier> values) {
        candidates = List.copyOf(values);
        if (!candidates.contains(selected)) selected = candidates.isEmpty() ? null : candidates.get(0);
    }

    public static boolean select(Identifier id) {
        if (id == null || !candidates.contains(id) || Objects.equals(selected, id)) return false;
        selected = id;
        return true;
    }

    public static boolean step(int delta) {
        if (candidates.isEmpty()) return false;
        int index = candidates.indexOf(selected);
        if (index < 0) index = 0;
        int next = Math.floorMod(index + delta, candidates.size());
        return select(candidates.get(next));
    }

    public static Identifier selected() { return selected; }
    public static int index() { return selected == null ? -1 : candidates.indexOf(selected); }
    public static List<Identifier> candidates() { return candidates; }
}
