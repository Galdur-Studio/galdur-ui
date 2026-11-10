# Galdur UI

*[Version française](README.fr.md)*

Galdur Studio's GUI library: theme, components and screens for our Minecraft mods.

[Website](https://galdurstudio.com/mods/galdur-ui) · [Documentation](https://docs.galdurstudio.com) · [Wiki](https://wiki.galdurstudio.com) · [GitHub](https://github.com/Galdur-Studio/galdur-ui) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/galdur-ui) · [Issues](https://github.com/Galdur-Studio/galdur-ui/issues)

## Versions

| Minecraft | Loaders | Java |
|---|---|---|
| 1.20.1 | Forge 47.x, Fabric (Loader 0.16+, Fabric API) | 17 |
| 1.21.1 | NeoForge 21.1.x, Fabric (Loader 0.16+, Fabric API), Forge 52.x | 21 |
| 26.3 | NeoForge 26.3.x, Fabric (Loader 0.16+, Fabric API) | 25 |

## Getting started

### 1. Add Galdur UI to your mod

Prerequisites: a mod project for one of the versions above, on your loader (NeoForge with
ModDevGradle, Fabric with Loom, Forge with ForgeGradle 7), with the Java version from the table.

**Step 1. Publish Galdur UI** (once, then again after each change to the library). In the
`galdur-ui` folder:

```powershell
.\gradlew.bat publish
```

This writes one artifact per loader and Minecraft version in `galdur-ui/repo/`: `galdurui-neoforge`,
`galdurui-fabric` and `galdurui-forge` (`com.galdurstudio.galdurui:<artifact>:1.1.0+1.21.1`). Your mod
only needs the one for its loader. Until Galdur Studio has a Maven repository, this local folder is the repository.

**Step 2. Add the dependency** in your mod's `build.gradle` (the path is relative to your mod's
folder; adjust it to where `galdur-ui` is):

```groovy
repositories {
    maven { url = uri("../galdur-ui/repo") }
}

dependencies {
    // NeoForge (ModDevGradle) or Forge (ForgeGradle):
    implementation "com.galdurstudio.galdurui:galdurui-neoforge:1.1.0+1.21.1"   // or galdurui-forge

    // Fabric (Loom): a mod dependency, remapped by Loom
    modImplementation "com.galdurstudio.galdurui:galdurui-fabric:1.1.0+1.21.1"
}
```

Keep only the line of your loader. Fabric also needs Fabric API in your mod, as usual.

**Step 3. Declare the dependency in your mod's metadata**, so the game refuses to start your mod
without Galdur UI instead of crashing later (replace `mymod` with your mod id):

| Loader | File | What to add |
|---|---|---|
| NeoForge | `src/main/resources/META-INF/neoforge.mods.toml` | the block below |
| Forge | `src/main/resources/META-INF/mods.toml` | the same block, with `mandatory = true` instead of `type = "required"` |
| Fabric | `src/main/resources/fabric.mod.json` | `"depends": { "galdurui": ">=1.0.0" }` |

```toml
[[dependencies.mymod]]
modId = "galdurui"
type = "required"
versionRange = "[1.0,2.0)"
ordering = "NONE"
side = "BOTH"
```

Galdur UI is a separate mod, not bundled in your jar: players install the jar for their loader and
Minecraft version (`galdurui-<loader>-1.1.0+1.21.1.jar`) next to yours. A local build puts it in
`versions/<minecraft>-<loader>/build/libs`.

**Step 4. Check that it works**: run `.\gradlew.bat runClient` in your mod (the task has the
same name on all three loaders), then open a screen built with the examples below. If the game says
Galdur UI is missing, the dependency of step 2 is not on the run classpath.

Then use only `com.galdurstudio.galdurui.api.*`, with the same code on every loader. The only
thing that differs is how you register a menu screen (section 3).

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
read or change the element later (`setVisible`, `setEnabled`, `setProgress`...). See the
Javadoc of `api.window` for the details.

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
then its screen. This is the one line that differs per loader, because each loader registers
screens its own way; the helper classes are in the artifact of the loader
(`com.galdurstudio.galdurui.api.neoforge.GaldurNeoForgeScreens`, `...api.forge.GaldurForgeScreens`,
`...api.fabric.GaldurFabricScreens`). A mod for several loaders keeps this line in its loader module.
Client side only:

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
terminals); see `TestTerminalScreen` in the NeoForge test mod.

