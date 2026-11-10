// Forge build for Minecraft 1.20.1 and older (versions/<minecraft>-forge), with ModDevGradle Legacy.
// Up to 1.20.1, Forge runs mods with SRG names: the jar must be reobfuscated, which ForgeGradle 7
// does not do. From 1.20.6 Forge uses Mojang names at runtime: see build.forge.gradle.kts.
// Shared settings: buildSrc/src/main/kotlin/galdur.common.gradle.kts

plugins {
    id("galdur.common")
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
}

val mc = sc.current.version
fun prop(name: String): String = sc.properties[name]

legacyForge {
    version = "$mc-${prop("deps.forge")}"

    parchment {
        mappingsVersion = prop("parchment.mappings")
        minecraftVersion = prop("parchment.minecraft")
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            client()
            // Loads the classes changed by the bridge mixins at startup (internal.bridge.BridgeCheck).
            systemProperty("galdurui.checkMixins", "true")
        }
        register("server") {
            server()
            programArgument("--nogui")
        }
    }
}

// Mixins of the bridge. Up to 1.20.1 they need a refmap, which turns the Mojang names they use
// into the SRG names Forge runs with.
mixin {
    add(sourceSets.main.get(), "galdurui.refmap.json")
    config("galdurui.mixins.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Optional JEI and EMI integrations (internal.compat): compiled against their API but neither
    // in the jar nor passed on. Without them installed, their plugins are simply never loaded.
    // "mod" configurations: published mods use SRG names and are remapped for development.
    modCompileOnly("mezz.jei:jei-$mc-common-api:${prop("deps.jei")}")
    modCompileOnly("mezz.jei:jei-$mc-forge-api:${prop("deps.jei")}")
    modRuntimeOnly("mezz.jei:jei-$mc-forge:${prop("deps.jei")}")
    sc.properties.getOrNull("deps.mezz_config")?.let { modRuntimeOnly("net.mezzdev.config:mezz_config-$mc-forge:$it") }
    modCompileOnly("maven.modrinth:emi:${prop("deps.emi")}+${prop("deps.emi_forge_variant")}")
}

// Fills the ${...} placeholders of src/main/templates/forge (mods.toml, pack.mcmeta).
// A missing property fails the build.
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version_range" to prop("mc.range"),
        "forge_version_range" to prop("deps.forge_range"),
        "pack_format" to prop("resource.pack_format"),
        "loader_version_range" to "[1,)",
        "mod_id" to prop("mod.id"),
        "mod_name" to prop("mod.name"),
        "mod_license" to prop("mod.license"),
        "mod_version" to prop("mod.version"),
        "mod_authors" to prop("mod.authors"),
        "mod_description" to prop("mod.description"),
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from(rootProject.file("src/main/templates/forge"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main { resources.srcDir(generateModMetadata) }
legacyForge.ideSyncTask(generateModMetadata)

tasks {
    processResources {
        // Fabric metadata.
        exclude("fabric.mod.json", "galdurui.accesswidener")
        // The refmap written by the Mixin annotation processor.
        filesMatching("galdurui.mixins.json") {
            filter { line -> line.replace("\"minVersion\"", "\"refmap\": \"galdurui.refmap.json\", \"minVersion\"") }
        }
    }

    jar {
        manifest.attributes("MixinConfigs" to "galdurui.mixins.json")
    }

    // Decompiles Minecraft once per version: one at a time, or the machine runs out of memory.
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}
