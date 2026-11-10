# Changelog

All notable changes to Galdur UI. The format follows [Keep a Changelog](https://keepachangelog.com/),
and versions follow [Semantic Versioning](https://semver.org/) (see "Versioning" in the README).

## [Unreleased]

### Added

- Minecraft 1.20.1, on Forge 47 and Fabric. Same API as on 1.21.1.
- `GaldurStack`: a row or column of elements with a fixed gap. Use it instead of `LinearLayout`, which
  works differently in 1.20.1.
- Minecraft 26.3, on NeoForge and Fabric. Same API, except that render methods take a
  `GuiGraphicsExtractor`, Minecraft's new name for `GuiGraphics`. No EMI integration until EMI is
  released for 26.3.
- `GaldurBridge`: restyles a mod's existing screens with one call per screen class or package, without
  touching their code. Vanilla buttons, text fields, checkboxes and sliders use the Galdur theme, and
  container screens can also get the Galdur panel and slot frames.
- "Galdur look on every menu" setting (`restyleAll`, off by default): restyles every menu in the game,
  vanilla and other mods included. Menus listed in `excludedScreens` are left alone.
- Theme editor (Galdur UI settings, "Edit the theme…"): players pick the theme colours in game, with a
  live preview. The colours are saved in `config/galdurui-theme.json`, in the format of `theme.json`.
- Gradients: any theme colour can be a gradient, in `theme.json` (a list of colours) and in the editor (up to four).
- Element colours (`button`, `field`, `checkbox`, `slider`, `panel`, `slot`, `list`, `tab`, `progress`...),
  each falling back on a palette colour until set. Read them with `GaldurTheme.paint(key)`, which returns a `GaldurPaint`.
- Theme sharing: copy a theme to paste in a message, paste one back, load a saved or dropped file.
  "Galdur theme" clears the player's colours; the theme it replaces is kept as a backup.
- "Where to use the style…": each part of the Galdur look on or off, for the whole game, vanilla
  inventories or menus, a mod or one screen (`config/galdurui-style.json`).
- The player inventory and the creative inventory get the Galdur panel, slots, tabs and scroll bar with
  the "Galdur look on every menu" setting; Minecraft still draws their tabs, search field and player.
- `GaldurBridge.lock` and `GaldurBridge.lockTheme` for mods, and a `"locked"` section for modpacks.
- `GaldurWidget`: base class of the Galdur widgets, with the same mouse and keyboard methods on every
  Minecraft version (1.21.9 switched to input events). Extend it to write a widget once for all versions.

### Changed

- The project is built with Stonecutter, to publish Galdur UI for several Minecraft versions from the same
  code. All the code is now in `src/`; the `common`, `neoforge`, `fabric` and `forge` modules are gone.
- Artifact versions now end with the Minecraft version: `galdurui-neoforge:1.0.0+1.21.1` instead of
  `galdurui-neoforge:1.0.0`. Nothing changes in the existing API.
- Gradle 9.9, run on Java 25. The 1.21.1 jars are still compiled for Java 21.
- The Galdur widgets extend `GaldurWidget` instead of `AbstractWidget` directly.
- NeoForge now opens the same settings screen as Fabric and Forge, with the theme editor, instead of
  NeoForge's generic config screen.
- The settings screens use the Galdur widgets, always with the default Galdur theme, so they stay
  readable whatever colours the player picked.
- Titles use the Minecraft font again. `GaldurFonts` is deprecated: `title(text)` returns the text
  unchanged, and the Fredoka font is no longer in the jar.
- The config files explain each setting in English and in French.

## [1.0.0] - 2026-10-26

First stable release, for Minecraft 1.21.1 on NeoForge 21.1, Fabric and Forge 52. From now on `api.*` follows
Semantic Versioning strictly.

### Added

- Fabric and Forge versions (`galdurui-fabric`, `galdurui-forge`), next to NeoForge. The code is shared in the
  `common` module; each loader module only holds its entry point, settings and metadata.
- Settings screen for Fabric (through Mod Menu) and Forge; Fabric settings are in `config/galdurui-client.json`.
- Registration helpers for `GaldurContainerScreen`: `GaldurNeoForgeScreens`, `GaldurForgeScreens`,
  `GaldurFabricScreens`.

### Changed

- **Breaking**: there is now one artifact per loader (`galdurui-neoforge` instead of `galdurui`).
- **Breaking**: `GaldurContainerScreen.factory()` is replaced by `GaldurNeoForgeScreens.factory()` (NeoForge).
- Widgets override the vanilla `onClick(double, double)` instead of NeoForge's 3-argument one (same behaviour
  on NeoForge, which calls it).
- On Fabric, the hovered slot of a `GaldurContainerScreen` gets the vanilla white veil (the accent veil needs a
  NeoForge or Forge hook); the accent outline stays.
- The font licence file is now `fredoka_ofl.txt` (Fabric rejects upper case in resource paths).

## [0.1.0]

First version, for Minecraft 1.21.1 and NeoForge 21.1.

### Added

- Theme: colours and spacings in `assets/galdurui/theme.json`, reloaded with resources and
  overridable by a resource pack (missing keys fall back to the defaults).
- Grayscale 9-slice sprites tinted by the theme, and their generator (`generateSprites`).
- Widgets (`api.widget`): button, icon button, label, checkbox, search field, scrolling list,
  tabs, progress bar, item slot with abbreviated amounts, scrolling item grid without real slots.
- `api.layout.GaldurPanelLayout`: a themed panel around vanilla layouts.
- Declarative windows (`api.window`, `GaldurLib.createWindow()`): titles, text, buttons, rows,
  lists, search, checkboxes, progress bars and tabs, with handles to change them afterwards.
- Slot menus: `api.menu.GaldurMenu` (client and server) and `api.screen.GaldurContainerScreen`.
- Optional JEI and EMI integrations: exclusion zones, recipes (R / U) from an item grid.
- Narration of every widget, in English and French.
- Animations (hover, opening, tab change, scrolling), with a client setting to turn them off.
- Fredoka SemiBold title font (`api.text.GaldurFonts`).
- Batched rendering of sprites, grid cells and items.
