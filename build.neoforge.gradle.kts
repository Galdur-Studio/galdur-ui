// NeoForge build, run once per Minecraft version (versions/<minecraft>-neoforge).
// Shared settings: buildSrc/src/main/kotlin/galdur.common.gradle.kts

import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService

plugins {
    id("galdur.common")
    id("net.neoforged.moddev") version "2.0.148"
}

val mc = sc.current.version
fun prop(name: String): String = sc.properties[name]

sourceSets {
    // Test mod (demo screens, test blocks, dev commands): loaded by the dev runs only, never in the jar.
    create("testmod") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}
val testmod = sourceSets["testmod"]

// localRuntime: present when running in dev, but not passed on to mods that depend on Galdur UI.
val localRuntime = configurations.maybeCreate("localRuntime")
configurations {
    runtimeClasspath { extendsFrom(localRuntime) }
    named(testmod.runtimeClasspathConfigurationName) { extendsFrom(localRuntime) }
}

neoForge {
    version = prop("deps.neoforge")

    // Parameter names in Minecraft code; none from 26.1, which ships with its real names.
    sc.properties.getOrNull("parchment.mappings")?.let { mappings ->
        parchment {
            mappingsVersion = mappings
            minecraftVersion = prop("parchment.minecraft")
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets.main.get())
        }
        register("galduruitest") {
            sourceSet(testmod)
        }
    }

    runs {
        register("client") {
            client()
            // -Pgalduruitest.screenshots: screenshot tour of the test mod (testmod/ScreenshotTour).
            if (providers.gradleProperty("galduruitest.screenshots").isPresent) {
                systemProperty("galduruitest.screenshots", "true")
            }
            // -Pgalduruitest.media=en_us (or fr_fr): captures the wiki's photos and clips (testmod/MediaSession).
            providers.gradleProperty("galduruitest.media").orNull?.let { systemProperty("galduruitest.media", it) }
        }
        register("server") {
            server()
            programArgument("--nogui")
        }
        configureEach {
            // Every dev run loads the library and the test mod.
            sourceSet = testmod
            loadedMods = listOf(mods.getByName(prop("mod.id")), mods.getByName("galduruitest"))

            systemProperty("forge.logging.markers", "REGISTRIES")
            // Loads the classes changed by the bridge mixins at startup (internal.bridge.BridgeCheck).
            systemProperty("galdurui.checkMixins", "true")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }
}

neoForge.addModdingDependenciesTo(testmod)

dependencies {
    // Spark, JEI and EMI show up on screen: left out of the runs that capture the wiki's photos and clips.
    val capturing = providers.gradleProperty("galduruitest.media").isPresent
    // Spark profiler (/sparkc): dev only.
    if (!capturing) localRuntime("maven.modrinth:spark:${prop("deps.spark")}")

    // Optional JEI and EMI integrations (internal.compat): compiled against their API but neither
    // in the jar nor passed on. Without them installed, their plugins are simply never loaded.
    compileOnly("mezz.jei:jei-$mc-common-api:${prop("deps.jei")}")
    compileOnly("mezz.jei:jei-$mc-neoforge-api:${prop("deps.jei")}")
    if (!capturing) localRuntime("mezz.jei:jei-$mc-neoforge:${prop("deps.jei")}")
    sc.properties.getOrNull("deps.emi")?.let {
        compileOnly("maven.modrinth:emi:$it+neoforge")
        if (!capturing) localRuntime("maven.modrinth:emi:$it+neoforge")
    }
}

// Fills the ${...} placeholders of src/main/templates/neoforge (neoforge.mods.toml).
// A missing property fails the build.
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to mc,
        "minecraft_version_range" to prop("mc.range"),
        "neo_version" to prop("deps.neoforge"),
        "loader_version_range" to "[1,)",
        "mod_id" to prop("mod.id"),
        "mod_name" to prop("mod.name"),
        "mod_license" to prop("mod.license"),
        "mod_version" to prop("mod.version"),
        "mod_authors" to prop("mod.authors"),
        "mod_description" to prop("mod.description"),
        // Square icon: logoFile up to 1.21.x, iconFile since 26.1 (logoFile is deprecated there and
        // shows a warning screen at startup).
        "icon_key" to if (sc.current.parsed >= "26.1") "iconFile" else "logoFile",
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from(rootProject.file("src/main/templates/neoforge"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main { resources.srcDir(generateModMetadata) }
neoForge.ideSyncTask(generateModMetadata)

tasks {
    // Other loaders' metadata.
    processResources {
        exclude("fabric.mod.json", "galdurui.accesswidener")
    }
    named<ProcessResources>(testmod.processResourcesTaskName) {
        exclude("fabric.mod.json")
    }

    // NeoForge decompiles Minecraft once per version: one at a time, or the machine runs out of memory.
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
        usesService(StonecutterTaskMutexService.create(gradle, "createMinecraftArtifacts"))
    }
}
