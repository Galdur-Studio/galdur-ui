# Galdur UI

*[Version française](README.fr.md)*

Galdur Studio's GUI library: theme, components and screens for our Minecraft mods.

## Versions

- Minecraft 1.21.1
- NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API) and Forge 52.x
- Java 21

## Getting started

### 1. Add the dependency

Until Galdur Studio has a Maven repository, Galdur UI is published to a `repo/` folder next to
its sources. In `galdur-ui`, run:

```powershell
.\gradlew.bat publish     # writes repo/com/galdurstudio/galdurui/galdurui-<loader>/<version>/
```

One artifact per loader: `galdurui-neoforge`, `galdurui-fabric` and `galdurui-forge`.

Then, in your mod's `build.gradle` (the path is relative to your mod's folder):

```groovy
repositories {
    maven { url = uri("../galdur-ui/repo") }
}

dependencies {
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:0.1.0"   // or galdurui-forge
    // Fabric (Loom): modImplementation "com.galdurstudio.galdurui:galdurui-fabric:0.1.0"
}
```

And declare the dependency in your mod's metadata. NeoForge: `src/main/resources/META-INF/neoforge.mods.toml`
(replace `mymod` with your mod id); Forge: the same block in `mods.toml`, with `mandatory = true` instead of
`type = "required"`; Fabric: `"depends": { "galdurui": ">=0.1.0" }` in `fabric.mod.json`.

```toml
[[dependencies.mymod]]
modId = "galdurui"
type = "required"
versionRange = "[0.1,0.2)"
ordering = "NONE"
side = "BOTH"
```

Galdur UI is a separate mod: players install it next to yours (it is not bundled in your jar).

### 2. A window, built in code (client)

```java
GaldurWindow window = GaldurLib.createWindow();
window.addTitle(Component.literal("Storage"), true);

List<String> ores = List.of("Coal", "Iron", "Copper", "Gold");
SearchHandle search = window.addSearch(Component.literal("Search..."));
ListHandle list = window.addList(4);
list.setLines(ores.stream().<Component>map(Component::literal).toList());
search.onChange(text -> list.setLines(ores.stream()
        .filter(ore -> ore.toLowerCase().contains(text.toLowerCase()))
        .<Component>map(Component::literal).toList()));

RowHandle buttons = window.addRow(true);
buttons.addButton(Component.literal("Take"), () -> take(list.getSelected()));
buttons.addButton(Component.literal("Close"), window::close);
window.open();
```

Also available: `addText`, `addButton`, `addIconButton`, `addCheckbox`, `addProgress` and
`addTabs` (each tab is a container with the same methods). Every `add...` returns a handle to
read or change the element later (`setVisible`, `setEnabled`, `setProgress`...). Javadoc of
`api.window` for the details.

### 3. A menu with slots (client and server)

```java
public class CrateMenu extends GaldurMenu {
    private final Container crate;

    public CrateMenu(int id, Inventory player) {               // client side
        this(id, player, new SimpleContainer(27));
    }

    public CrateMenu(int id, Inventory player, Container crate) { // server side
        super(MY_MENUS.CRATE.get(), id);
        this.crate = crate;
        addSlotGrid(crate, 0, 9, 3, 8, 18);   // container slots first
        addPlayerInventory(player, 8, 84);    // player inventory last
    }

    @Override
    public boolean stillValid(Player player) {
        return crate.stillValid(player);
    }
}
```

Register the menu type as usual (`new MenuType<>(CrateMenu::new, FeatureFlags.DEFAULT_FLAGS)`),
then its screen, client side on the mod bus:

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

Shift-click, drag and drop, the theme panel and the slot frames work without more code.
`GaldurItemGrid` adds a scrolling grid of thousands of items without real slots (storage
terminals); see `TestTerminalScreen` in the test mod.

### Rules

- Only use `com.galdurstudio.galdurui.api.*`; `internal.*` can change in any version.
- Everything except `api.menu` is client-only: keep it out of code that runs on a dedicated
  server.
- Read theme colours and spacings every frame or every `init()`, never in a `static final`
  field: a resource pack can change them.

## Development

The project has one module per loader around a shared one:

| Module | Content |
|---|---|
| `common` | all the code and assets (`api`, `internal`, sprites, theme, lang), compiled against vanilla Minecraft only: a NeoForge, Forge or Fabric API does not compile there. Holds the unit tests and `generateSprites` |
| `neoforge` | NeoForge entry point, `ModConfigSpec` settings, `neoforge.mods.toml`, the test mod |
| `fabric` | Fabric entry point, JSON settings, Mod Menu screen, `fabric.mod.json` |
| `forge` | Forge entry point, `ForgeConfigSpec` settings, `mods.toml` |

Each loader module compiles the sources of `common` into its own jar: a mod depends on a single
`galdurui-<loader>` artifact. Code shared by all loaders goes in `common`; a loader module only holds what
needs its API.

```powershell
.\gradlew.bat :neoforge:runClient   # development client (library + test mod); same with :fabric: or :forge:
.\gradlew.bat build                 # compiles, runs the tests and builds one jar per loader (<module>/build/libs)
.\gradlew.bat :common:test          # unit tests only (without starting Minecraft)
```

