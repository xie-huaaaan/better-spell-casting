package com.ysjx.spellcyclewheel;

import com.yuansujuexing.spellcycle.SelectionState;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.List;

/** Accesses spell-cycle's single selection state without adding another persisted selection. */
public final class SpellCycleSelectionBridge {
    private static final Field SELECTION_FIELD = findSelectionField();

    private SpellCycleSelectionBridge() {
    }

    public static void syncCandidates(List<Identifier> candidates) {
        SelectionState<Identifier> selection = selection();
        if (selection != null) {
            selection.update(candidates);
        }
    }

    public static boolean select(Identifier id) {
        SelectionState<Identifier> selection = selection();
        return selection != null && selection.select(id);
    }

    public static Identifier selected() {
        SelectionState<Identifier> selection = selection();
        return selection == null ? null : selection.selected();
    }

    private static SelectionState<Identifier> selection() {
        if (SELECTION_FIELD == null) return null;
        try {
            @SuppressWarnings("unchecked")
            SelectionState<Identifier> selection = (SelectionState<Identifier>) SELECTION_FIELD.get(null);
            return selection;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static Field findSelectionField() {
        try {
            Field field = com.yuansujuexing.spellcycle.SpellCycleClient.class.getDeclaredField("SELECTION");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
