# Galdur UI

*[Version française](README.fr.md)*

Galdur UI changes the look of Minecraft's menus. Players install it to restyle their game; modders use it for the menus of their mod or server.

[Documentation](https://docs.galdurstudio.com) · [Wiki](https://wiki.galdurstudio.com) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/galdur-ui) · [Website](https://galdurstudio.com/en/mods/galdur-ui) · [Issues](https://github.com/Galdur-Studio/galdur-ui/issues)

## Compatibility

| Minecraft | Loaders | Java |
|---|---|---|
| 1.20.1 | Forge 47.x, Fabric (Loader 0.20+, Fabric API) | 17 |
| 1.21.1 | NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API), Forge 52.x | 21 |
| 26.3 | NeoForge 26.3.x, Fabric (Loader 0.20+, Fabric API) | 25 |

## What it does

- Windows described in code: titles, text, buttons, lists, search, tabs. No coordinates.
- Slot menus, like a chest: the menu on the server, the screen on the client.
- Standalone widgets, including a grid that shows thousands of items.
- Restyle your existing screens, with one line (`GaldurBridge`).
- A theme that resource packs and players can change, gradients included.

Everything is explained in the [documentation](https://docs.galdurstudio.com): installation, first window, slot menus, theme, API reference.

## Quick start

```gradle
repositories {
    maven { url = uri("../galdur-ui/repo") }   // after ./gradlew publish in galdur-ui
}
dependencies {
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:1.1.0+1.21.1"   // or -forge; Fabric: modImplementation
}
```

```java
GaldurWindow window = GaldurLib.createWindow();
window.addTitle(Component.literal("Storage"), true);
window.addButton(Component.literal("Close"), window::close, true);
window.open();
```

Also declare the `galdurui` dependency (`[1.0,2.0)`) in your mod's metadata: see [Installation](https://docs.galdurstudio.com/galdur-ui/installation).

## Development

All the code is in `src/`. [Stonecutter](https://stonecutter.kikugie.dev/) builds one jar per Minecraft version and loader, each in `versions/<minecraft>-<loader>`. Every dependency version is in `stonecutter.properties.toml`.

```powershell
.\gradlew.bat :1.21.1-neoforge:runClient   # dev client (library + test mod); same with -fabric, -forge
.\gradlew.bat build                        # every jar, tests included (versions/*/build/libs)
.\gradlew.bat :1.21.1-neoforge:test        # unit tests only
```

Gradle runs on Java 25, downloaded if missing. The rest (layout, test mod, screenshots, rules) is in [Contributing](https://docs.galdurstudio.com/galdur-ui/contributing) and [CONTRIBUTING.md](CONTRIBUTING.md).

## Galdur UI versions

[Semantic Versioning](https://semver.org/): `api.*` has been stable since 1.0.0 and won't break before 2.0, hence `[1.0,2.0)`. `internal.*` can change in any release. Changes are in [CHANGELOG.md](CHANGELOG.md).

## License

Copyright © 2026 Galdur Studio. Galdur UI is under the GNU LGPL v3 only (`LGPL-3.0-only`): see [LICENSE](LICENSE) and [COPYING](COPYING). Your mod can depend on it regardless of its license.

The Galdur logo and name belong to Galdur Studio: the LGPL covers the code, not the branding.
