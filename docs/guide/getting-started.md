<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Getting Started

This guide will help you add typed-value to your project and start using type-safe values.

## Installation

### Kotlin Multiplatform

For multiplatform projects targeting JVM, JS, or Native:

```kotlin-vue
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

### JVM Only (Gradle Kotlin DSL)

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
}
```

### JVM Only (Gradle Groovy)

```groovy-vue
dependencies {
    implementation 'com.ekino.oss:typed-value-core:{{ v.typedValue }}'
}
```

### Maven

```xml-vue
<dependency>
    <groupId>com.ekino.oss</groupId>
    <artifactId>typed-value-core</artifactId>
    <version>{{ v.typedValue }}</version>
</dependency>
```

## Basic Usage

### 1. Define Your Types

First, define the marker classes that will tag your values:

```kotlin
// For identifiers
class User
class Product
class Order

// For quantities
class Banana
class Apple

// For money
class Cents
class Euros
```

::: tip
These can be empty marker classes, data classes, or full entity implementations. The type parameter is only used at compile time for type checking.
:::

### 2. Create Typed Values

Use extension functions to create typed values from raw primitives:

```kotlin
import com.ekino.oss.typedvalue.*

// Type-safe identifiers
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()
val orderId = UUID.randomUUID().toTypedUuid<Order>() // JVM only

// Type-safe quantities
val bananas = 5.toTypedInt<Banana>()
val apples = 3.toTypedInt<Apple>()

// Type-safe money
val priceInCents = 1999L.toTypedLong<Cents>()
```

### 3. Use in Domain Models

Define your data classes with typed values:

```kotlin
data class UserDto(
    val id: TypedString<User>,
    val name: String,
    val email: String
)

data class CartItem(
    val productId: TypedLong<Product>,
    val quantity: TypedInt<Product>,
    val priceInCents: TypedLong<Cents>
)

data class FruitBasket(
    val bananas: TypedInt<Banana>,
    val apples: TypedInt<Apple>
)
```

### 4. Enjoy Type Safety

The compiler now prevents mixing up values:

```kotlin
// Can't mix different IDs
fun findUser(id: TypedString<User>): User? { ... }
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()

findUser(userId)     // ✅ OK
findUser(productId)  // ❌ Compilation error!

// Can't mix different quantities
fun addBananas(count: TypedInt<Banana>) { ... }
val bananas = 5.toTypedInt<Banana>()
val apples = 3.toTypedInt<Apple>()

addBananas(bananas)  // ✅ OK
addBananas(apples)   // ❌ Compilation error!
```

## Choosing the Right Type

| Type | Value Type | Use Cases | Example |
|------|------------|-----------|---------|
| `TypedString<T>` | String | IDs, emails, codes, slugs | `"user-abc123"` |
| `TypedLong<T>` | Long | Large IDs, money (cents), timestamps | `1234567890L` |
| `TypedInt<T>` | Int | Quantities, small counts, indexes | `42` |
| `TypedUuid<T>` | UUID | Distributed IDs (JVM only) | `UUID.randomUUID()` |

## Adding Framework Integrations

For full-stack applications, add the integration modules you need:

```kotlin-vue
dependencies {
    // Core (required)
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")

    // JSON serialization
    implementation("com.ekino.oss:typed-value-jackson:{{ v.typedValue }}")

    // Spring MVC support
    implementation("com.ekino.oss:typed-value-spring:{{ v.typedValue }}")

    // QueryDSL support
    implementation("com.ekino.oss:typed-value-querydsl:{{ v.typedValue }}")

    // Hibernate/JPA support
    implementation("com.ekino.oss:typed-value-hibernate:{{ v.typedValue }}")

    // Elasticsearch support
    implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:{{ v.typedValue }}")
}
```

See the [Integrations](/integrations/jackson) section for detailed setup instructions.

## Next Steps

- [Core Concepts](/guide/core-concepts) - Understand how TypedValue works
- [Convenience Types](/guide/convenience-types) - Deep dive into TypedString, TypedInt, TypedLong, TypedUuid
- [Platform Support](/platforms/jvm) - Platform-specific features and limitations