### 4. Restyle the screens a mod already has

`GaldurBridge` draws the vanilla buttons, cycle buttons, text fields, checkboxes and sliders of
existing screens with the Galdur theme, without touching the screen code. Minecraft still draws
their text and handles input; a mod's own widgets keep their look. Register the screens once on the
client, at startup:

```java
GaldurBridge.restyle(MachineScreen.class);                   // one screen and its subclasses
GaldurBridge.restylePackage("com.example.mymod");            // every screen of the mod
GaldurBridge.restyle(CrateScreen.class, Background.REPLACE); // also the panel and slots
```

For a screen with slots, `Background.KEEP` (the default) keeps its texture. `Background.REPLACE`
draws the Galdur panel and slot frames instead and puts the labels in the theme text colour, but
what the screen drew in its background (arrows, gauges) is gone. To keep them, stay on `KEEP` and
call `GaldurBridge.drawContainerBackground(graphics, this)` in your `renderBg` (`extractBackground`
since 26.1) in place of the texture, then draw them on top.

### Rules

- Only use `com.galdurstudio.galdurui.api.*`; `internal.*` can change in any version.
- Everything except `api.menu` is client-only: keep it out of code that runs on a dedicated
  server.
- Read theme colours and spacings every frame or every `init()`, never in a `static final`
  field: a resource pack can change them.

## Development

All the code is in `src/`, and [Stonecutter](https://stonecutter.kikugie.dev/) builds one jar per
Minecraft version and loader from it. Each build is a Gradle project in `versions/<minecraft>-<loader>`
(for example `1.21.1-neoforge`).

| Where | What |
|---|---|
| `src/main/java/.../galdurui/api`, `internal` | the code shared by every loader: it only uses vanilla Minecraft |
| `src/main/java/.../galdurui/<loader>`, `api/<loader>` | what needs a loader API: entry point, settings, screen registration. A jar only gets its own loader's packages |
| `src/main/templates/<loader>` | `neoforge.mods.toml` and `mods.toml` |
| `src/testmod` | the test mod; `testmod/<loader>` for what only one loader has |
| `stonecutter.properties.toml` | the versions of Minecraft, of the loaders and of every dependency |
| `build.<loader>.gradle.kts` | the build of each loader; what they share is in `buildSrc` |
| `build.legacyforge.gradle.kts` | Forge 1.20.1, built with ModDevGradle Legacy: the jar is reobfuscated to SRG names |
| `src/main/java/.../internal/mixin` | the bridge mixins (`GaldurBridge`) |

```powershell
.\gradlew.bat :1.21.1-neoforge:runClient   # development client (library + test mod); same with -fabric or -forge
.\gradlew.bat build                        # compiles, runs the tests and builds every jar (versions/*/build/libs)
.\gradlew.bat :1.21.1-neoforge:test        # unit tests only (without starting Minecraft)
```

Gradle runs on Java 25, which Stonecutter needs, and downloads it if it is missing
(`gradle/gradle-daemon-jvm.properties`). Each Minecraft version compiles with its own Java: 21 for 1.21.1.

### Code that differs between Minecraft versions

`src/` is written for one version at a time, the *active* one (`stonecutter.gradle.kts`). When a
line differs from one version to another, it goes in a Stonecutter comment:

```java
//? if >=1.21.6 {
graphics.pose().pushMatrix();
//?} else {
/*graphics.pose().pushPose();
*///?}
```

Stonecutter comments out the lines that do not belong to the active version. To work on another
version, run the Gradle task `Set active project to <version>` (group `stonecutter`) or use the IntelliJ
plugin. Before committing, run `Reset active project`: CI fails if `src/` is not on `1.21.1-neoforge`. Keep these comments in `internal` as much as possible, so that `api` stays the
same on every version.

Mojang's plain renames (`ResourceLocation` to `Identifier`, `GuiGraphics` to `GuiGraphicsExtractor`,
`render` to `extractRenderState`...) need no comment: Stonecutter replaces them in the whole source
(`replacements` in `stonecutter.gradle.kts`). The calls that changed in other ways are kept in a few
classes: `GaldurRender` for drawing, `GuiSprites` for sprites before 26.1, `internal.client.Client`
for screens and the game loop, `GaldurInput` for keys and buttons, and `GaldurWidget` for the input
events of widgets.

