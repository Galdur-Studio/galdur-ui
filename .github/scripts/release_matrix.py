"""Lists the jars built by Gradle (versions/<minecraft>-<loader>/build/libs) for the release workflow.

Prints one JSON line: the GitHub Actions matrix, one entry per jar, with what mc-publish needs
(game versions and Java from stonecutter.properties.toml, dependencies by loader).

    python .github/scripts/release_matrix.py <mod version>
"""
import json
import pathlib
import sys
import tomllib

version = sys.argv[1]
root = pathlib.Path(__file__).resolve().parents[2]
props = tomllib.loads((root / "stonecutter.properties.toml").read_text(encoding="utf-8"))

LOADER_NAMES = {"neoforge": "NeoForge", "fabric": "Fabric", "forge": "Forge"}
DEPENDENCIES = {
    "neoforge": ["jei(optional)", "emi(optional)"],
    "fabric": ["fabric-api(required)", "modmenu(optional)", "jei(optional)", "emi(optional)"],
    "forge": ["jei(optional)", "emi(optional)"],
}

entries = []
for node in sorted((root / "versions").iterdir()):
    minecraft, loader = node.name.rsplit("-", 1)
    jar = node / "build" / "libs" / f"galdurui-{loader}-{version}+{minecraft}.jar"
    if not jar.is_file():
        sys.exit(f"Missing jar: {jar.relative_to(root)}")
    mc = props[minecraft]["mc"]
    entries.append({
        "node": node.name,
        "jar": jar.relative_to(root).as_posix(),
        "loader": loader,
        "name": f"Galdur UI {version} ({LOADER_NAMES[loader]} {minecraft})",
        "version": f"{version}+{minecraft}-{loader}",
        "game_versions": "\n".join(mc["releases"]),
        "java": mc["java"],
        "dependencies": "\n".join(DEPENDENCIES[loader]),
    })

print(json.dumps({"include": entries}))
