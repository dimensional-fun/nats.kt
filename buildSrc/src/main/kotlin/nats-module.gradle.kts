plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi()

    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = "19"
        }
    }

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }

    linuxX64()

    sourceSets["commonMain"].dependencies {
        implementation("io.ktor:ktor-io:2.3.10")
        implementation("io.ktor:ktor-http:2.3.10")

        implementation("io.github.oshai:kotlin-logging:6.0.9")

        implementation("naibu.stdlib:naibu-io:1.4-RC.8")
        implementation("naibu.stdlib:naibu-core:1.4-RC.8")
        implementation("naibu.stdlib:naibu-ktor-io:1.4-RC.8")

        implementation("fun.dimensional:kyuso:1.1.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
        implementation("org.jetbrains.kotlinx:atomicfu:0.24.0")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    }
}

tasks {
    val jvmMainClasses by named("jvmMainClasses") {
        dependsOn("compileJava")
    }

    val jvmTestClasses by named("jvmTestClasses") {
        dependsOn("compileJava")
    }
}