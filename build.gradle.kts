plugins {
    java
    idea
    id("net.neoforged.moddev") version "2.0.107"
}

val minecraftVersion = "1.21.1"
val neoForgeVersion = "21.1.234"
val parchmentVersion = "2024.11.17"
val parchmentMinecraftVersion = "1.21.1"
val jeiVersion = "19.25.0.321"
val patchouliVersion = "1.21.1-92-NEOFORGE"

val modId = "tfc_modern_life"
val modVersion = System.getenv("VERSION") ?: "1.1.2"
val modJavaVersion = "21"

val tfcJar = file("C:/Users/g1739/Desktop/群峦前置源码/1.21/TerraFirmaCraft-4.2.9/[群峦传说] TerraFirmaCraft-NeoForge-1.21.1-4.2.9.jar")
val firmaLifeJar = file("../Firmalife-NeoForge-1.21.1-3.0.14.jar")
val lsoJar = file("../[传说生存] legendarysurvivaloverhaul-1.21.1-2.4.7.2.jar")
val ieJar = file("../[沉浸工程] ImmersiveEngineering-1.21.1-12.4.2-194.jar")

group = "com.jccy.tfcmodernlife"
version = modVersion

base {
    archivesName.set("TFCModernLife-NeoForge-$minecraftVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
}

repositories {
    mavenCentral()
    mavenLocal()
    exclusiveContent {
        forRepository { maven("https://maven.blamejared.com") }
        filter { includeGroup("mezz.jei") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.blamejared.com") }
        filter { includeGroup("vazkii.patchouli") }
    }
    exclusiveContent {
        forRepository { maven("https://www.cursemaven.com") }
        filter { includeGroup("curse.maven") }
    }
}

neoForge {
    version = neoForgeVersion

    parchment {
        minecraftVersion.set(parchmentMinecraftVersion)
        mappingsVersion.set(parchmentVersion)
    }

    runs {
        configureEach {
            jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-ea")
        }
        register("client") {
            client()
            gameDirectory = file("run/client")
        }
        register("server") {
            server()
            gameDirectory = file("run/server")
            programArgument("--nogui")
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    if (tfcJar.exists()) {
        compileOnly(files(tfcJar))
        runtimeOnly(files(tfcJar))
    } else {
        throw GradleException("Missing TFC reference jar: $tfcJar")
    }
    if (firmaLifeJar.exists()) {
        compileOnly(files(firmaLifeJar))
        runtimeOnly(files(firmaLifeJar))
    } else {
        throw GradleException("Missing FirmaLife jar: $firmaLifeJar")
    }
    if (lsoJar.exists()) {
        compileOnly(files(lsoJar))
        runtimeOnly(files(lsoJar))
    }
    if (ieJar.exists()) {
        compileOnly(files(ieJar))
        runtimeOnly(files(ieJar))
    }

    compileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
    compileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:$jeiVersion")
    runtimeOnly("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion")

    implementation("vazkii.patchouli:Patchouli:$patchouliVersion")
    implementation(group = "curse.maven", name = "jade-324717", version = "6853386")
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("assets/tfcelectriccooking/**")
    exclude("assets/firmalife_greenhouse_patch/**")
    exclude("data/tfcelectriccooking/**")
    exclude("data/firmalife_greenhouse_patch/**")
}

tasks.processResources {
    val jarVersion = modVersion
    from(rootDir) {
        include("LICENSE", "DISCLAIMER.md")
        into("META-INF")
    }
    inputs.property("jarVersion", jarVersion)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("file" to mapOf("jarVersion" to jarVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "TFC Modern Life",
            "Implementation-Version" to project.version,
            "Bundle-License" to "All Rights Reserved",
            "Bundle-Disclaimer" to "See META-INF/DISCLAIMER.md"
        )
    }
}
