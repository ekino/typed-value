---
layout: home

hero:
  name: Typed-Value
  text: Type-safe Values
  tagline: Kotlin Multiplatform library that prevents mixing incompatible values at compile time
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
    details: Compile-time prevention of value mixing. Never accidentally pass a User ID where a Product ID is expected, or mix up quantities.
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
    title: Flexible Value Types
    details: Support for String, Long, Int, UUID, and any custom Comparable type. Use for IDs, quantities, money, measurements, and more.
  - icon: ✨
    title: Kotlin-First
    details: Idiomatic Kotlin API with extension functions, reified generics, and full Java interoperability.
---

<script setup>
import { data as v } from './.vitepress/versions.data'
</script>

## The Problem

In any application, mixing up values of the same primitive type is a common source of bugs:

```kotlin
// Classic bug: passing wrong value
fun getOrder(orderId: String): Order { ... }
fun getUser(userId: String): User { ... }

val userId = "user-123"
val order = getOrder(userId) // Compiles, but wrong!

// Another classic bug: mixing quantities
fun addToCart(productId: Long, quantity: Int) { ... }
addToCart(quantity = 5, productId = 42) // Oops, swapped arguments!
```

## The Solution

With **Typed-Value**, the compiler catches these mistakes:

```kotlin
// Type-safe identifiers
fun getOrder(orderId: TypedString<Order>): Order { ... }
fun getUser(userId: TypedString<User>): User { ... }

val userId = "user-123".toTypedString<User>()
val order = getOrder(userId) // Compilation error!

// Type-safe quantities
class Banana
class Apple
val bananas = 5.toTypedInt<Banana>()
val apples = 3.toTypedInt<Apple>()

fun addBananas(count: TypedInt<Banana>) { ... }
addBananas(apples) // Compilation error!
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

Then define your typed values:

```kotlin
import com.ekino.oss.typedvalue.*

// Type-safe identifiers
class User
class Product
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()

// Type-safe quantities
class Banana
class Apple
val bananas = 5.toTypedInt<Banana>()
val apples = 3.toTypedInt<Apple>()

// Type-safe money
class Cents
val price = 1999L.toTypedLong<Cents>()

// Use in your domain model
data class CartItem(
    val productId: TypedLong<Product>,
    val quantity: TypedInt<Product>,
    val priceInCents: TypedLong<Cents>
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
