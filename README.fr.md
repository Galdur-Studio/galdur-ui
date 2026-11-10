# Galdur UI

*[English version](README.md)*

Bibliothèque d'interface de Galdur Studio : thème, composants et écrans pour nos mods Minecraft.

[Site](https://galdurstudio.com/mods/galdur-ui) · [Documentation](https://docs.galdurstudio.com) · [Wiki](https://wiki.galdurstudio.com) · [GitHub](https://github.com/Galdur-Studio/galdur-ui) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/galdur-ui) · [Issues](https://github.com/Galdur-Studio/galdur-ui/issues)

## Versions

| Minecraft | Loaders | Java |
|---|---|---|
| 1.20.1 | Forge 47.x, Fabric (Loader 0.16+, Fabric API) | 17 |
| 1.21.1 | NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API), Forge 52.x | 21 |
| 26.3 | NeoForge 26.3.x, Fabric (Loader 0.16+, Fabric API) | 25 |

## Prise en main

### 1. Ajouter Galdur UI à son mod

Prérequis : un projet de mod pour une des versions ci-dessus, sur le loader visé (NeoForge avec
ModDevGradle, Fabric avec Loom, Forge avec ForgeGradle 7), avec le Java indiqué dans le tableau.

**Étape 1. Publier Galdur UI** (une fois, puis après chaque modification de la bibliothèque).
Dans le dossier `galdur-ui` :

```powershell
.\gradlew.bat publish
```

Cela écrit dans `galdur-ui/repo/` un artefact par loader et par version de Minecraft :
`galdurui-neoforge`, `galdurui-fabric` et `galdurui-forge` (`com.galdurstudio.galdurui:<artefact>:1.0.0+1.21.1`).
Un mod n'a besoin que de celui de son loader. Tant que Galdur Studio n'a pas de dépôt Maven, ce dossier local sert de dépôt.

**Étape 2. Ajouter la dépendance** dans le `build.gradle` du mod (le chemin est relatif au dossier
du mod ; l'adapter à l'emplacement de `galdur-ui`) :

```groovy
repositories {
    maven { url = uri("../galdur-ui/repo") }
}

dependencies {
    // NeoForge (ModDevGradle) ou Forge (ForgeGradle) :
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:1.0.0+1.21.1"   // ou galdurui-forge

    // Fabric (Loom) : dépendance de mod, remappée par Loom
    modImplementation "com.galdurstudio.galdurui:galdurui-fabric:1.0.0+1.21.1"
}
```

Ne garder que la ligne de son loader. Sur Fabric, Fabric API est aussi nécessaire dans le mod,
comme d'habitude.

**Étape 3. Déclarer la dépendance dans les métadonnées du mod**, pour que le jeu refuse de
démarrer le mod sans Galdur UI au lieu de planter plus tard (remplacer `mymod` par l'identifiant
du mod) :

| Loader | Fichier | À ajouter |
|---|---|---|
| NeoForge | `src/main/resources/META-INF/neoforge.mods.toml` | le bloc ci-dessous |
| Forge | `src/main/resources/META-INF/mods.toml` | le même bloc, avec `mandatory = true` à la place de `type = "required"` |
| Fabric | `src/main/resources/fabric.mod.json` | `"depends": { "galdurui": ">=1.0.0" }` |

```toml
[[dependencies.mymod]]
modId = "galdurui"
type = "required"
versionRange = "[1.0,2.0)"
ordering = "NONE"
side = "BOTH"
```

Galdur UI est un mod à part, qui n'est pas inclus dans le jar du mod : les joueurs installent le jar
de leur loader et de leur version de Minecraft (`galdurui-<loader>-1.0.0+1.21.1.jar`) à côté. Un build
local le met dans `versions/<minecraft>-<loader>/build/libs`.

**Étape 4. Vérifier que ça marche** : lancer `.\gradlew.bat runClient` dans le mod (la tâche a
le même nom sur les trois loaders), puis ouvrir un écran construit avec les exemples ci-dessous. Si le jeu dit que Galdur UI
manque, la dépendance de l'étape 2 n'est pas sur le classpath du lancement.

Ensuite, n'utiliser que `com.galdurstudio.galdurui.api.*`, avec le même code sur tous les loaders.
La seule différence est l'enregistrement d'un écran de menu (section 3).

### 2. Une fenêtre construite en code (client)

