# Galdur UI

*[English version](README.md)*

Bibliothèque d'interface de Galdur Studio : thème, composants et écrans pour nos mods Minecraft.

## Versions

- Minecraft 1.21.1
- NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API) et Forge 52.x
- Java 21

## Prise en main

### 1. Ajouter la dépendance

Tant que Galdur Studio n'a pas de dépôt Maven, Galdur UI est publié dans un dossier `repo/` à
côté de ses sources. Dans `galdur-ui`, lancer :

```powershell
.\gradlew.bat publish     # écrit repo/com/galdurstudio/galdurui/galdurui-<loader>/<version>/
```

Un artefact par loader : `galdurui-neoforge`, `galdurui-fabric` et `galdurui-forge`.

Puis, dans le `build.gradle` du mod (chemin relatif au dossier du mod) :

```groovy
repositories {
    maven { url = uri("../galdur-ui/repo") }
}

dependencies {
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:0.1.0"   // ou galdurui-forge
    // Fabric (Loom) : modImplementation "com.galdurstudio.galdurui:galdurui-fabric:0.1.0"
}
```

Et déclarer la dépendance dans les métadonnées du mod. NeoForge : `src/main/resources/META-INF/neoforge.mods.toml`
(remplacer `mymod` par l'identifiant du mod) ; Forge : le même bloc dans `mods.toml`, avec `mandatory = true` à la
place de `type = "required"` ; Fabric : `"depends": { "galdurui": ">=0.1.0" }` dans `fabric.mod.json`.

```toml
[[dependencies.mymod]]
modId = "galdurui"
type = "required"
versionRange = "[0.1,0.2)"
ordering = "NONE"
side = "BOTH"
```

Galdur UI est un mod à part : les joueurs l'installent à côté du vôtre (il n'est pas inclus dans
votre jar).

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
(`new MenuType<>(CrateMenu::new, FeatureFlags.DEFAULT_FLAGS)`), puis son écran, côté client sur
le bus du mod :

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
de stockage) ; voir `TestTerminalScreen` dans le mod de test.

### Règles

- N'utiliser que `com.galdurstudio.galdurui.api.*` ; `internal.*` peut changer à chaque
  version.
- Tout sauf `api.menu` est client uniquement : le garder hors du code qui tourne sur un serveur
  dédié.
- Relire les couleurs et espacements du thème à chaque image ou à chaque `init()`, jamais dans
  un champ `static final` : un pack de ressources peut les changer.

## Développement

Le projet a un module par loader autour d'un module partagé :

| Module | Contenu |
|---|---|
| `common` | tout le code et les assets (`api`, `internal`, sprites, thème, lang), compilés contre Minecraft vanilla seul : une API NeoForge, Forge ou Fabric n'y compile pas. Contient les tests unitaires et `generateSprites` |
| `neoforge` | point d'entrée NeoForge, réglages `ModConfigSpec`, `neoforge.mods.toml`, le mod de test |
| `fabric` | point d'entrée Fabric, réglages JSON, écran Mod Menu, `fabric.mod.json` |
| `forge` | point d'entrée Forge, réglages `ForgeConfigSpec`, `mods.toml` |

Chaque module de loader compile les sources de `common` dans son propre jar : un mod dépend d'un seul
artefact `galdurui-<loader>`. Le code commun à tous les loaders va dans `common` ; un module de loader ne
contient que ce qui demande son API.

```powershell
.\gradlew.bat :neoforge:runClient   # client de développement (bibliothèque + mod de test) ; idem avec :fabric: ou :forge:
.\gradlew.bat build                 # compile, lance les tests et produit un jar par loader (<module>/build/libs)
.\gradlew.bat :common:test          # tests unitaires seuls (sans démarrer Minecraft)
```

