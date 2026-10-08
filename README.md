# Galdur UI

Bibliothèque d'interface de Galdur Studio : thème, composants et écrans pour nos mods Minecraft.

## Versions

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Développement

```powershell
.\gradlew.bat runClient   # lance le client de développement
.\gradlew.bat build       # compile et produit le jar dans build/libs
```

En jeu, la commande `/galdurui test` ouvre l'écran de test (environnement de développement uniquement).

## Sprites

Les sprites 9-slice (`panel`, `button`, `button_hover`, `button_pressed`, `button_disabled`,
16×16, bordure 4) de `assets/galdurui/textures/gui/sprites/` sont générés à partir des
couleurs de `assets/galdurui/theme.json`. Pour retoucher la charte, on modifie `theme.json`
puis on régénère, plutôt que d'éditer les images à la main :

```powershell
.\gradlew.bat generateSprites
```

Le générateur (`tools/GenerateSprites.java`) est compilé dans un source set séparé : il ne
tourne pas au build et n'est pas inclus dans le jar du mod.

## Structure

- `com.galdurstudio.galdurui.api` : API publique et stable, utilisée par nos mods.
- `com.galdurstudio.galdurui.internal` : implémentation interne, peut changer à tout moment.

## Licence

Galdur UI est distribué sous licence GNU LGPL v3 uniquement (`LGPL-3.0-only`).
Voir [LICENSE](LICENSE) (LGPL v3) et [COPYING](COPYING) (GPL v3, à laquelle la LGPL se réfère).
