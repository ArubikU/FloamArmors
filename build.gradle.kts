import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.freefair.lombok") version "8.6"
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.5"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "com.floamyarmor"
version = "1.1.2a"
description = "A Minecraft plugin for custom armor management"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")

}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    reobfJar {
        dependsOn("build")
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    dependencies {
        include(dependency("commons-io:commons-io:2.11.0"))
        include(dependency("com.fasterxml.jackson.core:jackson-databind:2.15.2"))
        include(dependency("com.fasterxml.jackson.core:jackson-core:2.15.2"))
        include(dependency("com.fasterxml.jackson.core:jackson-annotations:2.15.2"))

    }
    relocate("dev.jorel.commandapi", "com.floamyarmor.libs.commandapi")
    relocate("org.apache.commons", "com.floamyarmor.libs.commons")
    relocate("com.fasterxml.jackson", "com.floamyarmor.libs.jackson")
}

bukkitPluginYaml {
    main = "dev.arubiku.floamyarmor.FloamyArmor"
    apiVersion = "1.21"
    authors.add("ArubikU")
    commands {
        register("floamyarmor") {
            description = "FloamyArmor main command"
            permission = "floamyarmor.use"
        }
    }
    loadBefore = listOf("Nexo","ItemsAdder")
    permissions {
        register("floamyarmor.use") {
            description = "Allows use of FloamyArmor commands"
        }
    }
}

