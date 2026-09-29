# Better Spellcasting

Better Spellcasting is a client-side overhaul of the Spell Engine casting workflow. It absorbs the useful spell selection and casting controls from `spell-cycle` and adds a radial selector, a clearer spell HUD, configurable casting keys, and optional left-click bow and crossbow controls.

The mod does not add spells, change spell data, or require installation on a server. It follows Spell Engine's own candidate ordering and availability rules, so content mods remain responsible for their own spells and dependencies.

简体中文：[README.zh-CN.md](README.zh-CN.md)

## Features

- Hold the **Select spell** key (default: `X`) to open a radial selector.
- Point the mouse at a spell and release the key to keep it selected. A center dead zone keeps the current selection.
- Use **Cast selected spell** (default: right mouse button) to cast the selected spell. This binding can be changed in Minecraft's Controls screen under **Better Spellcasting**.
- Optionally enable shortcut casting. The configured number keys cast their corresponding spells directly without changing the radial selection.
- Choose a single-row or dynamic three-row spell bar and adjust its size from 50% to 150%.
- While the radial selector is open, show the focused spell's name and Spell Engine description/details.
- Optionally move bow and crossbow use to the left mouse button. Vanilla item-use and release paths are retained, while attack and block-breaking input is suppressed during ranged-weapon use.
- The spell bar, radial selector, shortcut keys, and cast key use one shared selection and casting route.

The mod does not introduce a second spell filter. The candidate list is the list supplied by Spell Engine, including its hand, container, item-use, and availability rules.

## Installation

Choose the JAR matching both your Minecraft version and loader:

| Minecraft | Loader | Branch | File name |
| --- | --- | --- | --- |
| 1.20.1 | Fabric | `1.20.1-fabric` | `better-spell-casting-fabric-1.0.1+1.20.1.jar` |
| 1.20.1 | Forge | `1.20.1-forge` | `better-spell-casting-forge-1.0.1+1.20.1.jar` |
| 1.21.1 | Fabric | `1.21.1-fabric` | `better-spell-casting-fabric-1.0.1+1.21.1.jar` |
| 1.21.1 | NeoForge | `1.21.1-neoforge` | `better-spell-casting-neoforge-1.0.1+1.21.1.jar` |
| 26.1 | Fabric | `26.1-fabric` | `better-spell-casting-fabric-1.0.1+26.1.jar` |
| 26.1 | NeoForge | `26.1-neoforge` | `better-spell-casting-neoforge-1.0.1+26.1.2.jar` |

1. Copy the matching JAR into the instance's `mods` directory.
2. Install the matching **Spell Engine** version and all dependencies required by your loader and content mods.
3. Remove the old `spell-cycle` JAR. Better Spellcasting includes its casting workflow; loading both mods can create duplicate key handling, HUDs, or mixin conflicts.
4. Start the client. The server does not need Better Spellcasting.

Open **Options → Controls** to change **Select spell** and **Cast selected spell**. Open **Options → Spellcasting Settings** to change shortcut casting, ranged-weapon input, spell-bar layout, and spell-bar size.

## Building

Each supported target is maintained on its own branch. Check out the target branch before building. The build needs a Spell Engine JAR for that exact Minecraft and loader version; pass its path explicitly in a public checkout.

For the 26.1 NeoForge branch:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat clean build `
  -Pspell_engine_jar='D:\path\to\spell_engine-neoforge-1.10.5+26.1.2.jar'
```

The output is written to `build/libs/` using the file names listed above. Older targets use the Java version required by their loader toolchain; 1.20.1 Forge uses Java 17, 1.21.1 NeoForge uses Java 21, and 26.1 uses Java 25.

## Branches and project scope

The repository branches correspond to the six supported targets listed in the installation table. Loader-specific input, GUI, rendering, and metadata code stays on the target branch; the behavior and configuration model are kept aligned where the platform APIs allow it.

This project is intended to remain a client-only enhancement. It does not add a server protocol, register gameplay content, or take compile-time dependencies on Wizards, Paladins & Priests, Archers, Rogues & Warriors, or other content mods.

