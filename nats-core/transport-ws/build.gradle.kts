kotlin {
//    mingwX64()

    sourceSets["commonMain"].dependencies {
        api(projects.natsCore)
        api(libs.ktor.client.core)
        api(libs.ktor.client.websockets)
    }
}
