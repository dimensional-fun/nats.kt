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
        implementation("io.ktor:ktor-io:2.3.2")
        implementation("io.ktor:ktor-http:2.3.2")

        implementation("io.github.oshai:kotlin-logging:5.0.0")

        implementation("naibu.stdlib:naibu-io:1.3-RC.2")
        implementation("naibu.stdlib:naibu-core:1.3-RC.2")
        implementation("naibu.stdlib:naibu-ktor-io:1.3-RC.2")

        implementation("fun.dimensional:kyuso:1.1.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
        implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
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