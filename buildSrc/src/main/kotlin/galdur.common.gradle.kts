// What the three loader build scripts (build.<loader>.gradle.kts) share: Java version, the code
// of the other loaders left out, repositories, unit tests and the local Maven publication.
// Applied to each versions/<minecraft>-<loader> project.

plugins {
    `java-library`
    `maven-publish`
    idea
}

val minecraft = project.name.substringBeforeLast('-')
val loader = project.name.substringAfterLast('-')
fun prop(name: String): String = project.property(name).toString()

version = "${prop("mod.version")}+$minecraft"
group = prop("mod.group")

base {
    archivesName = "${prop("mod.id")}-$loader"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(prop("mc.java").toInt())

// All the code is in src/. Each loader has its own packages (com.galdurstudio.galdurui.<loader>,
// api.<loader>, testmod.<loader>): a jar never contains the other loaders' ones.
val otherLoaders = listOf("neoforge", "fabric", "forge") - loader
sourceSets.configureEach {
    java {
        for (other in otherLoaders) {
            exclude(
                "com/galdurstudio/galdurui/$other/**",
                "com/galdurstudio/galdurui/api/$other/**",
                "com/galdurstudio/galdurui/testmod/$other/**",
            )
        }
    }
}

// The EMI integration needs EMI for this Minecraft version (deps.emi in stonecutter.properties.toml).
if (!project.hasProperty("deps.emi")) {
    sourceSets.named("main") {
        java.exclude("com/galdurstudio/galdurui/internal/compat/emi/**")
    }
}

repositories {
    mavenCentral() // JUnit and Gson for the unit tests
    maven {
        // Mods published on Modrinth (Spark, EMI)
        url = uri("https://api.modrinth.com/maven")
        content { includeGroup("maven.modrinth") }
    }
    maven {
        // JEI, for the optional integration (internal.compat.jei), and its config library
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
            includeGroup("net.mezzdev.config")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // All the errors at once when porting to a new Minecraft version (javac stops at 100 by default).
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}

dependencies {
    // Unit tests without Minecraft. Gson: same version as the one shipped with the game.
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.google.code.gson:gson:2.10.1")
    // The theme store logs through Galdur UI's logger (SLF4J, shipped with the game).
    testImplementation("org.slf4j:slf4j-api:2.0.9")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // The tests read files of src/ by their path from the root of the repository.
    workingDir = rootDir
}

// TEMPORARY Maven publication to the local repo/ folder, until Galdur Studio has a Maven repository.
// Coordinates: com.galdurstudio.galdurui:galdurui-<loader>:<mod.version>+<minecraft>
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "${prop("mod.id")}-$loader"
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = rootDir.resolve("repo").toURI()
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
