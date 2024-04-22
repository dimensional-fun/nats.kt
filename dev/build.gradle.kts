kotlin {
    mingwX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets["commonMain"].dependencies {
        implementation(projects.natsCore)
        implementation(projects.natsCore.transportWs)

        implementation(projects.natsJetstream)
    }

    sourceSets["linuxX64Main"].dependencies() {
        implementation(projects.natsCore.transportTcp)
    }

    sourceSets["mingwX64Main"].dependencies {
        implementation("io.ktor:ktor-client-winhttp:2.3.2")
    }

    sourceSets["jvmMain"].dependencies {
        implementation(projects.natsCore.transportTcp)
        implementation("org.slf4j:slf4j-api:2.0.3")
        implementation("ch.qos.logback:logback-classic:1.4.8")
        implementation("io.nats:jnats:2.16.12")
    }
}
