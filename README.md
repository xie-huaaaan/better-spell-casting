# Better Spellcasting

Client-side DLC for the Forge 1.20.1 Element Awakening instance. The JAR is a Fabric-format client mod loaded through Connector; the server does not need it.

## Install

Copy `build/libs/better-spellcasting-1.0.0.jar` into the instance `mods` directory alongside Connector, Fabric API, Spell Engine, and Spell Cycle. It is an additional client mod; do not replace the original mod JARs. When updating, replace only the previous `better-spellcasting` JAR.

Open `ESC -> Options -> Controls -> Spell Cycle Settings` and choose `Radial wheel switching` to enable the new behavior. Hold `X`, point at a spell, and release `X` to select it. The `Cast selected spell` binding (right mouse by default) casts the selected spell and is listed under Spell Engine in Controls. The same settings page controls shortcut casting, the single-row/dynamic-three-row HUD layout, HUD scale, and optional left-click bow/crossbow controls.

## Build

Use Java 17:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-17'
.\gradlew.bat build
```

The remapped distributable is written to `build/libs/better-spellcasting-1.0.0.jar`.