Gradle must run on Java 21 (`gradle/gradle-daemon-jvm.properties` asks for it and downloads it when missing).

### Test mod

Demo screens, test blocks and debug tools live in a separate source set, `neoforge/src/testmod`, as a
second mod (`galduruitest`). Development runs of the NeoForge module load it next to Galdur UI; it is
**never** in the library jar. Fabric and Forge have no test mod yet.

| Command | What it does |
|---|---|
| `/galdurui test` | demo screen of every widget |
| `/galdurui window` | demo of the declarative `GaldurWindow` API |
| `/galdurui debug` | toggles the layout debug outlines |
| `/galdurui fps` | toggles an FPS counter that stays visible on screens (F3 is hidden there) |
| `/galduruidev chest` | gives a test chest (placeable block, also in the "Functional Blocks" creative tab) that opens in a `GaldurContainerScreen` |
| `/galduruidev terminal` | opens a test terminal: a grid of every item in the game above the inventory |

The test mod also adds a **blue theme** resource pack (Options → Resource Packs). It overrides
only part of `theme.json`, to check that a pack can change the theme and that missing keys fall
back to the defaults.

## Theme

`assets/galdurui/theme.json` is the single source of the style guide: colours and spacing
(`spacing` section). It is read again on every resource reload. Minecraft ignores F3+T while a
screen is open: in `/galdurui test`, use the "Reload theme" button. The same file, read from the
jar, provides the defaults when a resource pack ships an incomplete `theme.json`.

Colours: write `"#RRGGBB"` (opaque) or `"#AARRGGBB"`. A JSON integer is accepted; if it fits in
24 bits (≤ `0xFFFFFF`, so no alpha byte), it is read as opaque `RRGGBB`, with a warning in the
log, rather than giving an invisible colour.

## Sprites

The 9-slice sprites in `assets/galdurui/textures/gui/sprites/` are grayscale: their colour comes
from the theme and is applied at render time (a white pixel takes exactly the theme colour).
Changing the colours in `theme.json` therefore needs no new images; only the shape (corners,
relief) is described by the generator. Minecraft 1.21.1 does not stretch the edges and centre of
a 9-slice, it repeats them in tiles (sprite − 2 × border). `GaldurRender` sends all tiles of a
sprite in a single draw call, but large sprites still keep the tile count low. Edges and centre
must stay uniform, or the repetition shows.

| Sprite | Size, border | Relief | Used by (tint) |
|---|---|---|---|
| `panel` | 256, 4 | raised | `GaldurPanelLayout` (`background`) |
| `button`, `button_hover`, `button_pressed`, `button_disabled` | 64, 4 | raised (`pressed` sunken) | `GaldurButton`, `GaldurIconButton` (accent by state, `surface` when disabled) |
| `field` | 64, 4 | sunken | search field, list frame, checkbox (`surface`), progress bar track (`background`) |
| `slot` | 64, 4 | sunken | `GaldurItemSlot`, `GaldurItemGrid` (`background`) |
| `tab`, `tab_selected` | 64, 4 | raised, open at the bottom | `GaldurTabs` (`surface`, `accent` for the active tab) |
| `scrollbar_track`, `scrollbar_thumb` | 32, 2 | sunken / raised | `GaldurScrollList` and `GaldurItemGrid` scrollbars (`background`, `text_muted`) |

Sprites with the same pixels (`button`/`button_hover`/`button_disabled`, `field`/`slot`,
`tab`/`tab_selected`) stay separate so a resource pack can redraw each one. To regenerate them:

```powershell
.\gradlew.bat generateSprites
```

The generator (`common/tools/GenerateSprites.java`) is compiled in its own source set: it does not run
during the build and is not in the mod jar. CI runs it again and fails if the committed PNGs no
longer match its output.

## Animations

Hover fades on buttons and tabs, a checkbox mark that grows, windows and menu screens that rise
slightly on open, tab content sliding in from its side, smooth list scrolling. Short durations
(90 to 180 ms) and the curves of `api.anim.GaldurEasing`. Players can turn everything off:
Mods → Galdur UI → Config → Animations (`config/galdurui-client.toml` on NeoForge and Forge, `config/galdurui-client.json` on Fabric, where the screen needs Mod Menu). A mod animating its own
widgets uses `api.anim.AnimatedValue`, which respects this setting.

## Title font

Titles (`addTitle`, menu screen titles) use Fredoka SemiBold, font `galdurui:title`
(`assets/galdurui/font/title.json`). `api.text.GaldurFonts.title(text)` applies it to any text.
Missing characters come from Minecraft's font; a resource pack can replace the font.

## Narration

Every widget is read by Minecraft's narrator (Ctrl+B), in the player's language: vanilla texts
when they exist ("Button", "Checkbox", "Tab 1 of 3 selected"...), otherwise the
`narration.galdurui.*` keys of `assets/galdurui/lang` (English and French). A disabled widget
adds "disabled"; a slot or a grid reads the item and its full amount; a menu screen reads the
item of the hovered slot; a window reads its first title on open. The code is in
`internal.narration.GaldurNarration`.

