# Galdur UI

*[English version](README.md)*

Galdur UI change le style des menus de Minecraft. Les joueurs l'installent pour donner un nouveau look à leur jeu ; les moddeurs s'en servent pour les menus de leur mod ou de leur serveur.

[Documentation](https://docs.galdurstudio.com) · [Wiki](https://wiki.galdurstudio.com) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/galdur-ui) · [Site](https://galdurstudio.com/mods/galdur-ui) · [Issues](https://github.com/Galdur-Studio/galdur-ui/issues)

## Compatibilité

| Minecraft | Loaders | Java |
|---|---|---|
| 1.20.1 | Forge 47.x, Fabric (Loader 0.20+, Fabric API) | 17 |
| 1.21.1 | NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API), Forge 52.x | 21 |
| 26.3 | NeoForge 26.3.x, Fabric (Loader 0.20+, Fabric API) | 25 |

## Ce qu'elle fait

- Fenêtres décrites en code : titres, textes, boutons, listes, recherche, onglets. Pas de coordonnées.
- Menus à cases, comme un coffre : le menu côté serveur, l’écran côté client.
- Widgets seuls, dont une grille qui affiche des milliers d'objets.
- Tes écrans existants au style Galdur, en une ligne (`GaldurBridge`).
- Un thème que les packs de ressources et les joueurs peuvent changer, dégradés compris.

Tout est expliqué dans la [documentation](https://docs.galdurstudio.com) : installation, première fenêtre, menus à cases, thème, référence API.

## Démarrage rapide

```gradle
repositories {
    maven { url = uri("../galdur-ui/repo") }   // après ./gradlew publish dans galdur-ui
}
dependencies {
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:1.1.0+1.21.1"   // ou -forge ; Fabric : modImplementation
}
```

```java
GaldurWindow window = GaldurLib.createWindow();
window.addTitle(Component.literal("Storage"), true);
window.addButton(Component.literal("Close"), window::close, true);
window.open();
```

Déclare aussi la dépendance `galdurui` (`[1.0,2.0)`) dans les métadonnées de ton mod : voir [Installation](https://docs.galdurstudio.com/fr/galdur-ui/installation).

## Développement

Tout le code est dans `src/`. [Stonecutter](https://stonecutter.kikugie.dev/) en tire un jar par version de Minecraft et par loader, chacun dans `versions/<minecraft>-<loader>`. Les versions de chaque dépendance sont dans `stonecutter.properties.toml`.

```powershell
.\gradlew.bat :1.21.1-neoforge:runClient   # client de dev (bibliothèque + mod de test) ; idem -fabric, -forge
.\gradlew.bat build                        # tous les jars, tests compris (versions/*/build/libs)
.\gradlew.bat :1.21.1-neoforge:test        # tests unitaires seuls
```

Gradle tourne sous Java 25, téléchargé s'il manque. Le reste (organisation, mod de test, captures, règles) est dans [Contribuer](https://docs.galdurstudio.com/fr/galdur-ui/contributing) et [CONTRIBUTING.md](CONTRIBUTING.md).

## Versions de Galdur UI

[Versionnage sémantique](https://semver.org/lang/fr/) : `api.*` est stable depuis la 1.0.0 et ne cassera pas avant la 2.0, d’où la plage `[1.0,2.0)`. `internal.*` peut changer à chaque version. Les changements sont dans [CHANGELOG.md](CHANGELOG.md).

## Licence

Copyright © 2026 Galdur Studio. Galdur UI est sous GNU LGPL v3 uniquement (`LGPL-3.0-only`) : voir [LICENSE](LICENSE) et [COPYING](COPYING). Ton mod peut en dépendre quelle que soit sa licence.

Le logo et le nom « Galdur » appartiennent à Galdur Studio : la LGPL couvre le code, pas l'identité visuelle.
