# Galdur UI

Bibliothèque d'interface de Galdur Studio : thème, composants et écrans pour nos mods Minecraft.

## Versions

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Développement

```powershell
.\gradlew.bat runClient   # lance le client de développement
.\gradlew.bat build       # compile, lance les tests et produit le jar dans build/libs
.\gradlew.bat test        # tests unitaires seuls (sans démarrer Minecraft)
```

En jeu, la commande `/galdurui test` ouvre l'écran de test (environnement de développement uniquement).

## Thème

`assets/galdurui/theme.json` est la seule source de la charte : couleurs et espacements
(section `spacing`). Il est relu à chaque rechargement des ressources. Minecraft ignore F3+T
tant qu'un écran est ouvert : dans `/galdurui test`, utiliser le bouton « Recharger le thème ».
Le même fichier, lu dans le jar, sert de valeurs par défaut quand un pack de ressources fournit
un `theme.json` incomplet.

Couleurs : écrire `"#RRGGBB"` (opaque) ou `"#AARRGGBB"`. Un entier JSON est accepté ; s'il tient
sur 24 bits (≤ `0xFFFFFF`, donc sans octet alpha), il est lu comme `RRGGBB` opaque, avec un
avertissement dans le journal, plutôt que de donner une couleur invisible.

## Sprites

Les sprites 9-slice (`panel`, `button`, `button_hover`, `button_pressed`, `button_disabled`,
16×16, bordure 4) de `assets/galdurui/textures/gui/sprites/` sont en niveaux de gris : leur
couleur vient du thème et est appliquée au rendu (un pixel blanc prend exactement la couleur
du thème). Modifier les couleurs de `theme.json` ne demande donc pas de régénérer les images.
Seule la forme (coins, relief) est décrite par le générateur :

```powershell
.\gradlew.bat generateSprites
```

Le générateur (`tools/GenerateSprites.java`) est compilé dans un source set séparé : il ne
tourne pas au build et n'est pas inclus dans le jar du mod. La CI le relance et échoue si les
PNG commités ne correspondent plus à ce qu'il produit.

## Structure

- `com.galdurstudio.galdurui.api` : API publique et stable, utilisée par nos mods.
- `com.galdurstudio.galdurui.internal` : implémentation interne, peut changer à tout moment.
  `internal.render.GaldurRender` est la seule classe qui appelle `GuiGraphics`.

## Compatibilité et versions

L'API de Galdur UI n'est pas indépendante de Minecraft : ses composants héritent de classes
vanilla pour s'intégrer aux écrans et aux layouts du jeu.

| Classe Galdur UI | Hérite de / implémente (vanilla) |
|---|---|
| `api.widget.GaldurButton` | `AbstractWidget` |
| `api.widget.GaldurLabel` | `AbstractWidget` |
| `api.widget.GaldurPanel` | `Renderable` |
| `api.layout.GaldurPanelLayout` | `AbstractLayout` (donc `Layout`), `Renderable` |

Leurs méthodes de rendu reçoivent un `GuiGraphics` et celles de souris et de clavier suivent
les signatures d'`AbstractWidget`. Quand Mojang change ces classes (par exemple le rendu en
1.21.6 ou les entrées en 1.21.9), l'API de Galdur UI change avec elles : une telle montée de
version peut imposer une **version majeure** de Galdur UI, même si `GaldurRender` absorbe la
plus grande partie du travail.

**Règle pour nos mods** : n'appeler que l'API Galdur (`api.*`) pour dessiner et construire les
écrans, jamais `GuiGraphics` directement, et ne jamais dépendre de `internal.*`. Plus un mod
passe par l'API, moins il a de code à reprendre à chaque version de Minecraft.

## Décisions

- **Narration des `GaldurLabel`** : un libellé est inactif (`active = false`), comme
  `StringWidget` en vanilla ; la narration automatique de l'écran ne le lit donc pas, même si
  `updateWidgetNarration` renvoie son texte. Comportement identique à vanilla, assumé.
- **Outils de développement dans le jar** : `TestScreen` et `GaldurDevCommands` sont livrés
  dans le jar, mais la commande `/galdurui test` n'est enregistrée qu'en développement
  (`!FMLEnvironment.production`) : inactifs en production. Assumé pour l'instant.
- **Méthodes d'API non utilisées en interne** : les getters de `GaldurPanel`,
  `GaldurButton.withSprites`, `getPreferredWidth`, `isHeld`, `clearHeld`,
  `GaldurLabel.getText`, `GaldurPanelLayout.withSprite`, `getChild`, etc. ne sont pas appelés
  dans ce dépôt mais sont gardés volontairement : ils font partie de l'API offerte à nos mods.

## Licence

Galdur UI est distribué sous licence GNU LGPL v3 uniquement (`LGPL-3.0-only`).
Voir [LICENSE](LICENSE) (LGPL v3) et [COPYING](COPYING) (GPL v3, à laquelle la LGPL se réfère).