### Adding a Minecraft version

1. In `stonecutter.properties.toml`, copy the tables of the closest version and update the versions.
2. In `settings.gradle.kts`, add a line `mc("<version>", <loaders>)`.
3. Switch to the new version, then fix what no longer compiles with `//?` comments.
4. Run `.\gradlew.bat build`, then the screenshot tour on Fabric and NeoForge (see Test mod).

The release workflow publishes every jar built, with no list to update.

### Test mod

Demo screens, test blocks and debug tools live in a separate source set, `src/testmod`, as a
second mod (`galduruitest`). Development runs load it next to Galdur UI; it is never in the library
jar. The test blocks and most commands are NeoForge only (`testmod/neoforge`); Fabric has
`/galdurui test` and `/galdurui window` only (`testmod/fabric`). Forge has none: use the NeoForge or
Fabric client to look at the widgets.

To check a version without clicking, run the screenshot tour. From the title screen, it opens each
tab of the demo screen and the demo window, saves a screenshot of each to `run/screenshots/` and quits.
Since 26.1, items only exist in a loaded world, so the tour first creates the flat creative world
`galduruitest`. On NeoForge it also opens the test terminal (`/galduruidev terminal`):

```powershell
.\gradlew.bat :1.20.1-fabric:runClient -Pgalduruitest.screenshots
```

| Command | What it does |
|---|---|
| `/galdurui test` | demo screen of every widget |
| `/galdurui window` | demo of the declarative `GaldurWindow` API |
| `/galdurui debug` | toggles the layout debug outlines |
| `/galdurui fps` | toggles an FPS counter that stays visible on screens (F3 is hidden there) |
| `/galduruidev chest` | gives a test chest (placeable block, also in the "Functional Blocks" creative tab) that opens in a `GaldurContainerScreen` |
| `/galduruidev terminal` | opens a test terminal: a grid of every item in the game above the inventory |

The NeoForge test mod also adds a blue theme resource pack (Options → Resource Packs). It overrides
only part of `theme.json`, to check that a pack can change the theme and that missing keys fall
back to the defaults.

## Galdur look on every menu

This setting gives the Galdur look to every menu in the game, vanilla and other mods included, and
is off by default. Players turn it on in the Galdur UI settings; modpacks can set
`restyleAll = true` in `config/galdurui-client.toml` (`galdurui-client.json` on Fabric).

Buttons, text fields, checkboxes and sliders change everywhere. Vanilla chests, shulker boxes,
dispensers and hoppers also get the Galdur panel. Other menus with slots keep their texture so
furnace arrows or a mod's gauges stay visible.

If a menu looks wrong, add its class name or package to `excludedScreens`, even if a mod restyles it
itself. The log line `Galdur look given to` gives the class name. The list can also be edited in the Galdur
UI settings, comma-separated.

## Theme

`assets/galdurui/theme.json` is the single source of the style guide: colours and spacing
(`spacing` section). It is read again on every resource reload. Minecraft ignores F3+T while a
screen is open: in `/galdurui test`, use the "Reload theme" button. The same file, read from the
jar, provides the defaults when a resource pack ships an incomplete `theme.json`.

Colours: write `"#RRGGBB"` (opaque) or `"#AARRGGBB"`. A JSON integer is accepted; if it fits in
24 bits (≤ `0xFFFFFF`, so no alpha byte), it is read as opaque `RRGGBB`, with a warning in the
log, rather than giving an invisible colour.

### Theme editor

