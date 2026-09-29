package com.betterspellcasting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.client.input.Keybindings;
import net.spell_engine.client.input.WrappedKeybinding;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;

import java.util.ArrayList;
import java.util.List;

public final class SpellcastingController {
    private enum InputPass {
        NONE,
        FULL,
        SHORTCUT,
        USE
    }

    private static InputPass inputPass = InputPass.NONE;
    private static Identifier inputOwnerSpell;
    private static SpellCast.Process inputOwnerProcess;
    private static WrappedKeybinding.Unwrapped inputOwnerBinding;

    private SpellcastingController() {}

    public static List<SpellHotbar.Slot> castSlots() {
        // Spell Engine has already applied hand, container, mode, and availability rules.
        // Keeping this snapshot intact prevents the DLC from inventing a second candidate policy.
        return rawCastSlots().stream().filter(slot -> spellId(slot) != null).toList();
    }

    private static List<SpellHotbar.Slot> rawCastSlots() {
        return SpellHotbar.INSTANCE == null || SpellHotbar.INSTANCE.slots == null
                ? List.of() : SpellHotbar.INSTANCE.slots;
    }

    public static Identifier spellId(SpellHotbar.Slot slot) {
        return slot == null || slot.option() == null ? null : slot.option().id();
    }

    public static void ensureSelection() {
        List<SpellHotbar.Slot> slots = castSlots();
        if (slots.isEmpty()) {
            SpellSelectionState.syncCandidates(List.of());
            return;
        }
        SpellSelectionState.syncCandidates(slots.stream().map(SpellcastingController::spellId).toList());
        Identifier current = BetterSpellcastingClient.selectedSpell();
        for (SpellHotbar.Slot slot : slots) {
            if (spellId(slot).equals(current)) return;
        }
        SpellSelectionState.select(spellId(slots.get(0)));
    }

    public static void refresh(Minecraft client) {
        List<SpellHotbar.Slot> slots = castSlots();
        SpellSelectionState.syncCandidates(slots.stream().map(SpellcastingController::spellId).toList());
    }

    public static void confirmSelection(Minecraft client) {
        refresh(client);
        routeHotbar(client);
    }

    public static int selectedIndex() {
        List<SpellHotbar.Slot> slots = castSlots();
        Identifier selected = BetterSpellcastingClient.selectedSpell();
        for (int i = 0; i < slots.size(); i++) {
            if (spellId(slots.get(i)).equals(selected)) return i;
        }
        return slots.isEmpty() ? -1 : 0;
    }

    public static Identifier hudSelectedSpell() {
        return BetterSpellcastingClient.selectedSpell();
    }

