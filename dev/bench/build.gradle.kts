kotlin.sourceSets["commonMain"].dependencies {
    implementation(projects.natsCore)
    implementation(projects.natsCore.transportTcp)
}

kotlin.sourceSets["jvmMain"].dependencies {
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("io.nats:jnats:2.16.12")
}