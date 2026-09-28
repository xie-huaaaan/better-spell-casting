package com.ysjx.spellcyclewheel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.Identifier;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.client.input.WrappedKeybinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Low-volume runtime trace for diagnosing the right-click routing path. */
public final class SpellInputTrace {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpellCycleWheel/InputTrace");
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static String lastRouteState = "";
    private static String lastOwnerPollState = "";
    private static long activeCall;
    private static boolean activeCallUsesSlot;

    private SpellInputTrace() {
    }

    public static void routed(Identifier selected, SpellHotbar.Slot useSlot,
                              List<SpellHotbar.Slot> shortcutSlots, GameOptions options) {
        String shortcuts = shortcutSlots.stream()
                .map(slot -> id(slot) + "=" + key(slot, options))
                .toList().toString();
        String state = selected + "|" + id(useSlot) + "|" + shortcuts;
        if (state.equals(lastRouteState)) return;
        lastRouteState = state;
        LOGGER.info("[SCW] routed selection={} onUseKey={} shortcuts={}", selected, id(useSlot), shortcuts);
    }

    public static void rightClickSlot(SpellHotbar.Slot requested, SpellHotbar.Slot canonical) {
        activeCall = SEQUENCE.incrementAndGet();
        activeCallUsesSlot = true;
        MinecraftClient client = MinecraftClient.getInstance();
        LOGGER.info("[SCW#{}] useKey slot request={} canonical={} selected={} sameObject={} usePressed={} shortcutEnabled={}",
                activeCall, id(requested), id(canonical), SpellCycleWheelClient.selectedSpell(),
                requested == canonical, client.options.useKey.isPressed(),
                SpellCycleWheelClient.isShortcutCastingEnabled());
    }

    public static void rightClickList(List<SpellHotbar.Slot> requested, SpellHotbar.Slot canonical) {
        activeCall = SEQUENCE.incrementAndGet();
        activeCallUsesSlot = false;
        LOGGER.info("[SCW#{}] full-list use route listIsHotbar={} size={} focused={} canonical={} usePressed={}",
                activeCall, requested == SpellHotbar.INSTANCE.slots, requested.size(),
                SpellCycleWheelClient.selectedSpell(), id(canonical),
                MinecraftClient.getInstance().options.useKey.isPressed());
    }

    public static void castInputOwner(Identifier spell, WrappedKeybinding.Unwrapped binding, String source) {
        if (spell == null || binding == null) {
            LOGGER.info("[SCW] cast input owner cleared");
            lastOwnerPollState = "";
            return;
        }
        LOGGER.info("[SCW] cast input owner spell={} source={} key={} category={}", spell, source,
                binding.keyBinding().getBoundKeyLocalizedText().getString(), binding.vanillaHandle());
    }

    public static void ownerPoll(Identifier spell, WrappedKeybinding.Unwrapped binding, String source,
                                 String pass, boolean selected, GameOptions options) {
        if (binding == null) {
            String state = spell + "|" + source + "|" + pass + "|null";
            if (state.equals(lastOwnerPollState)) return;
            lastOwnerPollState = state;
            LOGGER.warn("[SCW] cast input poll has no binding spell={} source={} pass={}", spell, source, pass);
            return;
        }
        boolean pressed = binding.keyBinding().isPressed();
        String state = spell + "|" + source + "|" + pass + "|" + binding.vanillaHandle() + "|" + pressed + "|" + selected;
        if (state.equals(lastOwnerPollState)) return;
        lastOwnerPollState = state;
        LOGGER.info("[SCW] cast input poll spell={} source={} pass={} selected={} key={} category={} pressed={} usePressed={}",
                spell, source, pass, selected, binding.keyBinding().getBoundKeyLocalizedText().getString(),
                binding.vanillaHandle(), pressed, options.useKey.isPressed());
    }

    public static void handleList(List<SpellHotbar.Slot> slots) {
        if (activeCall == 0) return;
        SpellHotbar.Slot canonical = SpellHotbar.INSTANCE.structuredSlots.onUseKey();
        LOGGER.info("[SCW#{}] handle(List) slots={} firstIsOnUseKey={} canonical={}", activeCall,
                slots.stream().map(SpellInputTrace::id).toList(),
                !slots.isEmpty() && slots.get(0) == canonical, id(canonical));
    }

    public static void listResult(SpellHotbar.Handle handled) {
        if (activeCall != 0) {
            LOGGER.info("[SCW#{}] handle(List) result={}", activeCall,
                    handled == null ? "<none>" : handled.spell().id());
            if (!activeCallUsesSlot) {
                activeCall = 0;
            }
        }
    }

    public static void slotResult(SpellHotbar.Handle handled) {
        if (activeCall != 0) {
            LOGGER.info("[SCW#{}] handle(Slot) result={}", activeCall,
                    handled == null ? "<none>" : handled.spell().id());
            activeCall = 0;
            activeCallUsesSlot = false;
        }
    }

    private static Identifier id(SpellHotbar.Slot slot) {
        return slot == null || slot.spell() == null ? null : slot.spell().id();
    }

    private static String key(SpellHotbar.Slot slot, GameOptions options) {
        var key = slot.getKeyBinding(options);
        return key == null ? "<none>" : key.getBoundKeyLocalizedText().getString();
    }
}
