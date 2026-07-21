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
    maven("https://modmaven.dev/") { content { includeGroup("mekanism") } }
    maven("https://maven.creeperhost.net/release") { content { includeGroup("com.refinedmods") } }
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://cursemaven.com") { content { includeGroup("curse.maven") } }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    "forge"("net.minecraftforge:forge:$minecraft-${common.mod.dep("forge_loader")}")

    compileOnly("org.jgrapht:jgrapht-core:1.5.2")

    when (minecraft) {
        "1.20.1" -> {
            modCompileOnly("noobanidus.mods.lootr:lootr-$minecraft:0.7.28.67.3")
            compileOnly("curse.maven:applied-energistics-2-223794:7148487")
            compileOnly("curse.maven:refined-storage-243076:4844585")
            compileOnly("curse.maven:projecte-226410:4901949")
            compileOnly("curse.maven:mekanism-268560:6552911")
            compileOnly("curse.maven:toms-storage-378609:6418133")
            compileOnly("curse.maven:simple-storage-network-268495:8178993")
            compileOnly("curse.maven:pretty-pipes-376737:4769646")
            compileOnly("curse.maven:game-stages-268655:5790361")
        }
        "1.19.2" -> {
            modCompileOnly("noobanidus.mods.lootr:lootr-1.19:0.4.25.65.36")
            compileOnly("curse.maven:applied-energistics-2-223794:6223556")
            compileOnly("com.refinedmods:refinedstorage:1.11.7")
            compileOnly("mekanism:Mekanism:1.19.2-10.3.9.13")
            compileOnly("maven.modrinth:pretty-pipes:1.13.6")
            compileOnly("net.darkhax.gamestages:GameStages-Forge-1.19.2:11.1.5")
            compileOnly("net.darkhax.bookshelf:Bookshelf-Forge-1.19.2:16.3.20")
            compileOnly("curse.maven:projecte-226410:4860858")
            compileOnly("curse.maven:toms-storage-378609:4439073")
            compileOnly("maven.modrinth:simple-storage-network:1.19.2-1.7.1")
        }
        "1.18.2" -> {
            modCompileOnly("noobanidus.mods.lootr:lootr-1.18.2:0.3.26.65.34")
            compileOnly("curse.maven:applied-energistics-2-223794:4733112")
            compileOnly("com.refinedmods:refinedstorage:1.10.6")
            compileOnly("mekanism:Mekanism:1.18.2-10.2.5.465")
            compileOnly("maven.modrinth:pretty-pipes:1.12.8")
            compileOnly("net.darkhax.gamestages:GameStages-Forge-1.18.2:8.1.3")
            compileOnly("net.darkhax.bookshelf:Bookshelf-Forge-1.18.2:13.3.56")
            compileOnly("curse.maven:projecte-226410:4860857")
            compileOnly("curse.maven:toms-storage-378609:5211203")
            compileOnly("maven.modrinth:simple-storage-network:1.18.2-1.7.1")
        }
        "1.16.5" -> {
            modCompileOnly("noobanidus.mods.lootr:lootr-1.16.5:0.2.18.49.26")
            modCompileOnly("curse.maven:applied-energistics-2-223794:3608871")
            modCompileOnly("curse.maven:refined-storage-243076:3807951")
            modCompileOnly("mekanism:Mekanism:1.16.5-10.1.2.457")
            modCompileOnly("maven.modrinth:pretty-pipes:1.9.8")
            modCompileOnly("net.darkhax.gamestages:GameStages-1.16.5:7.2.8")
            modCompileOnly("net.darkhax.bookshelf:Bookshelf-Forge-1.16.5:10.4.33")
            modCompileOnly("curse.maven:projecte-226410:3736621")
            modCompileOnly("curse.maven:toms-storage-378609:5592595")
            modCompileOnly("maven.modrinth:simple-storage-network:1.16.5-1.5.6")
        }
    }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }
}

loom {
    forge {
        val atFile = if (stonecutter.eval(minecraft, ">=1.17")) "modern.cfg" else "legacy.cfg"
        accessTransformer(file("../../src/main/resources/accesstransformers/$atFile"))
    }
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
    }
}

java {
    withSourcesJar()
    val java = when {
        stonecutter.eval(minecraft, ">=1.20.5") -> JavaVersion.VERSION_21
        stonecutter.eval(minecraft, ">=1.17") -> JavaVersion.VERSION_17
        else -> JavaVersion.VERSION_1_8
    }
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
    val atFile = if (stonecutter.eval(minecraft, ">=1.17")) "modern.cfg" else "legacy.cfg"
    exclude("accesstransformers/**")
    from("../../src/main/resources/accesstransformers/$atFile") {
        into("META-INF")
        rename { "accesstransformer.cfg" }
    }
    properties(listOf("META-INF/mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to common.mod.prop("mc_dep_forgelike"),
        "forge_range" to common.mod.prop("forge_range"),
        "pack_format" to common.mod.prop("pack_format"),
        "data_pack_format" to common.mod.prop("data_pack_format")
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
        mainFile.addJavaVersion(if (stonecutter.eval(minecraft, ">=1.20.5")) "Java 21" else "Java 17")
        mainFile.addGameVersion("Client", "Server")
    }
}

tasks.register("publishMod") {
    group = "publishing"
    description = "Publish this loader/version to Modrinth + CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
