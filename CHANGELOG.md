# Changelog

All notable changes to Galdur UI. The format follows [Keep a Changelog](https://keepachangelog.com/),
and versions follow [Semantic Versioning](https://semver.org/) (see "Versioning" in the README).

## [Unreleased]

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