Players can change the theme in game with "Edit the theme…" in the Galdur UI settings. Besides the
palette, each element has its own colour (button, field, checkbox, slider, panel, slot, list, tab,
progress bar...), which falls back on a palette colour until it is changed. In the editor, any colour
can be a gradient of up to four colours, vertical or horizontal. Every Galdur UI screen updates right away.
"Done" saves to `config/galdurui-theme.json`, which overrides resource packs; "Cancel" or Escape
restores the old theme.

"Galdur theme" clears the player's colours, so the resource pack's theme or the default one applies
again. The button is also in the settings screen, which always uses the default Galdur theme and
stays readable whatever the player's colours. The old theme is kept in `galdurui-theme.backup.json`, and "Share…" > "Previous theme"
brings it back.

"Share…" copies the theme, with a name and an author, to paste in a message; it is also saved in
`config/galdurui-themes/`. "Paste a theme" finds the theme in copied text, even with other words
around it. You can also drop a theme file on the window. A shared theme only holds what its
author changed, in the `theme.json` format:

```json
{ "name": "Ocean", "accent": "#2F6FB5", "panel": ["#1E2A38", "#2A2420"],
  "button": { "gradient": ["#2F6FB5", "#24578F"], "direction": "horizontal" } }
```

### Where the style applies

"Where to use the style…" in the settings switches each part of the Galdur look on or off:
buttons, text fields, checkboxes, sliders, the inventory panel, inventory labels, and the player's
own colours. Each switch can be set for the whole game, vanilla inventories, vanilla menus, a
mod or a single screen (only screens opened since launch are listed). Left on "Default" (“Auto” in French), a switch
follows the wider level; otherwise the most precise setting wins. The choices are saved in `config/galdurui-style.json`.

A mod can lock a switch on its screens with `GaldurBridge.lock(...)`, and `GaldurBridge.lockTheme(...)`
keeps its colours whatever theme the player chose. A modpack locks values in the `"locked"` section
of `config/galdurui-style.json`, and its locks win over the mods'. `{"*": {"player_theme": false}}`
keeps the pack's theme for everyone and turns the theme editor off. Locked switches are greyed out.

## Sprites

The 9-slice sprites in `assets/galdurui/textures/gui/sprites/` are grayscale: their colour comes
from the theme and is applied at render time (a white pixel takes exactly the theme colour).
Changing the colours in `theme.json` therefore needs no new images; only the shape (corners,
relief) is described by the generator. Minecraft 1.21.1 does not stretch the edges and centre of
a 9-slice, it repeats them in tiles (sprite − 2 × border). `GaldurRender` sends all tiles of a
sprite in a single draw call, but the sprites are still made large to keep the tile count low. Edges and centre
must stay uniform, or the repetition shows.

