# Contributing to Galdur UI

Thanks for your interest! Bug reports, ideas and pull requests are welcome.

## Before you start

- Look at the [open issues](https://github.com/Galdur-Studio/galdur-ui/issues) first.
- For a big change, open an issue to discuss it before writing code.
- Code in `common/` must compile against vanilla Minecraft only: no loader API. Loader code goes in
  `neoforge/`, `fabric/` or `forge/`.
- `api.*` follows [Semantic Versioning](https://semver.org/); `internal.*` can change at any time.

## Building

Java 21 is required.

```bash
./gradlew build
```

Run the checks of the CI before opening a pull request: `./gradlew build javadoc`, and, if you changed
sprites, `./gradlew :common:generateSprites` and commit the PNGs it produces.

## Pull requests

- One topic per pull request, with a short description of the why.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`,
  `docs:`...).
- Add a line to the `[Unreleased]` section of [CHANGELOG.md](CHANGELOG.md) for any visible change.
- By contributing, you agree that your work is published under the project's licence
  (LGPL-3.0-only, see [LICENSE](LICENSE)).
