buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}

plugins {
    val kotlinVersion = "1.9.20"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion apply false
}

tasks.register("clean") {
    delete(rootProject.buildDir)
    subprojects {
        delete(buildDir)
    }
}