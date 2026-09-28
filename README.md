# Better Spellcasting

Client-side improvements for the Spell Engine casting workflow. The 1.20.1 Fabric build is a Fabric-format client mod and can be loaded in a Forge instance through Connector; the server does not need it.

## Install

Copy `build/libs/better-spell-casting-1.0.1.jar` into the instance `mods` directory alongside Spell Engine and the loader's client dependencies. This version absorbs the old `spell-cycle` controls, so remove the old `spell-cycle` JAR instead of loading both mods together. The server does not need this mod.

Open `ESC -> Options -> Controls -> Spellcasting Settings` and choose `Radial wheel switching` to enable the radial selector. Hold `X`, point at a spell, and release `X` to select it. The `Cast selected spell` binding (right mouse by default) casts the selected spell and is listed under Better Spellcasting in Controls. The same settings page controls shortcut casting, the single-row/three-row spell-bar layout, HUD scale, scroll direction, and optional left-click bow/crossbow controls.

## Build

Use Java 25 for the current Loom toolchain (the compiled Minecraft code still targets Java 17):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat build -Pspell_engine_jar='D:\path\to\spell_engine-fabric-1.10.7+1.20.1.jar'
```

The local Element Awakening installation is used automatically when that default path exists. A public checkout should pass the Spell Engine JAR path explicitly; TinyConfig is extracted from that JAR during compilation.

The remapped distributable is written to `build/libs/better-spell-casting-1.0.1.jar`.
