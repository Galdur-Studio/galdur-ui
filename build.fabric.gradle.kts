// Fabric build, run once per Minecraft version (versions/<minecraft>-fabric).
// Shared settings: buildSrc/src/main/kotlin/galdur.common.gradle.kts

plugins {
    id("galdur.common")
    // Applies the right Fabric Loom: with remapping up to 1.21.x, without from 26.1 (see settings.gradle.kts).
    id("dev.kikugie.loom-back-compat")
}

val mc = sc.current.version
fun prop(name: String): String = sc.properties[name]

sourceSets {
    // Test mod (demo screens): loaded by the dev runs only, never in the jar.
    create("testmod") {
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
    }
}
val testmod = sourceSets["testmod"]

repositories {
    maven {
        // Mod Menu (settings screen in the Mods list)
        url = uri("https://maven.terraformersmc.com/releases/")
        content { includeGroup("com.terraformersmc") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    // Mojang names, like NeoForge, so the shared code compiles unchanged (nothing to do from 26.1:
    // Minecraft ships with its real names).
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    // The theme is a client resource reload listener: Fabric API Resource Loader.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Optional integrations (Mod Menu, JEI, EMI): compiled against their API but neither in the
    // jar nor passed on. Without them installed, their entrypoints are simply never loaded.
    modCompileOnly("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
    modLocalRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
    modCompileOnly("mezz.jei:jei-$mc-common-api:${prop("deps.jei_fabric")}")
    modCompileOnly("mezz.jei:jei-$mc-fabric-api:${prop("deps.jei_fabric")}")
    modLocalRuntime("mezz.jei:jei-$mc-fabric:${prop("deps.jei_fabric")}")
    sc.properties.getOrNull("deps.mezz_config")?.let { modLocalRuntime("net.mezzdev.config:mezz_config-$mc-fabric:$it") }
    sc.properties.getOrNull("deps.emi")?.let {
        modCompileOnly("maven.modrinth:emi:$it+fabric")
        modLocalRuntime("maven.modrinth:emi:$it+fabric")
    }
}

loom {
    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets.main.get())
        }
        register("galduruitest") {
            sourceSet(testmod)
        }
    }
    // Processed by Stonecutter, so it can hold //? comments like the code.
    accessWidenerPath = sc.process(rootProject.file("src/main/resources/galdurui.accesswidener"), "build/processed.accesswidener")

    runs {
        named("client") {
            client()
            // Every dev run loads the library and the test mod.
            source(testmod)
            ideConfigGenerated(true)
            runDir("run")
            // Loads the classes changed by the bridge mixins at startup (internal.bridge.BridgeCheck).
            property("galdurui.checkMixins", "true")
            // -Pgalduruitest.screenshots: screenshot tour of the test mod (testmod/ScreenshotTour).
            if (providers.gradleProperty("galduruitest.screenshots").isPresent) {
                property("galduruitest.screenshots", "true")
            }
        }
        named("server") {
            server()
            runDir("run-server")
        }
    }
}

tasks {
    // Fills the ${...} placeholders of fabric.mod.json. A missing property fails the build.
    processResources {
        val replaceProperties = mapOf(
            "minecraft_version" to prop("mc.fabric_range"),
            "java_version" to prop("mc.java"),
            "mod_id" to prop("mod.id"),
            "mod_name" to prop("mod.name"),
            "mod_license" to prop("mod.license"),
            "mod_version" to prop("mod.version"),
            "mod_authors" to prop("mod.authors"),
            "mod_description" to prop("mod.description"),
            // Without EMI for this version, the integration is not compiled: no entrypoint.
            "emi_entrypoint" to if (sc.properties.getOrNull("deps.emi") != null)
                "\"com.galdurstudio.galdurui.internal.compat.emi.GaldurEmiPlugin\"" else "",
        )
        inputs.properties(replaceProperties)
        filesMatching("fabric.mod.json") {
            expand(replaceProperties)
        }
    }
    // The test resources are NeoForge's (test block, theme pack), except fabric.mod.json.
    named<ProcessResources>(testmod.processResourcesTaskName) {
        exclude("META-INF/**", "assets/**", "data/**", "resourcepacks/**")
    }
}
