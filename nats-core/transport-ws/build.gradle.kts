kotlin.sourceSets["commonMain"].dependencies {
    implementation(projects.natsCore)

    //
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.websockets)
    implementation("io.ktor:ktor-client-cio:2.3.2")
}