Minecraft 1.20.1 has no GUI sprite atlas, so on that version Galdur UI reads each PNG and its
`.png.mcmeta` itself (`internal.render.GuiSprites`). The files don't change between versions, and a
resource pack replaces them the same way on each.

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
.\gradlew.bat :tools:generateSprites
```

The generator (`tools/src/main/java/GenerateSprites.java`) is a separate Gradle project: it does not run
during the build and is not in the mod jar. CI runs it again and fails if the committed PNGs no
longer match its output.

## Animations

Hover fades on buttons and tabs, a checkbox mark that grows, windows and menu screens that rise
slightly on open, tab content sliding in from its side, smooth list scrolling. Short durations
(90 to 180 ms) and the curves of `api.anim.GaldurEasing`. Players can turn everything off:
Mods → Galdur UI → Config → Animations (`config/galdurui-client.toml` on NeoForge and Forge, `config/galdurui-client.json` on Fabric, where the screen needs Mod Menu). A mod animating its own
widgets uses `api.anim.AnimatedValue`, which respects this setting.

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
| `api.widget.GaldurWidget` | `AbstractWidget` |
| `api.widget.GaldurButton` | `GaldurWidget` |
| `api.widget.GaldurLabel` | `GaldurWidget` |
| `api.widget.GaldurCheckbox` | `GaldurWidget` |
| `api.widget.GaldurScrollList` | `GaldurWidget` |
| `api.widget.GaldurSearchField` | `GaldurWidget` (holds an `EditBox`) |
| `api.widget.GaldurItemSlot` | `GaldurWidget` |
| `api.widget.GaldurTabs` | `GaldurWidget` |
| `api.widget.GaldurProgressBar` | `GaldurWidget` |
| `api.widget.GaldurIconButton` | `GaldurButton` |
| `api.widget.GaldurItemGrid` | `GaldurWidget` |
| `api.screen.GaldurContainerScreen` | `AbstractContainerScreen` |
| `api.menu.GaldurMenu` | `AbstractContainerMenu` (logic side, loads on a server) |
| `api.layout.GaldurPanelLayout` | `AbstractLayout` (so `Layout`), `Renderable` |
| `api.layout.GaldurStack` | `GridLayout` |

Their render methods take a `GuiGraphics` (`GuiGraphicsExtractor` since 26.1). Minecraft 1.21.9
replaced the mouse and keyboard methods of widgets with input events, and `GaldurWidget` turns them
back into the old methods. When Mojang changes these classes (rendering in 1.21.6 or input in
1.21.9, for instance), the Galdur UI API changes with them: such an update may require a
major version of Galdur UI, even though `GaldurRender` absorbs most of the work.

In our own mods, we only call the Galdur API (`api.*`) to draw and build screens, never
`GuiGraphics` directly, and we never depend on `internal.*`. For rows and columns, we use `GaldurStack`
instead of `LinearLayout`, which works differently in 1.20.1. The more a mod goes through the API,
the less code it has to rework for each Minecraft version.

## Versioning

Galdur UI follows [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`.

- PATCH: bug fixes, no API change.
- MINOR: new features; code written against an earlier minor of the same major still
  compiles and works.
- MAJOR: incompatible change to `api.*`, including one forced by a Minecraft update (see
  above).

From 1.0.0, the `api.*` package is stable: depend on `[1.0,2.0)`. Versions before 1.0.0 could still break
the API in a MINOR. `internal.*` is not covered. Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## JEI and EMI integrations

Optional, in `internal.compat`: compiled against the JEI and EMI APIs (`compileOnly`), not
bundled in the jar and never loaded when the matching mod is not installed. Both run in
development (versions in `stonecutter.properties.toml`). EMI isn't out for 26.3 yet, so the 26.3
jars ship without the EMI integration.

- The item list avoids the panel of a `GaldurContainerScreen` and its `getExclusionZones()`.
- Hovering an item of a `GaldurItemGrid` and pressing R or U shows its recipes. This only works
  in a menu screen (`GaldurContainerScreen`), because JEI and EMI are not active on plain screens
  such as `/galdurui test`. To try it: `/galduruidev terminal`.

## Decisions

- A `GaldurLabel` is inactive (`active = false`), like vanilla's `StringWidget`, so the
  screen's automatic narration does not read it, even though `updateWidgetNarration` returns
  its text. We kept the vanilla behaviour on purpose.
- Demo screens, test blocks and commands are in the `galduruitest` test mod
  (`src/testmod`), loaded only by the development runs, so they never end up in the jar.
- Some API methods are not called anywhere in this repository (`GaldurButton.withSprites`,
  `getPreferredWidth`, `isHeld`, `clearHeld`, `GaldurLabel.getText`,
  `GaldurPanelLayout.withSprite`, `getChild`, etc.). They are kept on purpose because they are
  part of the API offered to our mods.
- Sprites stay separate even when their pixels are identical. `button`, `button_hover` and
  `button_disabled` currently have the same pixels, as do `field` and `slot`, or `tab` and
  `tab_selected` (only the theme tint tells them apart). They stay separate so a resource pack
  can redraw each state independently.

## License

Copyright © 2026 Galdur Studio.

Galdur UI is distributed under the GNU LGPL v3 only (`LGPL-3.0-only`). See [LICENSE](LICENSE)
(LGPL v3) and [COPYING](COPYING) (GPL v3, which the LGPL refers to).

The Galdur logo and the name "Galdur" belong to Galdur Studio: the LGPL covers the code, not the branding.
