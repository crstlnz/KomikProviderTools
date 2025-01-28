import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "com.crstlnz.komikchino"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        // Disables some unnecessary features
        freeCompilerArgs.addAll(
            listOf(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
            )
        )

        jvmTarget.set(JvmTarget.JVM_11)  // Required
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}


dependencies {
    implementation(kotlin("stdlib", "2.1.0"))
    compileOnly(gradleApi())

    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("com.android.tools:sdk-common:30.0.0")
    compileOnly("com.android.tools.build:gradle:8.7.3")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")

    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("com.github.vidstige:jadb:master-SNAPSHOT")
}

gradlePlugin {
    plugins {
        create("com.crstlnz.komikchino.plugintools") {
            id = "com.crstlnz.komikchino.plugintools"
            implementationClass = "com.crstlnz.komikchino.plugintools.KomikPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
        val token = System.getenv("GITHUB_TOKEN")
        if (token != null) {
            maven {
                credentials {
                    username = "crstlnz"
                    password = token
                }
                setUrl("https://maven.pkg.github.com/crstlnz/komikchino")
            }
        }
    }
}