```java
GaldurWindow window = GaldurLib.createWindow();
window.addTitle(Component.literal("Stockage"), true);

List<String> ores = List.of("Charbon", "Fer", "Cuivre", "Or");
SearchHandle search = window.addSearch(Component.literal("Rechercher..."));
ListHandle list = window.addList(4);
list.setLines(ores.stream().<Component>map(Component::literal).toList());
search.onChange(text -> list.setLines(ores.stream()
        .filter(ore -> ore.toLowerCase().contains(text.toLowerCase()))
        .<Component>map(Component::literal).toList()));

RowHandle buttons = window.addRow(true);
buttons.addButton(Component.literal("Prendre"), () -> take(list.getSelected()));
buttons.addButton(Component.literal("Fermer"), window::close);
window.open();
```

Existent aussi : `addText`, `addButton`, `addIconButton`, `addCheckbox`, `addProgress` et
`addTabs` (chaque onglet est un conteneur avec les mêmes méthodes). Chaque `add...` renvoie une
poignée pour relire ou modifier l'élément ensuite (`setVisible`, `setEnabled`,
`setProgress`...). Détails dans la Javadoc de `api.window`.

### 3. Un menu avec des slots (client et serveur)

```java
public class CrateMenu extends GaldurMenu {
    private final Container crate;

    public CrateMenu(int id, Inventory player) {               // côté client
        this(id, player, new SimpleContainer(27));
    }

    public CrateMenu(int id, Inventory player, Container crate) { // côté serveur
        super(MY_MENUS.CRATE.get(), id);
        this.crate = crate;
        addSlotGrid(crate, 0, 9, 3, 8, 18);   // les slots du conteneur d'abord
        addPlayerInventory(player, 8, 84);    // l'inventaire du joueur en dernier
    }

    @Override
    public boolean stillValid(Player player) {
        return crate.stillValid(player);
    }
}
```

Enregistrer le type de menu comme d'habitude
(`new MenuType<>(CrateMenu::new, FeatureFlags.DEFAULT_FLAGS)`), puis son écran. C'est la seule ligne qui change selon le loader, car chacun enregistre ses écrans à sa
manière ; les classes d'aide sont dans l'artefact du loader
(`com.galdurstudio.galdurui.api.neoforge.GaldurNeoForgeScreens`, `...api.forge.GaldurForgeScreens`,
`...api.fabric.GaldurFabricScreens`). Un mod pour plusieurs loaders garde cette ligne dans son module de
loader. Côté client uniquement :

```java
// NeoForge
modEventBus.addListener(RegisterMenuScreensEvent.class,
        event -> event.register(MY_MENUS.CRATE.get(), GaldurNeoForgeScreens.factory()));

// Forge
modEventBus.addListener((FMLClientSetupEvent event) ->
        GaldurForgeScreens.register(event, MY_MENUS.CRATE.get()));

// Fabric (ClientModInitializer)
GaldurFabricScreens.register(MY_MENUS.CRATE);
```

Shift-clic, glisser-déposer, panneau du thème et cadres des slots marchent sans autre code.
`GaldurItemGrid` ajoute une grille défilante de milliers d'objets sans vrais slots (terminaux
de stockage) ; voir `TestTerminalScreen` dans le mod de test NeoForge.

### 4. Restyler les écrans qu'un mod a déjà

`GaldurBridge` dessine avec le thème Galdur les boutons, boutons cycliques, champs de texte, cases à
cocher et curseurs vanilla des écrans existants, sans toucher à leur code. Minecraft dessine
toujours leur texte et gère les clics et la saisie ; les widgets propres à un mod gardent leur
apparence. À appeler une fois côté client, au démarrage :

```java
GaldurBridge.restyle(MachineScreen.class);                   // un écran et ses sous-classes
GaldurBridge.restylePackage("com.example.mymod");            // tous les écrans du mod
GaldurBridge.restyle(CrateScreen.class, Background.REPLACE); // aussi le panneau et les slots
```

Pour un écran avec des slots, `Background.KEEP` (par défaut) garde sa texture. `Background.REPLACE`
dessine à la place le panneau et les cadres de slots Galdur, et met les libellés aux couleurs du
thème, mais ce que l'écran dessinait dans son fond (flèches, jauges) disparaît. Pour le garder, rester
sur `KEEP` et appeler `GaldurBridge.drawContainerBackground(graphics, this)` dans son `renderBg`
(`extractBackground` depuis la 26.1) à la place de la texture, puis le dessiner par-dessus.

