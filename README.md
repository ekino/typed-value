# Typed-Value - Type-Safe Values for Kotlin

[![GitHub Actions](https://github.com/ekino/typed-value/workflows/Build%20and%20Test/badge.svg)](https://github.com/ekino/typed-value/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.ekino.oss/typed-value-core)](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple)](https://kotlinlang.org/)
[![Multiplatform](https://img.shields.io/badge/Multiplatform-JVM%20|%20JS%20|%20Native-blue)](https://kotlinlang.org/docs/multiplatform.html)

A lightweight Kotlin Multiplatform library providing type-safe wrappers for primitive values, preventing accidental mixing of incompatible values at compile time.

📖 **[Documentation](https://ekino.github.io/typed-value/)** | 🐙 **[GitHub](https://github.com/ekino/typed-value)**

## Features

- **Kotlin Multiplatform**: JVM, JS, and Native support (core module)
- **Flexible Value Types**: String, Long, Int, UUID, or any `Comparable` type
- **Type Safety**: Compile-time prevention of mixing incompatible values
- **Framework Integrations** (JVM-only): Jackson, Spring MVC, QueryDSL, Spring Data Elasticsearch

## Modules

| Module | Description | Platforms |
|--------|-------------|-----------|
| `typed-value-core` | Core TypedValue interface | JVM, JS, Native |
| `typed-value-jackson` | Jackson serialization | JVM |
| `typed-value-spring` | Spring MVC converters | JVM |
| `typed-value-hibernate` | JPA/Hibernate abstract entities & converters | JVM |
| `typed-value-querydsl` | QueryDSL support (expressions + Q-classes for Hibernate entities) | JVM |
| `typed-value-spring-data-elasticsearch` | Elasticsearch mapping | JVM |

## Quick Start

### Installation

```kotlin
dependencies {
  implementation("com.ekino.oss:typed-value-core:0.1.0-SNAPSHOT")
  // Optional integrations
  implementation("com.ekino.oss:typed-value-jackson:0.1.0-SNAPSHOT")
  implementation("com.ekino.oss:typed-value-spring:0.1.0-SNAPSHOT")
}
```

### Basic Usage

```kotlin
import com.ekino.oss.typedvalue.*

// Type-safe identifiers
data class User(val id: TypedString<User>, val name: String)
data class Product(val id: TypedLong<Product>, val price: BigDecimal)

// Type-safe quantities
class Banana
class Apple
val bananas = 5.toTypedInt<Banana>()
val apples = 3.toTypedInt<Apple>()

// Type-safe money
class Cents
val price = 1999L.toTypedLong<Cents>()

// Create using extension functions
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()

// Type safety prevents mixing values
fun deleteUser(id: TypedString<User>) { /* ... */ }
deleteUser(userId)     // ✅ Compiles
deleteUser(productId)  // ❌ Compile error!

fun addBananas(count: TypedInt<Banana>) { /* ... */ }
addBananas(bananas)    // ✅ Compiles
addBananas(apples)     // ❌ Compile error!
```

### Available Types

| Type | Value Type | Platforms | Use Cases |
|------|------------|-----------|-----------|
| `TypedString<T>` | String | All | IDs, emails, codes |
| `TypedInt<T>` | Int | All | Quantities, small counts |
| `TypedLong<T>` | Long | All | Large IDs, money (cents) |
| `TypedUuid<T>` | UUID | JVM only | Distributed IDs |
| `TypedValue<V, T>` | Any Comparable | All | Custom types |

### Java Interop (JVM)

```java
import com.ekino.oss.typedvalue.*;

TypedString<User> userId = TypedValues.typedString("user-123", User.class);
TypedLong<Product> productId = TypedValues.typedLong(42L, Product.class);
TypedUuid<Order> orderId = TypedValues.typedUuid(UUID.randomUUID(), Order.class);
```

## Framework Integrations

### Jackson

```kotlin
@Configuration
class JacksonConfig {
    @Bean
    fun typedValueModule() = TypedValueModule()
}
```

```kotlin
data class UserDto(val id: TypedString<User>, val name: String)

val json = objectMapper.writeValueAsString(UserDto("user-123".toTypedString(), "Alice"))
// {"id":"user-123","name":"Alice"}
```

### Spring MVC

Auto-configured when `typed-value-spring` is on the classpath.

```kotlin
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: TypedString<User>): UserDto {
    return userService.findById(id)
}
```

### Hibernate (JPA)

```kotlin
// Use abstract entity classes for automatic equals/hashCode
@Entity
class User : AbstractUuidEntity<User>(User::class) {
    var name: String? = null
}

// Or use HibernateEntityUtils for custom entities
@Entity
class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var _id: Long? = null

    @get:Transient
    var id: TypedLong<Product>?
        get() = _id?.toTypedLong()
        set(value) { _id = value?.value }

    override fun equals(other: Any?) = HibernateEntityUtils.entityEquals(this, other) { it.id }
    override fun hashCode() = HibernateEntityUtils.entityHashCode(this)
}
```

### QueryDSL

```kotlin
val qUser = QUser.user
val userIdExpr = qUser.typedValueExpressionOf { it._id }

queryFactory.selectFrom(qUser)
    .where(userIdExpr.eq(userId))
    .fetch()
```

### Spring Data Elasticsearch

```kotlin
@Configuration
class ElasticsearchConfig : ElasticsearchConfigurationSupport() {
    @Bean
    override fun elasticsearchMappingContext() = TypedValueElasticsearchMappingContext()
}
```

```kotlin
@Document(indexName = "users")
data class UserDocument(
    val id: TypedString<User>,
    val friendIds: List<TypedString<User>>  // Collections supported (Lists only)
)
```

## Utility Methods

```kotlin
val userId = "user-123".toTypedString<User>()

// Type checking
userId.isAboutType<User>()  // true

// Safe casting
val casted: TypedString<User>? = someId.takeIfAboutType<String, User>()

// Sorting
val sorted = listOf(userId1, userId2, userId3).sorted()
```

## Development

```bash
./gradlew build                    # Build all
./gradlew test                     # Run all tests
./gradlew spotlessApply            # Format code
./gradlew typed-value-core:jvmTest # Test JVM only
./gradlew typed-value-core:jsTest  # Test JS only
```

## License

Copyright © 2025 Ekino
