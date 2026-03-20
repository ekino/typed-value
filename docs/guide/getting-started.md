<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Getting Started

This guide will help you add typed-value to your project and start using type-safe values.

## Installation

### Using the BOM (recommended) {#bom}

The BOM (Bill of Materials) lets you declare a single versioned dependency and omit versions on individual modules:

::: code-group
```kotlin-vue [Gradle Kotlin DSL]
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:{{ v.typedValue }}"))
    implementation("com.ekino.oss:typed-value-core")
    // Add any integration modules without versions
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
}
```

```groovy-vue [Gradle Groovy]
dependencies {
    implementation platform('com.ekino.oss:typed-value-bom:{{ v.typedValue }}')
    implementation 'com.ekino.oss:typed-value-core'
    implementation 'com.ekino.oss:typed-value-jackson'
}
```

```xml-vue [Maven]
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.ekino.oss</groupId>
            <artifactId>typed-value-bom</artifactId>
            <version>{{ v.typedValue }}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.ekino.oss</groupId>
        <artifactId>typed-value-core</artifactId>
    </dependency>
</dependencies>
```
:::

### Without the BOM

::: code-group
```kotlin-vue [Gradle Kotlin DSL]
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-jackson:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring:{{ v.typedValue }}")
}
```

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

```groovy-vue [Gradle Groovy]
dependencies {
    implementation 'com.ekino.oss:typed-value-core:{{ v.typedValue }}'
    implementation 'com.ekino.oss:typed-value-jackson:{{ v.typedValue }}'
    implementation 'com.ekino.oss:typed-value-spring:{{ v.typedValue }}'
}
```

```xml-vue [Maven]
<dependencies>
    <dependency>
        <groupId>com.ekino.oss</groupId>
        <artifactId>typed-value-core</artifactId>
        <version>{{ v.typedValue }}</version>
    </dependency>
    <dependency>
        <groupId>com.ekino.oss</groupId>
        <artifactId>typed-value-jackson</artifactId>
        <version>{{ v.typedValue }}</version>
    </dependency>
    <dependency>
        <groupId>com.ekino.oss</groupId>
        <artifactId>typed-value-spring</artifactId>
        <version>{{ v.typedValue }}</version>
    </dependency>
</dependencies>
```
:::

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

For full-stack applications, add the integration modules you need. Using the [BOM](#bom) is the recommended approach:

::: code-group
```kotlin-vue [With BOM (recommended)]
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:{{ v.typedValue }}"))

    // Core (required)
    implementation("com.ekino.oss:typed-value-core")

    // JSON serialization
    implementation("com.ekino.oss:typed-value-jackson")

    // Spring MVC support
    implementation("com.ekino.oss:typed-value-spring")

    // QueryDSL support
    implementation("com.ekino.oss:typed-value-querydsl")

    // Hibernate/JPA support
    implementation("com.ekino.oss:typed-value-hibernate")

    // Elasticsearch support
    implementation("com.ekino.oss:typed-value-spring-data-elasticsearch")
}
```

```kotlin-vue [Without BOM]
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
:::

See the [Integrations](/integrations/jackson) section for detailed setup instructions.

## Next Steps

- [Core Concepts](/guide/core-concepts) - Understand how TypedValue works
- [Convenience Types](/guide/convenience-types) - Deep dive into TypedString, TypedInt, TypedLong, TypedUuid
- [Platform Support](/platforms/jvm) - Platform-specific features and limitations
