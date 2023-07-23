allprojects {
    group   = "fun.dimensional.nats"
    version = "1.0"

    repositories {
        maven("https://maven.dimensional.fun/releases")
        maven("https://maven.dimensional.fun/snapshots")
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "nats-module")
}
