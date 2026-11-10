// Dev tools, never in the mod. Run from the root: ./gradlew :tools:generateSprites

plugins {
    java
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

// Regenerates the grayscale 9-slice sprites in src/main/resources (colour comes from the theme at
// render time). Run by hand; CI checks that the committed PNGs are up to date.
tasks.register<JavaExec>("generateSprites") {
    group = "galdur"
    description = "Generates the grayscale 9-slice sprites (PNG + .mcmeta), tinted by the theme at render time."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "GenerateSprites"
    workingDir = rootDir
}
