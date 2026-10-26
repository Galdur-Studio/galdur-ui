# Changelog

All notable changes to Galdur UI. The format follows [Keep a Changelog](https://keepachangelog.com/),
and versions follow [Semantic Versioning](https://semver.org/) (see "Versioning" in the README).

## [Unreleased]

### Added

- Fabric and Forge versions (`galdurui-fabric`, `galdurui-forge`), next to NeoForge. The code is shared in the
  `common` module; each loader module only holds its entry point, settings and metadata.
- Settings screen for Fabric (through Mod Menu) and Forge; Fabric settings are in `config/galdurui-client.json`.
- Registration helpers for `GaldurContainerScreen`: `GaldurNeoForgeScreens`, `GaldurForgeScreens`,
  `GaldurFabricScreens`.

### Changed

- **Breaking**: the artifact is now one per loader (`galdurui-neoforge` instead of `galdurui`).
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