Gradle doit tourner sous Java 21 (`gradle/gradle-daemon-jvm.properties` le demande et le télécharge s'il manque).

### Mod de test

Les écrans de démo, les blocs de test et les outils de debug vivent dans un source set séparé,
`neoforge/src/testmod`, sous la forme d'un second mod (`galduruitest`). Les lancements de développement du
module NeoForge le chargent à côté de Galdur UI ; il n'est **jamais** dans le jar de la bibliothèque. Le module
Fabric a aussi un petit mod de test (`fabric/src/testmod` : `/galdurui test` et `/galdurui window` seulement), bâti
sur les écrans de démo de `testmod-shared/`. Forge n'en a pas : utiliser le client NeoForge ou Fabric pour voir les
widgets.

| Commande | Effet |
|---|---|
| `/galdurui test` | écran de démo de tous les widgets |
| `/galdurui window` | démo de l'API déclarative `GaldurWindow` |
| `/galdurui debug` | active ou coupe les contours de debug des layouts |
| `/galdurui fps` | active ou coupe un compteur de FPS visible sur les écrans (F3 y est masqué) |
| `/galduruidev chest` | donne un coffre de test (bloc posable, aussi dans l'onglet créatif « Blocs fonctionnels ») qui s'ouvre dans un `GaldurContainerScreen` |
| `/galduruidev terminal` | ouvre un terminal de test : une grille de tous les objets du jeu au-dessus de l'inventaire |

Le mod de test ajoute aussi un pack de ressources **thème bleu** (Options → Packs de
ressources). Il ne remplace qu'une partie de `theme.json`, pour vérifier qu'un pack peut changer
le thème et que les clés absentes reprennent les valeurs par défaut.

## Thème

`assets/galdurui/theme.json` est la seule source de la charte : couleurs et espacements
(section `spacing`). Il est relu à chaque rechargement des ressources. Minecraft ignore F3+T
tant qu'un écran est ouvert : dans `/galdurui test`, utiliser le bouton « Reload theme ». Le même
fichier, lu dans le jar, sert de valeurs par défaut quand un pack de ressources fournit un
`theme.json` incomplet.

Couleurs : écrire `"#RRGGBB"` (opaque) ou `"#AARRGGBB"`. Un entier JSON est accepté ; s'il tient
sur 24 bits (≤ `0xFFFFFF`, donc sans octet alpha), il est lu comme `RRGGBB` opaque, avec un
avertissement dans le journal, plutôt que de donner une couleur invisible.

## Sprites

Les sprites 9-slice de `assets/galdurui/textures/gui/sprites/` sont en niveaux de gris : leur
couleur vient du thème et est appliquée au rendu (un pixel blanc prend exactement la couleur du
thème). Modifier les couleurs de `theme.json` ne demande donc pas de régénérer les images ; seule
la forme (coins, relief) est décrite par le générateur. Minecraft 1.21.1 n'étire pas les bords
et le centre d'un 9-slice, il les répète par tuiles (sprite − 2 × bordure). `GaldurRender` envoie
toutes les tuiles d'un sprite en un seul appel de dessin, mais des sprites grands limitent
quand même le nombre de tuiles. Bords et centre doivent rester uniformes, sinon la répétition se
voit.

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
.\gradlew.bat generateSprites
```

Le générateur (`common/tools/GenerateSprites.java`) est compilé dans un source set séparé : il ne
tourne pas au build et n'est pas inclus dans le jar du mod. La CI le relance et échoue si les
PNG commités ne correspondent plus à ce qu'il produit.

## Animations

Survol des boutons et des onglets en fondu, case qui se coche en grandissant, fenêtres et écrans
de menu qui montent légèrement à l'ouverture, contenu d'onglet qui glisse depuis son côté,
défilement fluide des listes. Durées courtes (90 à 180 ms) et courbes de `api.anim.GaldurEasing`.
Le joueur peut tout couper : Mods → Galdur UI → Configurer → Animations
(`config/galdurui-client.toml` sur NeoForge et Forge, `config/galdurui-client.json` sur Fabric, où l'écran demande Mod Menu). Un mod qui anime ses propres widgets utilise
`api.anim.AnimatedValue`, qui respecte ce réglage.

## Police des titres

Les titres (`addTitle`, titre des écrans de menu) utilisent Fredoka SemiBold, police
`galdurui:title` (`assets/galdurui/font/title.json`). `api.text.GaldurFonts.title(texte)`
l'applique à n'importe quel texte. Les caractères absents viennent de la police de Minecraft ; un
pack de ressources peut remplacer la police.

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
| `api.widget.GaldurButton` | `AbstractWidget` |
| `api.widget.GaldurLabel` | `AbstractWidget` |
| `api.widget.GaldurCheckbox` | `AbstractWidget` |
| `api.widget.GaldurScrollList` | `AbstractWidget` |
| `api.widget.GaldurSearchField` | `AbstractWidget` (contient un `EditBox`) |
| `api.widget.GaldurItemSlot` | `AbstractWidget` |
| `api.widget.GaldurTabs` | `AbstractWidget` |
| `api.widget.GaldurProgressBar` | `AbstractWidget` |
| `api.widget.GaldurIconButton` | `GaldurButton` (donc `AbstractWidget`) |
| `api.widget.GaldurItemGrid` | `AbstractWidget` |
| `api.screen.GaldurContainerScreen` | `AbstractContainerScreen` |
| `api.menu.GaldurMenu` | `AbstractContainerMenu` (côté logique, chargeable sur serveur) |
| `api.layout.GaldurPanelLayout` | `AbstractLayout` (donc `Layout`), `Renderable` |

Leurs méthodes de rendu reçoivent un `GuiGraphics` et celles de souris et de clavier suivent
les signatures d'`AbstractWidget`. Quand Mojang change ces classes (par exemple le rendu en
1.21.6 ou les entrées en 1.21.9), l'API de Galdur UI change avec elles : une telle montée de
version peut imposer une **version majeure** de Galdur UI, même si `GaldurRender` absorbe la
plus grande partie du travail.

**Règle pour nos mods** : n'appeler que l'API Galdur (`api.*`) pour dessiner et construire les
écrans, jamais `GuiGraphics` directement, et ne jamais dépendre de `internal.*`. Plus un mod
passe par l'API, moins il a de code à reprendre à chaque version de Minecraft.

## Versions de Galdur UI

Galdur UI suit le [versionnage sémantique](https://semver.org/lang/fr/) : `MAJEUR.MINEUR.CORRECTIF`.

- **CORRECTIF** : corrections de bugs, aucune modification de l'API.
- **MINEUR** : nouveautés ; le code écrit pour une mineure précédente de la même majeure compile
  et fonctionne toujours.
- **MAJEUR** : changement incompatible de `api.*`, y compris imposé par une mise à jour de
  Minecraft (voir plus haut).

Avant la 1.0.0, une MINEURE peut encore casser l'API : dépendre de `[0.1,0.2)` plutôt que de
`[0.1,)`. `internal.*` n'est pas concerné. Les changements sont listés dans
[CHANGELOG.md](CHANGELOG.md) (en anglais).

## Intégrations JEI et EMI

Optionnelles, dans `internal.compat` : compilées contre l'API de JEI et d'EMI (`compileOnly`),
absentes du jar et jamais chargées si le mod correspondant n'est pas installé. En développement,
les deux sont lancés (versions dans `gradle.properties`).

- La liste d'objets évite le panneau d'un `GaldurContainerScreen` et ses `getExclusionZones()`.
- Survoler un objet d'une `GaldurItemGrid` puis appuyer sur R ou U montre ses recettes,
  **dans un écran de menu** (`GaldurContainerScreen`) : JEI et EMI ne sont pas actifs sur les
  écrans simples comme `/galdurui test`. Pour tester : `/galduruidev terminal`.

## Décisions

- **Narration des `GaldurLabel`** : un libellé est inactif (`active = false`), comme
  `StringWidget` en vanilla ; la narration automatique de l'écran ne le lit donc pas, même si
  `updateWidgetNarration` renvoie son texte. Comportement identique à vanilla, assumé.
- **Outils de développement hors du jar** : écrans de démo, blocs et commandes de test sont dans
  le mod de test `galduruitest` (`neoforge/src/testmod`), chargé seulement par les lancements de
  développement.
- **Méthodes d'API non utilisées en interne** : `GaldurButton.withSprites`, `getPreferredWidth`,
  `isHeld`, `clearHeld`, `GaldurLabel.getText`, `GaldurPanelLayout.withSprite`, `getChild`, etc.
  ne sont pas appelés dans ce dépôt mais sont gardés volontairement : ils font partie de l'API
  offerte à nos mods.
- **Sprites distincts, même aux pixels identiques** : `panel`, `button`, `button_hover` et
  `button_disabled` ont aujourd'hui les mêmes pixels, comme `field` et `slot`, ou `tab` et
  `tab_selected` (seule la teinte du thème les distingue). Ils restent séparés pour qu'un pack de
  ressources puisse redessiner chaque état indépendamment.

## Licence

Galdur UI est distribué sous licence GNU LGPL v3 uniquement (`LGPL-3.0-only`). Voir
[LICENSE](LICENSE) (LGPL v3) et [COPYING](COPYING) (GPL v3, à laquelle la LGPL se réfère).

La police des titres, Fredoka (Milena Brandão, Hafontia), est sous licence SIL Open Font
License 1.1 : voir `common/src/main/resources/assets/galdurui/font/fredoka_ofl.txt`. Le fichier inclus
est une instance SemiBold de la police variable, réduite aux alphabets latins.
