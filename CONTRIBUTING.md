# Contributing to Galdur UI

Thanks for taking a look. Bug reports, ideas and pull requests are all welcome.

## Before you start

- Look at the [open issues](https://github.com/Galdur-Studio/galdur-ui/issues) first.
- For a big change, open an issue to discuss it before writing code.
- Code in `api` and `internal` must only use vanilla Minecraft: no loader API. Loader code goes in
  the `neoforge`, `fabric` or `forge` packages.
- Code that differs between Minecraft versions goes in Stonecutter comments (`//? if >=1.21.6 {`),
  in `internal` when possible: drawing in `GaldurRender`, client calls in `internal.client.Client`,
  keys and buttons in `GaldurInput`. See the Development section of the README.
- `api.*` follows [Semantic Versioning](https://semver.org/); `internal.*` can change at any time.

## Building

Gradle runs on Java 25 and compiles Minecraft 1.21.1 with Java 21. It downloads both if they are
missing; you only need Java 17 or newer to start `gradlew`.

```bash
./gradlew build
```

Before opening a pull request, run `./gradlew build javadoc compileTestmodJava`, then switch `src/` back
to the committed version with the `Reset active project` task (CI fails otherwise). If you changed sprites, also
run `./gradlew :tools:generateSprites` and commit the PNGs it produces.

## Pull requests

- One topic per pull request, with a short note on why you made the change.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`,
  `docs:`...).
- Add a line to the `[Unreleased]` section of [CHANGELOG.md](CHANGELOG.md) for any visible change.
- By contributing, you agree that your work is published under the project's licence
  (LGPL-3.0-only, see [LICENSE](LICENSE)).
