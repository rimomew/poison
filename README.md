# Client Marker — Minecraft Java 26.3

Minimal Fabric client-only mod that toggles a local UUID-backed marker on the living entity under the crosshair.

## Toolchain

- Minecraft: 26.3
- Fabric Loader: 0.19.5
- Fabric API: 0.161.0+26.3
- Fabric Loom: 1.18-SNAPSHOT
- Gradle: 9.7.1
- Java: 25+

## Build

Use JDK 25 and run:

```text
./gradlew build
```

On Windows:

```text
gradlew.bat build
```

The finished jar is under `build/libs/`.

## Controls

`P` is the default key for **Mark Target** and can be changed in Minecraft's Controls screen.

The mod only keeps UUIDs in its marked set. It never sends a marking packet, changes an entity, or applies a status effect.