    public static void updateSelection(Minecraft client) {
        List<SpellHotbar.Slot> slots = castSlots();
        if (slots.isEmpty()) return;
        double scaleX = client.getWindow().getGuiScaledWidth() / (double) client.getWindow().getWidth();
        double scaleY = client.getWindow().getGuiScaledHeight() / (double) client.getWindow().getHeight();
        double centerX = client.getWindow().getGuiScaledWidth() * 0.5;
        double centerY = client.getWindow().getGuiScaledHeight() * 0.5;
        double mouseX = client.mouseHandler.xpos() * scaleX;
        double mouseY = client.mouseHandler.ypos() * scaleY;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 42) return;
        double angle = Math.atan2(dx, -dy);
        if (angle < 0) angle += Math.PI * 2;
        int index = (int) Math.floor((angle + Math.PI * 2 / slots.size() / 2) / (Math.PI * 2 / slots.size())) % slots.size();
        BetterSpellcastingClient.setSelectedSpell(spellId(slots.get(index)));
    }

    public static void routeHotbar(Minecraft client) {
        if (client.player == null) {
            resetInputOwner();
            return;
        }
        List<SpellHotbar.Slot> slots = rawCastSlots();
        if (slots.isEmpty()) {
            resetInputOwner();
            return;
        }
        ensureSelection();
        Options options = client.options;
        List<WrappedKeybinding> shortcuts = Keybindings.Wrapped.all();
        List<SpellHotbar.Slot> routed = new ArrayList<>();
        List<SpellHotbar.Slot> shortcutSlots = new ArrayList<>();
        Identifier activeOwnerSpell = hasActiveCast() ? inputOwnerSpell : null;
        int castIndex = 0;
        SpellHotbar.Slot routedSelected = null;
        for (SpellHotbar.Slot slot : slots) {
            Identifier spellId = spellId(slot);
            if (spellId == null) {
                routed.add(slot);
                continue;
            }
            if (slot.castMode() == SpellCast.Mode.ITEM_USE) {
                // ITEM_USE entries belong to Spell Engine's item-use path and do not consume
                // one of its numbered spell bindings.
                routed.add(slot);
                if (spellId.equals(BetterSpellcastingClient.selectedSpell())) {
                    routedSelected = slot;
                }
                continue;
            }
            // Keep the focused spell in both Spell Engine input collections so both input paths agree.
            WrappedKeybinding shortcut = normalizedShortcut(options, shortcuts, castIndex);
            castIndex++;
            boolean selectedSlot = spellId.equals(BetterSpellcastingClient.selectedSpell());
            boolean ownerSlot = spellId.equals(activeOwnerSpell);
            boolean shortcutEnabled = BetterSpellcastingClient.isShortcutCastingEnabled() && shortcut != null;
            var binding = selectedSlot || ownerSlot
                    ? routedBinding(options, shortcutEnabled ? shortcut : null, spellId, selectedSlot)
                    : (shortcutEnabled ? shortcut : null);
            SpellHotbar.Slot routedSlot = new SpellHotbar.Slot(slot.option(), slot.itemStack(), binding, slot.modifier());
            routed.add(routedSlot);
            if (selectedSlot) {
                routedSelected = routedSlot;
            }
            if (shortcutEnabled || selectedSlot) {
                shortcutSlots.add(routedSlot);
            }
        }
        SpellHotbar.INSTANCE.slots = List.copyOf(routed);
        SpellHotbar.INSTANCE.structuredSlots = new SpellHotbar.StructuredSlots(routedSelected, List.copyOf(shortcutSlots));
    }

    private static WrappedKeybinding normalizedShortcut(Options options,
                                                         List<WrappedKeybinding> shortcuts,
                                                         int index) {
        if (index < 0 || index >= shortcuts.size()) return null;
        WrappedKeybinding shortcut = shortcuts.get(index);
        WrappedKeybinding.Unwrapped unwrapped = shortcut.get(options);
        if (unwrapped != null && unwrapped.vanillaHandle() != WrappedKeybinding.Category.USE_KEY) {
            return shortcut;
        }
        if (index < options.keyHotbarSlots.length) {
            return new WrappedKeybinding(options.keyHotbarSlots[index], WrappedKeybinding.VanillaAlternative.NONE);
        }
        return null;
    }

    /** Returns the same configured key used by routeHotbar for a candidate index. */
    public static KeyMapping shortcutKey(Minecraft client, int index) {
        if (client == null || index < 0) return null;
        List<WrappedKeybinding> bindings = Keybindings.Wrapped.all();
        if (index >= bindings.size()) return null;
        WrappedKeybinding normalized = normalizedShortcut(client.options, bindings, index);
        if (normalized == null) return null;
        WrappedKeybinding.Unwrapped unwrapped = normalized.get(client.options);
        return unwrapped == null ? null : unwrapped.keyBinding();
    }

    /**
     * The focused slot is present in both the full hotbar and shortcut lists. Its binding
     * selects the input source from the list being processed while keeping the same Slot
     * instance for the use-key path.
     */
    private static WrappedKeybinding routedBinding(Options options, WrappedKeybinding shortcut,
                                                    Identifier spellId, boolean selected) {
        return new WrappedKeybinding(options.keyUse, WrappedKeybinding.VanillaAlternative.USE_KEY) {
            @Override
            public Unwrapped get(Options currentOptions) {
                if (hasActiveCast()) {
                    if (!spellId.equals(inputOwnerSpell)) return null;
                    return inputOwnerBinding;
                }
                if (inputPass == InputPass.SHORTCUT) {
                    if (selected && castSelectedKey(currentOptions).isDown()) {
                        return selectedCastBinding(currentOptions);
                    }
                    if (shortcut != null) return shortcut.get(currentOptions);
                    return selected ? selectedCastBinding(currentOptions) : null;
                }
                if (inputPass == InputPass.USE || inputPass == InputPass.FULL) {
                    if (selected && (inputPass == InputPass.USE || castSelectedKey(currentOptions).isDown())) {
                        return selectedCastBinding(currentOptions);
                    }
                    if (inputPass == InputPass.FULL && shortcut != null) {
                        Unwrapped direct = shortcut.get(currentOptions);
                        if (direct != null && direct.keyBinding().isDown()) return direct;
                    }
                    if (selected) {
                        if (shortcut != null) {
                            Unwrapped direct = shortcut.get(currentOptions);
                            if (direct != null) return direct;
                        }
                        return selectedCastBinding(currentOptions);
                    }
                    return null;
                }
                if (shortcut != null) {
                    var direct = shortcut.get(currentOptions);
                    if (direct != null) return direct;
                }
                return selectedCastBinding(currentOptions);
            }
        };
    }

    private static KeyMapping castSelectedKey(Options options) {
        KeyMapping binding = BetterSpellcastingClient.castSelectedKey();
        return binding == null ? options.keyUse : binding;
    }

    private static WrappedKeybinding.Unwrapped selectedCastBinding(Options options) {
        KeyMapping selected = castSelectedKey(options);
        // The default custom binding is also the vanilla use key. Reusing the vanilla object keeps
        // Connector/Forge input arbitration on the path Spell Engine already handles reliably.
        if (selected.same(options.keyUse)) {
            selected = options.keyUse;
        }
        return new WrappedKeybinding.Unwrapped(selected, WrappedKeybinding.Category.USE_KEY);
    }

    private static boolean hasActiveCast() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        var progress = ((SpellCaster.Client) client.player).getSpellCastProgress();
        if (progress == null || inputOwnerProcess == null || progress.process() != inputOwnerProcess) {
            resetInputOwner();
            return false;
        }
        return true;
    }

    public static void beginHandle(List<SpellHotbar.Slot> slots) {
        if (slots == SpellHotbar.INSTANCE.slots) {
            inputPass = InputPass.FULL;
        } else if (slots == SpellHotbar.INSTANCE.structuredSlots.other()) {
            inputPass = InputPass.SHORTCUT;
        } else if (slots.size() == 1 && slots.get(0) == SpellHotbar.INSTANCE.structuredSlots.onUseKey()) {
            inputPass = InputPass.USE;
        } else {
                inputPass = InputPass.NONE;
        }
    }

    public static void endHandle() {
        inputPass = InputPass.NONE;
    }

    public static void finishHandle(SpellHotbar.Handle handled) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            resetInputOwner();
            return;
        }
        var progress = ((SpellCaster.Client) client.player).getSpellCastProgress();
        if (handled != null && progress != null
                && handled.spell().unwrapKey().map(key -> key.identifier().equals(progress.process().id())).orElse(false)) {
            inputOwnerSpell = progress.process().id();
            inputOwnerProcess = progress.process();
            inputOwnerBinding = new WrappedKeybinding.Unwrapped(handled.keyBinding(), handled.category());
        } else if (progress == null || inputOwnerProcess != null
                && inputOwnerProcess != progress.process()) {
            resetInputOwner();
        }
    }

    public static void resetInputOwner() {
        inputOwnerSpell = null;
        inputOwnerProcess = null;
        inputOwnerBinding = null;
    }

}
