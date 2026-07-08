@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow")
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
}

val loader = prop("loom.platform")!!
val minecraft: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "$minecraft-${mod.version}-$loader"
base {
    archivesName.set(mod.id)
}
architectury {
    platformSetupLoomIde()
    forge()
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    get("developmentForge").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.minecraftforge.net")
    mavenCentral()
    maven("https://maven.blamejared.com")
    maven("https://cursemaven.com") { content { includeGroup("curse.maven") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    "forge"("net.minecraftforge:forge:$minecraft-${common.mod.dep("forge_loader")}")

    compileOnly("org.jgrapht:jgrapht-core:1.5.2")
    modCompileOnly("noobanidus.mods.lootr:lootr-$minecraft:0.7.28.67.3")
    compileOnly("curse.maven:applied-energistics-2-223794:7148487")
    compileOnly("curse.maven:refined-storage-243076:4844585")
    compileOnly("curse.maven:projecte-226410:4901949")
    compileOnly("curse.maven:mekanism-268560:6552911")
    compileOnly("curse.maven:toms-storage-378609:6418133")
    compileOnly("curse.maven:simple-storage-network-268495:8178993")
    compileOnly("curse.maven:pretty-pipes-376737:4769646")
    compileOnly("curse.maven:game-stages-268655:5790361")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }
}

loom {
    forge {
        accessTransformer(file("../../src/main/resources/META-INF/accesstransformer.cfg"))
    }
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(minecraft, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.remapJar {
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("fabric.mod.json", "architectury.common.json")
}

tasks.processResources {
    properties(listOf("META-INF/mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to common.mod.prop("mc_dep_forgelike")
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

modrinth {
    token.set(providers.environmentVariable("MODRINTH_TOKEN"))
    projectId.set(providers.gradleProperty("publish.modrinth_id"))
    versionNumber.set("${mod.version}+$minecraft-$loader")
    versionName.set("${mod.name} ${mod.version} ($loader $minecraft)")
    versionType.set("release")
    uploadFile.set(tasks.named("remapJar"))
    gameVersions.add(minecraft)
    loaders.add(loader)
    detectLoaders.set(false)
    val changelogFile = rootProject.file("CHANGELOG.md")
    if (changelogFile.exists()) changelog.set(changelogFile.readText())
}

tasks.named("modrinth") {
    onlyIf {
        providers.environmentVariable("MODRINTH_TOKEN").isPresent &&
            providers.gradleProperty("publish.modrinth_id").orNull?.isNotBlank() == true
    }
}

tasks.register<net.darkhax.curseforgegradle.TaskPublishCurseForge>("publishCurseForge") {
    group = "publishing"
    apiToken = providers.environmentVariable("CURSEFORGE_TOKEN").orNull
    val curseId = providers.gradleProperty("publish.curseforge_id").orNull?.takeIf { it.toLongOrNull() != null }
    onlyIf { apiToken != null && curseId != null }
    if (curseId != null) {
        val mainFile = upload(curseId, tasks.named("remapJar"))
        mainFile.releaseType = "release"
        mainFile.changelogType = "markdown"
        val changelogFile = rootProject.file("CHANGELOG.md")
        mainFile.changelog = if (changelogFile.exists()) changelogFile.readText() else "No changelog provided."
        mainFile.addGameVersion(minecraft)
        mainFile.addModLoader(when (loader) {
            "fabric" -> "Fabric"
            "forge" -> "Forge"
            "neoforge" -> "NeoForge"
            else -> loader.replaceFirstChar { it.uppercase() }
        })
    }
}

tasks.register("publishMod") {
    group = "publishing"
    description = "Publish this loader/version to Modrinth + CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