## Performance

Measured with Spark on the test terminal (a grid of every item above the inventory): about
120 FPS, against 94 for the vanilla creative inventory on the same machine. The main gains
come from `GaldurRender`: one draw call per 9-slice sprite, cell backgrounds of a grid in one
call, items drawn in two lighting passes instead of one flush per item, and amounts batched
into a single submission.

## Structure

- `com.galdurstudio.galdurui.api`: public, stable API used by our mods. `api.menu` is the only
  package that can load on a dedicated server; all others are client-only.
- `com.galdurstudio.galdurui.internal`: internal implementation, may change at any time.
  `internal.render.GaldurRender` is the only class that calls `GuiGraphics`. No public `api`
  signature exposes an `internal` type.

## Compatibility and versions

The Galdur UI API is not independent from Minecraft: its components extend vanilla classes to
fit into the game's screens and layouts.

| Galdur UI class | Extends / implements (vanilla) |
|---|---|
| `api.widget.GaldurButton` | `AbstractWidget` |
| `api.widget.GaldurLabel` | `AbstractWidget` |
| `api.widget.GaldurCheckbox` | `AbstractWidget` |
| `api.widget.GaldurScrollList` | `AbstractWidget` |
| `api.widget.GaldurSearchField` | `AbstractWidget` (holds an `EditBox`) |
| `api.widget.GaldurItemSlot` | `AbstractWidget` |
| `api.widget.GaldurTabs` | `AbstractWidget` |
| `api.widget.GaldurProgressBar` | `AbstractWidget` |
| `api.widget.GaldurIconButton` | `GaldurButton` (so `AbstractWidget`) |
| `api.widget.GaldurItemGrid` | `AbstractWidget` |
| `api.screen.GaldurContainerScreen` | `AbstractContainerScreen` |
| `api.menu.GaldurMenu` | `AbstractContainerMenu` (logic side, loads on a server) |
| `api.layout.GaldurPanelLayout` | `AbstractLayout` (so `Layout`), `Renderable` |

Their render methods take a `GuiGraphics` and their mouse and keyboard methods follow the
`AbstractWidget` signatures. When Mojang changes these classes (rendering in 1.21.6 or input in
1.21.9, for instance), the Galdur UI API changes with them: such an update may require a
**major version** of Galdur UI, even though `GaldurRender` absorbs most of the work.

**Rule for our mods**: only call the Galdur API (`api.*`) to draw and build screens, never
`GuiGraphics` directly, and never depend on `internal.*`. The more a mod goes through the API,
the less code it has to rework for each Minecraft version.

## Versioning

Galdur UI follows [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`.

- **PATCH**: bug fixes, no API change.
- **MINOR**: new features; code written against an earlier minor of the same major still
  compiles and works.
- **MAJOR**: incompatible change to `api.*`, including one forced by a Minecraft update (see
  above).

Before 1.0.0, a MINOR may still break the API: depend on `[0.1,0.2)` rather than `[0.1,)`.
`internal.*` is not covered. Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## JEI and EMI integrations

Optional, in `internal.compat`: compiled against the JEI and EMI APIs (`compileOnly`), not
bundled in the jar and never loaded when the matching mod is not installed. Both run in
development (versions in `gradle.properties`).

- The item list avoids the panel of a `GaldurContainerScreen` and its `getExclusionZones()`.
- Hovering an item of a `GaldurItemGrid` and pressing R or U shows its recipes, **in a menu
  screen** (`GaldurContainerScreen`): JEI and EMI are not active on plain screens such as
  `/galdurui test`. To try it: `/galduruidev terminal`.

## Decisions

- **`GaldurLabel` narration**: a label is inactive (`active = false`), like vanilla's
  `StringWidget`, so the screen's automatic narration does not read it, even though
  `updateWidgetNarration` returns its text. Same behaviour as vanilla, on purpose.
- **Development tools outside the jar**: demo screens, test blocks and commands are in the
  `galduruitest` test mod (`neoforge/src/testmod`), loaded only by the development runs.
- **API methods unused internally**: `GaldurButton.withSprites`, `getPreferredWidth`, `isHeld`,
  `clearHeld`, `GaldurLabel.getText`, `GaldurPanelLayout.withSprite`, `getChild`, etc. are not
  called in this repository but are kept on purpose: they are part of the API offered to our
  mods.
- **Separate sprites, even with identical pixels**: `panel`, `button`, `button_hover` and
  `button_disabled` currently have the same pixels, as do `field` and `slot`, or `tab` and
  `tab_selected` (only the theme tint tells them apart). They stay separate so a resource pack
  can redraw each state independently.

## License

Galdur UI is distributed under the GNU LGPL v3 only (`LGPL-3.0-only`). See [LICENSE](LICENSE)
(LGPL v3) and [COPYING](COPYING) (GPL v3, which the LGPL refers to).

The title font, Fredoka (Milena Brandão, Hafontia), is under the SIL Open Font License 1.1: see
`common/src/main/resources/assets/galdurui/font/fredoka_ofl.txt`. The bundled file is a SemiBold
instance of the variable font, reduced to Latin alphabets.
