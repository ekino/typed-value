---
layout: home

hero:
  name: typed-value
  text: Type-safe Entity Identifiers
  tagline: Kotlin Multiplatform library for type-safe IDs that prevent mixing up identifiers at compile time
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: View on GitHub
      link: https://github.com/ekino/typed-value

features:
  - icon: 🛡️
    title: Type Safety
    details: Compile-time prevention of ID mixing. Never accidentally pass a User ID where a Product ID is expected.
  - icon: 🌍
    title: Multiplatform
    details: Full support for JVM, JavaScript, and Native platforms. Share your domain model across all targets.
  - icon: 📦
    title: Zero Dependencies
    details: Core module has no external dependencies. Add only the integrations you need.
  - icon: 🔌
    title: Framework Ready
    details: Out-of-the-box integrations for Jackson, Spring MVC, QueryDSL, JPA, and Elasticsearch.
  - icon: 🔢
    title: Flexible ID Types
    details: Support for String, Long, Int, UUID, and any custom Comparable type as identifiers.
  - icon: ✨
    title: Kotlin-First
    details: Idiomatic Kotlin API with extension functions, reified generics, and full Java interoperability.
---

<script setup>
import { data as v } from './.vitepress/versions.data'
</script>

## The Problem

In any application with multiple entities, mixing up IDs is a common source of bugs:

```kotlin
// Classic bug: passing wrong ID type
fun getOrder(orderId: String): Order { ... }
fun getUser(userId: String): User { ... }

val userId = "user-123"
val order = getOrder(userId) // Compiles, but wrong!
```

## The Solution

With **typed-value**, the compiler catches these mistakes:

```kotlin
fun getOrder(orderId: TypedString<Order>): Order { ... }
fun getUser(userId: TypedString<User>): User { ... }

val userId = "user-123".toTypedString<User>()
val order = getOrder(userId) // Compilation error!
```

## Quick Start

Add the dependency to your `build.gradle.kts`:

::: code-group
```kotlin-vue [Kotlin Multiplatform]
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
            }
        }
    }
}
```

```kotlin-vue [JVM Only]
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
}
```
:::

Then define your typed IDs:

```kotlin
import com.ekino.oss.typedvalue.*

// Define your entities
class User
class Product
class Order

// Create type-safe IDs
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()
val orderId = UUID.randomUUID().toTypedUuid<Order>() // JVM only

// Use in your domain model
data class UserDto(
    val id: TypedString<User>,
    val name: String
)
```

## Module Overview

| Module | Platform | Description |
|--------|----------|-------------|
| `typed-value-core` | JVM, JS, Native | Core TypedValue types with zero dependencies |
| `typed-value-jackson` | JVM | JSON serialization/deserialization |
| `typed-value-spring` | JVM | Spring MVC path variable & request param converters |
| `typed-value-hibernate` | JVM | JPA/Hibernate abstract entities, converters & repository support |
| `typed-value-querydsl` | JVM | Type-safe QueryDSL expressions + Q-classes for Hibernate entities |
| `typed-value-spring-data-elasticsearch` | JVM | Elasticsearch document mapping |

[Get started with the full guide →](/guide/getting-started)
