pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    // Stonecutter builds one jar per Minecraft version and loader from the single src/ folder.
    // Guide: https://stonecutter.kikugie.dev/wiki/v2/guides/start/introduction
    id("dev.kikugie.stonecutter") version "0.10"
    // Picks the Fabric Loom for each version: with remapping up to 1.21.x, without from 26.1
    // (Minecraft is no longer obfuscated). https://stonecutter.kikugie.dev/wiki/v2/guides/tips/loom-back-compat
    id("dev.kikugie.loom-back-compat") version "0.4.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // One Gradle project per Minecraft version and loader, in versions/<minecraft>-<loader>,
        // built by build.<loader>.gradle.kts. The versions of each dependency are in
        // stonecutter.properties.toml.
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) version("$version-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        mc("1.20.1", "fabric")
        // Forge 1.20.1 runs mods with SRG names: ModDevGradle Legacy builds and reobfuscates it.
        version("1.20.1-forge", "1.20.1").buildscript("build.legacyforge.gradle.kts")
        mc("1.21.1", "neoforge", "fabric", "forge")
        mc("26.3", "neoforge", "fabric")

        // The version src/ is in when committed. Always switch back to it before a commit.
        vcsVersion = "1.21.1-neoforge"
    }
}

rootProject.name = "galdur-ui"

// Dev tools (sprite generator), outside the mod.
include("tools")
