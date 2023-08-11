val kotlinVersion = "1.9.0"
plugins {
    groovy
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation(kotlin("serialization", version = kotlinVersion))
    implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.21.0")

    implementation(gradleApi())
    implementation(localGroovy())

    /* code generation */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup:kotlinpoet:1.14.2")
}