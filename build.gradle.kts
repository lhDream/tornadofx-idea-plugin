import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    kotlin("jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "no.tornado"
version = "1.7.20.1"

val publishUsername: String by rootProject.extra
val publishPassword: String by rootProject.extra

repositories {
    mavenCentral()
}

intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("java", "properties", "Kotlin", "com.intellij.javafx:1.0.3"))
}

tasks {
    patchPluginXml {
        version.set(project.version.toString())
        sinceBuild.set("203")
        untilBuild.set("")
     }

     publishPlugin {
//        username(publishUsername)
//        password(publishPassword)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    runIde {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }

    buildSearchableOptions {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

