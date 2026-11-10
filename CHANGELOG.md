# Changelog

All notable changes to Galdur UI. The format follows [Keep a Changelog](https://keepachangelog.com/),
and versions follow [Semantic Versioning](https://semver.org/) (see "Versioning" in the README).

## [Unreleased]

## [1.1.0] - 2026-11-10

Galdur UI now supports Minecraft 1.20.1 and 26.3, and players can apply the Galdur look to the whole game.

### Added

- Minecraft 1.20.1 (Forge, Fabric) and 26.3 (NeoForge, Fabric), besides 1.21.1. The API is the same. On 26.3, render methods take a `GuiGraphicsExtractor`, which is Minecraft's new name for `GuiGraphics`. No EMI integration on 26.3 until EMI is released for it.
- `GaldurBridge` restyles a mod's existing screens with one call per screen class or package, without changing their code. Buttons, text fields, checkboxes and sliders use the Galdur theme, and container screens can also get the Galdur panel and slot frames.
- "Galdur look on every menu" setting (`restyleAll`, off by default). It applies the look to every menu in the game, including vanilla and other mods' menus, the player inventory and the creative inventory. "Excluded menus" (`excludedScreens`) skips the ones you pick.
- "Where to use the style…" lets the player turn each part of the look on or off for the whole game, the vanilla inventories or menus, one mod, or one screen (`config/galdurui-style.json`). Mods can lock a part with `GaldurBridge.lock` and `lockTheme`. Modpacks can do the same with a `"locked"` section in that file.
- Theme editor in game ("Edit the theme…" in the Galdur UI settings), with live preview, one colour per element (`GaldurTheme.paint(key)`, which returns a `GaldurPaint`) and gradients of up to four colours, also available in `theme.json`. Saved in `config/galdurui-theme.json`.
- Theme sharing: copy a theme to paste in a message, paste one, or load a saved or dropped file. "Galdur theme" goes back to the default theme and keeps the previous one as a backup.
- `GaldurStack`, a row or column of elements with a fixed gap. Use it instead of `LinearLayout`, which works differently in 1.20.1.
- `GaldurWidget`, the base class of the Galdur widgets, with the same mouse and keyboard methods on every Minecraft version.

### Changed

- The project is built with Stonecutter. All the code is in `src/` and the `common`, `neoforge`, `fabric` and `forge` modules are gone. Artifact versions now end with the Minecraft version (`galdurui-neoforge:1.1.0+1.21.1`). The API does not change. Gradle 9.9 runs on Java 25.
- The Galdur widgets extend `GaldurWidget` instead of `AbstractWidget`.
- NeoForge opens the same settings screen as Fabric and Forge, theme editor included, instead of its generic config screen. The settings screens use the Galdur widgets with the default theme, so they stay readable.
- Titles use the Minecraft font again. `GaldurFonts` is deprecated: `title(text)` returns the text unchanged, and the Fredoka font is no longer in the jar.
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