### Règles

- N'utiliser que `com.galdurstudio.galdurui.api.*` ; `internal.*` peut changer à chaque
  version.
- Tout sauf `api.menu` est client uniquement : le garder hors du code qui tourne sur un serveur
  dédié.
- Relire les couleurs et espacements du thème à chaque image ou à chaque `init()`, jamais dans
  un champ `static final` : un pack de ressources peut les changer.

## Développement

Tout le code est dans `src/`, et [Stonecutter](https://stonecutter.kikugie.dev/) en tire un jar par
version de Minecraft et par loader. Chaque build est un projet Gradle dans `versions/<minecraft>-<loader>`
(par exemple `1.21.1-neoforge`).

| Où | Quoi |
|---|---|
| `src/main/java/.../galdurui/api`, `internal` | le code commun à tous les loaders : il n'utilise que Minecraft vanilla |
| `src/main/java/.../galdurui/<loader>`, `api/<loader>` | ce qui demande l'API d'un loader : point d'entrée, réglages, enregistrement des écrans. Un jar ne reçoit que les packages de son loader |
| `src/main/templates/<loader>` | `neoforge.mods.toml` et `mods.toml` |
| `src/testmod` | le mod de test ; `testmod/<loader>` pour ce qu'un seul loader a |
| `stonecutter.properties.toml` | les versions de Minecraft, des loaders et de chaque dépendance |
| `build.<loader>.gradle.kts` | le build de chaque loader ; ce qu'ils partagent est dans `buildSrc` |
| `build.legacyforge.gradle.kts` | Forge 1.20.1, construit avec ModDevGradle Legacy : le jar est réobfusqué en noms SRG |
| `src/main/java/.../internal/mixin` | les mixins du bridge (`GaldurBridge`) |

```powershell
.\gradlew.bat :1.21.1-neoforge:runClient   # client de développement (bibliothèque + mod de test) ; idem avec -fabric ou -forge
.\gradlew.bat build                        # compile, lance les tests et produit tous les jars (versions/*/build/libs)
.\gradlew.bat :1.21.1-neoforge:test        # tests unitaires seuls (sans démarrer Minecraft)
```

Gradle tourne sous Java 25, que demande Stonecutter, et le télécharge s'il manque
(`gradle/gradle-daemon-jvm.properties`). Chaque version de Minecraft compile avec son propre Java : 21 pour la 1.21.1.

### Code qui change d'une version de Minecraft à l'autre

`src/` est écrit pour une version à la fois, la version *active* (`stonecutter.gradle.kts`). Quand une
ligne change d'une version à l'autre, elle va dans un commentaire Stonecutter :

```java
//? if >=1.21.6 {
graphics.pose().pushMatrix();
//?} else {
/*graphics.pose().pushPose();
*///?}
```

Stonecutter met en commentaire les lignes qui ne sont pas pour la version active. Pour travailler sur une
autre version, lancer la tâche Gradle `Set active project to <version>` (groupe `stonecutter`) ou passer par
le plugin IntelliJ. Avant de commiter, lancer `Reset active project` : la CI échoue si `src/` n'est pas sur
`1.21.1-neoforge`. Garder ces commentaires dans `internal` autant que possible, pour que `api` reste le
même sur toutes les versions.

Les simples renommages de Mojang (`ResourceLocation` en `Identifier`, `GuiGraphics` en
`GuiGraphicsExtractor`, `render` en `extractRenderState`...) n'ont pas besoin de commentaire :
Stonecutter les remplace dans tout le code (`replacements` dans `stonecutter.gradle.kts`). Les appels
qui ont changé autrement sont regroupés dans quelques classes : `GaldurRender` pour le dessin,
`GuiSprites` pour les sprites avant 26.1, `internal.client.Client` pour les écrans et la boucle du jeu,
`GaldurInput` pour les touches et les boutons, et `GaldurWidget` pour les événements d'entrée des
widgets.

### Ajouter une version de Minecraft

1. Dans `stonecutter.properties.toml`, copier les tables de la version la plus proche et mettre à jour les versions.
2. Dans `settings.gradle.kts`, ajouter une ligne `mc("<version>", <loaders>)`.
3. Passer sur la nouvelle version, puis corriger ce qui ne compile plus avec des commentaires `//?`.
4. Lancer `.\gradlew.bat build`, puis la visite de captures sur Fabric et NeoForge (voir Mod de test).

Le workflow de publication publie tous les jars construits, sans liste à mettre à jour.

### Mod de test

Les écrans de démo, les blocs de test et les outils de debug sont dans un source set séparé,
`src/testmod`, sous la forme d'un second mod (`galduruitest`). Les lancements de développement le chargent à
côté de Galdur UI ; il n'est jamais dans le jar de la bibliothèque. Les blocs de test et la plupart des
commandes sont réservés à NeoForge (`testmod/neoforge`) ; Fabric n'a que `/galdurui test` et
`/galdurui window` (`testmod/fabric`). Forge n'en a pas : utiliser le client NeoForge ou Fabric pour voir les
widgets.

Pour vérifier une version sans cliquer, lancer la visite de captures. Depuis l'écran titre, elle ouvre
chaque onglet de l'écran de démo et la fenêtre de démo, range les captures dans `run/screenshots/` puis
quitte le jeu. Depuis la 26.1, les objets n'existent que dans un monde chargé, donc la visite crée
d'abord le monde plat en créatif `galduruitest`. Sur NeoForge, elle ouvre aussi le terminal de test
(`/galduruidev terminal`) :

```powershell
.\gradlew.bat :1.20.1-fabric:runClient -Pgalduruitest.screenshots
```

| Commande | Effet |
|---|---|
| `/galdurui test` | écran de démo de tous les widgets |
| `/galdurui window` | démo de l'API déclarative `GaldurWindow` |
| `/galdurui debug` | active ou coupe les contours de debug des layouts |
| `/galdurui fps` | active ou coupe un compteur de FPS visible sur les écrans (F3 y est masqué) |
| `/galduruidev chest` | donne un coffre de test (bloc posable, aussi dans l'onglet créatif « Blocs fonctionnels ») qui s'ouvre dans un `GaldurContainerScreen` |
| `/galduruidev terminal` | ouvre un terminal de test : une grille de tous les objets du jeu au-dessus de l'inventaire |

Le mod de test NeoForge ajoute aussi un pack de ressources de thème bleu (Options → Packs de
ressources). Il ne remplace qu'une partie de `theme.json`, pour vérifier qu'un pack peut changer
le thème et que les clés absentes reprennent les valeurs par défaut.

## Style Galdur sur tous les menus

Ce réglage donne le style Galdur à tous les menus du jeu, vanilla et autres mods compris. Il est
désactivé par défaut. Les joueurs l'activent dans les réglages de Galdur UI ; un pack de mods peut
mettre `restyleAll = true` dans `config/galdurui-client.toml` (`galdurui-client.json` sur Fabric).

Les boutons, champs de texte, cases à cocher et curseurs changent partout. Les coffres, boîtes de
Shulker, distributeurs et entonnoirs vanilla prennent aussi le panneau Galdur ; les autres menus avec
des slots gardent leur texture, pour que les flèches des fours ou les jauges d'un mod restent
visibles.

Si un menu s'affiche mal, ajouter son nom de classe ou son package à `excludedScreens`, même si un
mod le restyle lui-même. Le nom de classe est dans le journal du jeu, sur la ligne
`Galdur look given to`. On peut aussi la modifier dans les réglages de Galdur UI (noms séparés
par des virgules).

## Thème

`assets/galdurui/theme.json` est la seule source de la charte : couleurs et espacements
(section `spacing`). Il est relu à chaque rechargement des ressources. Minecraft ignore F3+T
tant qu'un écran est ouvert : dans `/galdurui test`, utiliser le bouton « Reload theme ». Le même
fichier, lu dans le jar, sert de valeurs par défaut quand un pack de ressources fournit un
`theme.json` incomplet.

Couleurs : écrire `"#RRGGBB"` (opaque) ou `"#AARRGGBB"`. Un entier JSON est accepté ; s'il tient
sur 24 bits (≤ `0xFFFFFF`, donc sans octet alpha), il est lu comme `RRGGBB` opaque, avec un
avertissement dans le journal, plutôt que de donner une couleur invisible.

### Éditeur de thème

Le bouton « Modifier le thème… » des réglages de Galdur UI sert à changer le thème en jeu. En plus
de la palette, chaque élément a sa propre couleur (bouton, champ, case à cocher, curseur, panneau,
slot, liste, onglet, barre de progression...), qui reprend une couleur de la palette tant qu'on ne la
change pas. Dans l'éditeur, chaque couleur peut être un dégradé de quatre couleurs au plus, vertical ou horizontal.
Tous les écrans Galdur UI suivent tout de suite. « Terminé » enregistre dans
`config/galdurui-theme.json`, qui passe avant les packs de ressources ; « Annuler » ou Échap remet le
thème d'avant.

« Thème Galdur » efface les couleurs du joueur : on retrouve le thème du pack de ressources, ou celui
par défaut. Le bouton est aussi dans l'écran de réglages, qui garde toujours le thème Galdur par défaut
et reste lisible quelles que soient les couleurs du joueur. L'ancien thème est gardé dans `galdurui-theme.backup.json`, et
« Partager… » > « Thème précédent » le remet.

« Partager… » copie le thème, avec un nom et un auteur, pour le coller dans un message ; il est aussi
enregistré dans `config/galdurui-themes/`. « Coller un thème » retrouve le thème dans le texte copié,
même s'il y a d'autres mots autour. On peut aussi déposer un fichier de thème sur la fenêtre. Un thème
partagé ne contient que ce que son auteur a changé, au format de `theme.json` :

```json
{ "name": "Océan", "accent": "#2F6FB5", "panel": ["#1E2A38", "#2A2420"],
  "button": { "gradient": ["#2F6FB5", "#24578F"], "direction": "horizontal" } }
```

### Où le style s'applique

« Où utiliser le style… » dans les réglages active ou coupe chaque partie du style Galdur : boutons,
champs de texte, cases à cocher, curseurs, panneau des inventaires, libellés des inventaires, et les
couleurs du joueur. Chaque réglage se fait pour tout le jeu, les inventaires vanilla, les menus
vanilla, un mod ou un seul écran (seuls les écrans ouverts depuis le lancement sont listés). Sur
« Auto », un réglage suit le niveau au-dessus ; sinon c'est le plus précis qui compte. Les choix sont enregistrés dans `config/galdurui-style.json`.

Un mod peut verrouiller un réglage sur ses écrans avec `GaldurBridge.lock(...)`, et
`GaldurBridge.lockTheme(...)` garde ses couleurs quel que soit le thème du joueur. Un pack de mods
verrouille des valeurs dans la section `"locked"` de `config/galdurui-style.json`, et ses verrous
passent avant ceux des mods. `{"*": {"player_theme": false}}` garde le thème du pack pour tout le
monde et désactive l'éditeur de thème. Les réglages verrouillés sont grisés.

## Sprites

Les sprites 9-slice de `assets/galdurui/textures/gui/sprites/` sont en niveaux de gris : leur
couleur vient du thème et est appliquée au rendu (un pixel blanc prend exactement la couleur du
thème). Modifier les couleurs de `theme.json` ne demande donc pas de régénérer les images ; seule
la forme (coins, relief) est décrite par le générateur. Minecraft 1.21.1 n'étire pas les bords
et le centre d'un 9-slice, il les répète par tuiles (sprite − 2 × bordure). `GaldurRender` envoie
toutes les tuiles d'un sprite en un seul appel de dessin, mais on garde quand même des sprites
grands pour limiter le nombre de tuiles. Bords et centre doivent rester uniformes, sinon la répétition se
voit.

Minecraft 1.20.1 n'a pas d'atlas de sprites pour les menus : Galdur UI y lit lui-même le PNG et son
`.png.mcmeta` (`internal.render.GuiSprites`). Les fichiers sont les mêmes sur toutes les versions, donc
un pack de ressources les remplace de la même façon.

| Sprite | Taille, bordure | Relief | Utilisé par (teinte) |
|---|---|---|---|
| `panel` | 256, 4 | bombé | `GaldurPanelLayout` (`background`) |
| `button`, `button_hover`, `button_pressed`, `button_disabled` | 64, 4 | bombé (`pressed` en creux) | `GaldurButton`, `GaldurIconButton` (accent selon l'état, `surface` désactivé) |
| `field` | 64, 4 | creux | champ de recherche, cadre de liste, case à cocher (`surface`), rail de barre de progression (`background`) |
| `slot` | 64, 4 | creux | `GaldurItemSlot`, `GaldurItemGrid` (`background`) |
| `tab`, `tab_selected` | 64, 4 | bombé, ouvert en bas | `GaldurTabs` (`surface`, `accent` pour l'onglet actif) |
| `scrollbar_track`, `scrollbar_thumb` | 32, 2 | creux / bombé | barres de défilement de `GaldurScrollList` et `GaldurItemGrid` (`background`, `text_muted`) |

Les sprites aux mêmes pixels (`button`/`button_hover`/`button_disabled`, `field`/`slot`,
`tab`/`tab_selected`) restent séparés pour qu'un pack de ressources puisse redessiner chacun.
Pour les régénérer :

```powershell
.\gradlew.bat :tools:generateSprites
```

Le générateur (`tools/src/main/java/GenerateSprites.java`) est un projet Gradle à part : il ne
tourne pas au build et n'est pas inclus dans le jar du mod. La CI le relance et échoue si les
PNG commités ne correspondent plus à ce qu'il produit.

## Animations

Survol des boutons et des onglets en fondu, case qui se coche en grandissant, fenêtres et écrans
de menu qui montent légèrement à l'ouverture, contenu d'onglet qui glisse depuis son côté,
listes qui défilent en douceur. Durées courtes (90 à 180 ms) et courbes de `api.anim.GaldurEasing`.
Le joueur peut tout couper : Mods → Galdur UI → Configurer → Animations
(`config/galdurui-client.toml` sur NeoForge et Forge, `config/galdurui-client.json` sur Fabric, où l'écran demande Mod Menu). Un mod qui anime ses propres widgets utilise
`api.anim.AnimatedValue`, qui respecte ce réglage.

## Narration

Chaque widget se lit au narrateur de Minecraft (Ctrl+B), dans la langue du joueur : textes vanilla
quand ils existent (« Bouton », « Case à cocher », « Onglet 1 sur 3 sélectionné »...), sinon clés
`narration.galdurui.*` de `assets/galdurui/lang` (anglais et français). Un widget désactivé
ajoute « désactivé » ; une case ou une grille lit l'objet et sa quantité complète ; un écran de
menu lit l'objet du slot survolé ; une fenêtre lit son premier titre à l'ouverture. Le code est
dans `internal.narration.GaldurNarration`.

## Performances

Mesuré avec Spark sur le terminal de test (grille de tous les objets au-dessus de
l'inventaire) : environ 120 FPS, contre 94 pour l'inventaire créatif vanilla sur la même
machine. Les gains viennent surtout de `GaldurRender` : un appel de dessin par sprite 9-slice,
le fond des cases d'une grille en un seul appel, les objets dessinés en deux passes d'éclairage
au lieu d'un vidage par objet, et les quantités regroupées en un seul envoi.

## Structure

- `com.galdurstudio.galdurui.api` : API publique et stable, utilisée par nos mods. `api.menu` est
  le seul package chargeable sur un serveur dédié ; tous les autres sont client.
- `com.galdurstudio.galdurui.internal` : implémentation interne, peut changer à tout moment.
  `internal.render.GaldurRender` est la seule classe qui appelle `GuiGraphics`. Aucune signature
  publique d'`api` n'expose un type `internal`.

## Compatibilité et versions

L'API de Galdur UI n'est pas indépendante de Minecraft : ses composants héritent de classes
vanilla pour s'intégrer aux écrans et aux layouts du jeu.

| Classe Galdur UI | Hérite de / implémente (vanilla) |
|---|---|
| `api.widget.GaldurWidget` | `AbstractWidget` |
| `api.widget.GaldurButton` | `GaldurWidget` |
| `api.widget.GaldurLabel` | `GaldurWidget` |
| `api.widget.GaldurCheckbox` | `GaldurWidget` |
| `api.widget.GaldurScrollList` | `GaldurWidget` |
| `api.widget.GaldurSearchField` | `GaldurWidget` (contient un `EditBox`) |
| `api.widget.GaldurItemSlot` | `GaldurWidget` |
| `api.widget.GaldurTabs` | `GaldurWidget` |
| `api.widget.GaldurProgressBar` | `GaldurWidget` |
| `api.widget.GaldurIconButton` | `GaldurButton` |
| `api.widget.GaldurItemGrid` | `GaldurWidget` |
| `api.screen.GaldurContainerScreen` | `AbstractContainerScreen` |
| `api.menu.GaldurMenu` | `AbstractContainerMenu` (côté logique, chargeable sur serveur) |
| `api.layout.GaldurPanelLayout` | `AbstractLayout` (donc `Layout`), `Renderable` |
| `api.layout.GaldurStack` | `GridLayout` |

Leurs méthodes de rendu reçoivent un `GuiGraphics` (`GuiGraphicsExtractor` depuis la 26.1). La 1.21.9
a remplacé les méthodes de souris et de clavier des widgets par des événements d'entrée, et
`GaldurWidget` les ramène aux anciennes méthodes. Quand Mojang change ces classes (par exemple le rendu en
1.21.6 ou les entrées en 1.21.9), l'API de Galdur UI change avec elles : une telle montée de
version peut imposer une version majeure de Galdur UI, même si `GaldurRender` absorbe la
plus grande partie du travail.

Dans nos mods, on n'appelle que l'API Galdur (`api.*`) pour dessiner et construire les écrans,
jamais `GuiGraphics` directement, et on ne dépend jamais de `internal.*`. Pour les lignes et les
colonnes, on prend `GaldurStack` au lieu de `LinearLayout`, qui ne marche pas pareil en 1.20.1. Plus un
mod passe par l'API, moins il a de code à reprendre à chaque version de Minecraft.

## Versions de Galdur UI

Galdur UI suit le [versionnage sémantique](https://semver.org/lang/fr/) : `MAJEUR.MINEUR.CORRECTIF`.

- CORRECTIF : corrections de bugs, aucune modification de l'API.
- MINEUR : nouveautés ; le code écrit pour une mineure précédente de la même majeure compile
  et fonctionne toujours.
- MAJEUR : changement incompatible de `api.*`, y compris imposé par une mise à jour de
  Minecraft (voir plus haut).

À partir de la 1.0.0, `api.*` est stable : dépendre de `[1.0,2.0)`. Avant la 1.0.0, une MINEURE
pouvait encore casser l'API. `internal.*` n'est pas concerné. Les changements sont listés dans
[CHANGELOG.md](CHANGELOG.md) (en anglais).

## Intégrations JEI et EMI

Optionnelles, dans `internal.compat` : compilées contre l'API de JEI et d'EMI (`compileOnly`),
absentes du jar et jamais chargées si le mod correspondant n'est pas installé. En développement,
les deux sont lancés (versions dans `stonecutter.properties.toml`). EMI n'est pas encore sorti pour
la 26.3, donc les jars 26.3 n'ont pas l'intégration EMI.

- La liste d'objets évite le panneau d'un `GaldurContainerScreen` et ses `getExclusionZones()`.
- Survoler un objet d'une `GaldurItemGrid` puis appuyer sur R ou U montre ses recettes. Ça ne
  marche que dans un écran de menu (`GaldurContainerScreen`), parce que JEI et EMI ne sont pas
  actifs sur les écrans simples comme `/galdurui test`. Pour tester : `/galduruidev terminal`.

## Décisions

- Un `GaldurLabel` est inactif (`active = false`), comme `StringWidget` en vanilla ; la
  narration automatique de l'écran ne le lit donc pas, même si `updateWidgetNarration` renvoie
  son texte. On a gardé exprès le comportement de vanilla.
- Les écrans de démo, les blocs et les commandes de test sont dans le mod de test
  `galduruitest` (`src/testmod`), chargé seulement par les lancements de développement,
  et n'arrivent donc jamais dans le jar.
- Certaines méthodes de l'API ne sont appelées nulle part dans ce dépôt (`GaldurButton.withSprites`,
  `getPreferredWidth`, `isHeld`, `clearHeld`, `GaldurLabel.getText`,
  `GaldurPanelLayout.withSprite`, `getChild`, etc.). On les garde exprès parce qu'elles font
  partie de l'API offerte à nos mods.
- Les sprites restent séparés même quand leurs pixels sont identiques. `button`, `button_hover` et
  `button_disabled` ont aujourd'hui les mêmes pixels, comme `field` et `slot`, ou `tab` et
  `tab_selected` (seule la teinte du thème les distingue). Ils restent séparés pour qu'un pack de
  ressources puisse redessiner chaque état indépendamment.

## Licence

Copyright © 2026 Galdur Studio.

Galdur UI est distribué sous licence GNU LGPL v3 uniquement (`LGPL-3.0-only`). Voir
[LICENSE](LICENSE) (LGPL v3) et [COPYING](COPYING) (GPL v3, à laquelle la LGPL se réfère).

Le logo Galdur et le nom « Galdur » appartiennent à Galdur Studio : la LGPL couvre le code, pas l'identité visuelle.
