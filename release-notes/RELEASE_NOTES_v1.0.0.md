# Typed-Value v1.0.0 Release Notes

## First Stable Release

We are excited to announce the first stable release of Typed-Value, a Kotlin Multiplatform library providing type-safe wrappers for primitive values.

## What's New Since v0.1.0-rc.1

### Infrastructure
- Documentation now published at https://ekino.github.io/typed-value/
- Default branch renamed from `master` to `main`

### Dependency Updates
- Kotlin updated to latest stable
- Spring framework dependencies updated
- Hibernate Core updated
- QueryDSL updated to 7.1
- Testing dependencies updated

## Features

### Type-Safe Values
- Prevent accidental mixing of incompatible values at compile time
- Support for identifiers, quantities, money, and any tagged values
- Works with String, Int, Long, UUID, or any `Comparable` type

### Kotlin Multiplatform
- **JVM**: Full support including `TypedUuid`
- **JavaScript**: Browser and Node.js support
- **Native**: iOS, macOS, Linux, Windows support

### Framework Integrations (JVM)
- **Jackson**: JSON serialization/deserialization
- **Spring MVC**: Path variable and request parameter converters
- **Hibernate**: Abstract entity classes and JPA converters
- **QueryDSL**: Type-safe query expressions
- **Spring Data Elasticsearch**: Document mapping support

## Modules

| Module | Platform | Description |
|--------|----------|-------------|
| `typed-value-core` | JVM, JS, Native | Core TypedValue types with zero dependencies |
| `typed-value-jackson` | JVM | JSON serialization/deserialization |
| `typed-value-spring` | JVM | Spring MVC path variable & request param converters |
| `typed-value-hibernate` | JVM | JPA/Hibernate abstract entities & converters |
| `typed-value-querydsl` | JVM | Type-safe QueryDSL expressions |
| `typed-value-spring-data-elasticsearch` | JVM | Elasticsearch document mapping |

## Installation

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.ekino.oss:typed-value-core:1.0.0")
            }
        }
    }
}
```

### JVM Only

```kotlin
dependencies {
    implementation("com.ekino.oss:typed-value-core:1.0.0")

    // Optional integrations
    implementation("com.ekino.oss:typed-value-jackson:1.0.0")
    implementation("com.ekino.oss:typed-value-spring:1.0.0")
    implementation("com.ekino.oss:typed-value-hibernate:1.0.0")
    implementation("com.ekino.oss:typed-value-querydsl:1.0.0")
    implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.0.0")
}
```

## Quick Example

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

// Compiler prevents mixing!
fun findUser(id: TypedString<User>) { /* ... */ }
findUser(userId)    // OK
findUser(productId) // Compile error!
```

## Links

- **Documentation:** https://ekino.github.io/typed-value/
- **GitHub:** https://github.com/ekino/typed-value
- **Maven Central:** https://central.sonatype.com/search?q=com.ekino.oss.typed-value

## Credits

Thank you for using Typed-Value! Report issues at https://github.com/ekino/typed-value/issues
