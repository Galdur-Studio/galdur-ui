// Forge build from 1.20.6, when Forge started running mods with Mojang names (versions/<minecraft>-forge).
// Older versions: build.legacyforge.gradle.kts.
// Shared settings: buildSrc/src/main/kotlin/galdur.common.gradle.kts

plugins {
    id("galdur.common")
    id("net.minecraftforge.gradle") version "[7.0.3,8)"
}

val mc = sc.current.version
fun prop(name: String): String = sc.properties[name]

minecraft {
    // Mojang names, like NeoForge and Fabric, so the shared code compiles unchanged.
    mappings("official", mc)

    runs {
        configureEach {
            workingDir = layout.projectDirectory.dir("run")
            // Mixins of the bridge; in production Forge reads them from the jar manifest.
            args("--mixin.config", "galdurui.mixins.json")
            // Loads the classes changed by the bridge mixins at startup (internal.bridge.BridgeCheck).
            jvmArgs("-Dgaldurui.checkMixins=true")
        }
        register("client")
        register("server") {
            args("--nogui")
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:$mc-${prop("deps.forge")}"))

    // Optional JEI and EMI integrations (internal.compat): compiled against their API but neither
    // in the jar nor passed on. Without them installed, their plugins are simply never loaded.
    // EMI has no Forge version for 1.21.1: its API is the same in all loaders and uses vanilla
    // classes only, so the NeoForge jar is enough to compile (deps.emi_forge_variant).
    compileOnly("mezz.jei:jei-$mc-common-api:${prop("deps.jei")}")
    compileOnly("mezz.jei:jei-$mc-forge-api:${prop("deps.jei")}")
    runtimeOnly("mezz.jei:jei-$mc-forge:${prop("deps.jei")}") { isTransitive = false }
    compileOnly("maven.modrinth:emi:${prop("deps.emi")}+${prop("deps.emi_forge_variant")}")
}

// Fills the ${...} placeholders of src/main/templates/forge (mods.toml).
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

tasks.processResources {
    // Fabric metadata.
    exclude("fabric.mod.json", "galdurui.accesswidener")
}

tasks.jar {
    manifest.attributes("MixinConfigs" to "galdurui.mixins.json")
}
