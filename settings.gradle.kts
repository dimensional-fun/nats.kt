enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":nats-core")
include(":nats-core:transport-tcp")
include(":nats-core:transport-ws")
include(":nats-jetstream")
include(":nats-micro")

include(":dev")

rootProject.name = "nats"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            ktor()
            common()
        }
    }
}

fun VersionCatalogBuilder.ktor() {
    val ktor = version("ktor", "2.3.2")

    library("ktor-network",     "io.ktor", "ktor-network").versionRef(ktor)
    library("ktor-network-tls", "io.ktor", "ktor-network-tls").versionRef(ktor)
    library("ktor-io",          "io.ktor", "ktor-io").versionRef(ktor)
    library("ktor-http",        "io.ktor", "ktor-http").versionRef(ktor)

    library("ktor-client-core",       "io.ktor", "ktor-client-core").versionRef(ktor)
    library("ktor-client-websockets", "io.ktor", "ktor-client-websockets").versionRef(ktor)
}

fun VersionCatalogBuilder.common() {
    /* naibu */
    val naibu = version("naibu", "1.1-RC.2")

    library("naibu-core",    "naibu.stdlib", "naibu-core").versionRef(naibu)
    library("naibu-io",      "naibu.stdlib", "naibu-io").versionRef(naibu)
    library("naibu-ktor-io", "naibu.stdlib", "naibu-ktor-io").versionRef(naibu)

    /* kotlin */
    library("kotlin-logging", "io.github.oshai", "kotlin-logging").version("5.0.0")

    /* coroutines */
    library("kx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.7.1")

    library("kyuso", "fun.dimensional", "kyuso").version("1.1.0")

    /* coroutines */
    library("kx-serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.5.1")
}
