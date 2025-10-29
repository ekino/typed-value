<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Getting Started

This guide will help you add typed-value to your project and start using type-safe identifiers.

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

### 1. Define Your Entities

First, define the entity classes that will be referenced by your typed IDs:

```kotlin
class User
class Product
class Order
```

::: tip
These can be empty marker classes, data classes, or full entity implementations. The type parameter is only used at compile time.
:::

### 2. Create Typed IDs

Use extension functions to create typed IDs from raw values:

```kotlin
import com.ekino.oss.typedvalue.*

// String IDs
val userId = "user-123".toTypedString<User>()

// Long IDs (e.g., database auto-increment)
val productId = 42L.toTypedLong<Product>()

// Int IDs
val categoryId = 5.toTypedInt<Category>()

// UUID IDs (JVM only)
val orderId = UUID.randomUUID().toTypedUuid<Order>()
```

### 3. Use in Domain Models

Define your data classes with typed IDs:

```kotlin
data class UserDto(
    val id: TypedString<User>,
    val name: String,
    val email: String
)

data class OrderDto(
    val id: TypedUuid<Order>,
    val userId: TypedString<User>,
    val productIds: List<TypedLong<Product>>
)
```

### 4. Enjoy Type Safety

The compiler now prevents mixing up IDs:

```kotlin
fun findUser(id: TypedString<User>): User? { ... }
fun findProduct(id: TypedLong<Product>): Product? { ... }

val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()

findUser(userId)     // OK
findUser(productId)  // Compilation error!
findProduct(userId)  // Compilation error!
```

## Choosing the Right Type

| Type | Use Case | Example |
|------|----------|---------|
| `TypedString<T>` | UUIDs as strings, external IDs, slugs | `"user-abc123"` |
| `TypedLong<T>` | Database auto-increment, large sequences | `1234567890L` |
| `TypedInt<T>` | Small sequences, enum-like IDs | `42` |
| `TypedUuid<T>` | Native UUID support (JVM only) | `UUID.randomUUID()` |

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
