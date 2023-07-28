kotlin.sourceSets["commonMain"].dependencies {
    implementation(projects.natsCore)

    //
    implementation(libs.ktor.network)
    implementation(libs.ktor.network.tls)
}
