# NATS - Kotlin Client

An idiomatic & multi-platform [Kotlin](http://java.com) client for the [NATS messaging system](https://nats.io).

> **Warning**  
> nats.kt is currently in active development!

- [Discord Server](https://discord.gg/8R4d8RydT4)

## Installation

Requires **Kotlin 1.9** and **Java 19** if you're using the JVM artifact.

- **Latest Version:** None

### 🐘 Gradle

```kotlin
repositories {
    maven("https://maven.dimensional.fun/releases") // or snapshots
}

dependencies {
    implementation("fun.dimensional:knats-core:{VERSION}")
    implementation("fun.dimensional:knats-jetstream:{VERSION}")
    implementation("fun.dimensional:knats-micro:{VERSION}")
}
```



---

Unless specified otherwise, all files are licensed under the [Apache Version 2.0](/LICENSE) license.
