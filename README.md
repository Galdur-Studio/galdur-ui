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

## Structure

- `com.galdurstudio.galdurui.api` : API publique et stable, utilisée par nos mods.
- `com.galdurstudio.galdurui.internal` : implémentation interne, peut changer à tout moment.

## Licence

Galdur UI est distribué sous licence GNU LGPL v3 uniquement (`LGPL-3.0-only`).
Voir [LICENSE](LICENSE) (LGPL v3) et [COPYING](COPYING) (GPL v3, à laquelle la LGPL se réfère).
