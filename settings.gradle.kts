pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.2"
}

stonecutter {
    create(rootProject) {
        versions("1.19.2", "1.19.4", "1.20.4", "1.20.6", "1.21.4", "1.21.11")
        vcsVersion = "1.21.11"
    }
}

