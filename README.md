# NATS - Kotlin Client

An idiomatic & multi-platform [Kotlin](http://java.com) client for the [NATS messaging system](https://nats.io).

> [!WARNING]
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

## Example

### Creating a Client

```kt
// create and connect a new NATS client.
val client = Client("nats://127.0.0.1:4222") {
    // use the TCP transport, you could also use the WebSocket transport.
    transport = TcpTransport
}
```

### Subscriptions

```kt
// create a subscription listening on all subjects.
val all = client.subscribe(">")

all.on<SubscriptionUnsubscribedEvent> {
    println("Subscription was ${if (auto) "auto-" else ""}unsubscribed.")
}

all.on<SubscriptionDeliveryEvent> {
    println("Received message $id on \"${delivery.subject}\"!")
}
```

_more coming soon_

---

Unless specified otherwise, all files are licensed under the [Apache Version 2.0](/LICENSE) license.
