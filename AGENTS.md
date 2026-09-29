# Better Spellcasting contribution notes

## Scope

- Keep this project client-side and preserve Spell Engine's candidate ordering and availability rules.
- Do not add compile-time dependencies on content or class mods such as Wizards, Paladins, Archers, Rogues, or Spell Anvil.
- The old `spell-cycle` implementation is absorbed here; new code must not depend on its classes or mixins.

## Structure

- Shared code owns configuration, selection state, and the single casting route.
- Loader and Minecraft-version code owns registration, input hooks, rendering hooks, and mappings.
- Fabric, Forge, and NeoForge metadata must declare only their own loader requirements.

## Comments

- Document a module, class, or method at its declaration when it explains its responsibility.
- Use inline comments only for the reason behind a non-obvious choice; do not narrate obvious statements.

## Git and validation

- Keep `main` buildable. Use focused branches for platform or subsystem work.
- Commit each coherent refactor or platform milestone so it can be reverted independently.
- Run the relevant Gradle build before merging a milestone; do not claim in-game validation unless it was performed.

## Regression checklist

- Add the settings entry inside the vanilla options layout container, immediately after the existing options. Do not place it by estimating screen coordinates after layout has completed.
- Left-click bow support must run during the client input phase, cancel vanilla attack and block-breaking continuation while a bow or crossbow is held, and still use the vanilla item-use and stop-using paths.
- Every loader's resources must use the namespace declared by that loader's mod metadata; otherwise all custom translation keys fall back to raw key names.
