<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# JVM Platform

The JVM platform provides the most complete feature set, including UUID support, full reflection, and all framework integrations.

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
}
```

## Available Types

All typed ID types are available on JVM:

| Type | ID Type | Description |
|------|---------|-------------|
| `TypedString<T>` | `String` | String identifiers |
| `TypedLong<T>` | `Long` | Long integer identifiers |
| `TypedInt<T>` | `Int` | Integer identifiers |
| `TypedUuid<T>` | `java.util.UUID` | Native UUID identifiers |

## TypedUuid (JVM Exclusive)

`TypedUuid` is only available on JVM as it uses `java.util.UUID`:

```kotlin
import com.ekino.oss.typedvalue.TypedUuid
import java.util.UUID

class Order

// From random UUID
val orderId = UUID.randomUUID().toTypedUuid<Order>()

// From string
val orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    .toTypedUuid<Order>()

// Factory method
val orderId = TypedUuid.of(UUID.randomUUID(), Order::class)
```

---

## Java Interoperability

### TypedValues Utility Class

The `TypedValues` utility class provides static factory methods for Java code. It's defined in `TypedValues.kt` with `@file:JvmName("TypedValues")`.

#### API

| Method | Signature | Description |
|--------|-----------|-------------|
| `typedString` | `<T> typedString(value: String, type: Class<T>): TypedString<T>` | Create TypedString |
| `typedLong` | `<T> typedLong(value: Long, type: Class<T>): TypedLong<T>` | Create TypedLong |
| `typedInt` | `<T> typedInt(value: Int, type: Class<T>): TypedInt<T>` | Create TypedInt |
| `typedUuid` | `<T> typedUuid(value: UUID, type: Class<T>): TypedUuid<T>` | Create TypedUuid |
| `typedValue` | `<ID, T> typedValue(value: ID, type: Class<T>): TypedValue<ID, T>` | Create generic TypedValue |

#### Java Usage

```java
import com.ekino.oss.typedvalue.TypedValues;
import com.ekino.oss.typedvalue.TypedString;
import com.ekino.oss.typedvalue.TypedLong;
import com.ekino.oss.typedvalue.TypedUuid;

public class UserService {

    public User findUser(TypedString<User> userId) {
        // ...
    }

    public void example() {
        // Create typed IDs from Java
        TypedString<User> userId = TypedValues.typedString("user-123", User.class);
        TypedLong<Product> productId = TypedValues.typedLong(42L, Product.class);
        TypedInt<Category> categoryId = TypedValues.typedInt(5, Category.class);
        TypedUuid<Order> orderId = TypedValues.typedUuid(UUID.randomUUID(), Order.class);

        // Access raw values
        String rawUserId = userId.getValue();
        Long rawProductId = productId.getValue();
    }
}
```

#### Java Records

```java
public record UserDto(
    TypedString<User> id,
    String name,
    String email
) {}

// Create instance
var user = new UserDto(
    TypedValues.typedString("u-123", User.class),
    "Alice",
    "alice@example.com"
);
```

#### Java with Lombok

```java
@Data
@AllArgsConstructor
public class OrderDto {
    private TypedUuid<Order> id;
    private TypedString<User> userId;
    private List<TypedLong<Product>> productIds;
}
```

---

## Full Reflection Support

On JVM, full Kotlin reflection is available:

```kotlin
val userId = "u-123".toTypedString<User>()

// These work on JVM
println(userId.type.qualifiedName) // "com.example.User"
println(userId.type.simpleName)    // "User"
```

::: warning
`qualifiedName` and `simpleName` are JVM-only. Don't use them in multiplatform `commonMain` code.
:::

---

## Class to KClass Conversion

When working with framework APIs that provide `Class<T>`, convert to `KClass<T>`:

```kotlin
// Framework gives you Class<*>
val entityClass: Class<*> = frameworkApi.getType()

// Convert to KClass for typed-value APIs
val entityKClass: KClass<*> = entityClass.kotlin

// Create TypedValue
val typedId = TypedValue(rawId, entityKClass)
```

This pattern is used internally by all integration modules (Jackson, Spring, QueryDSL, JPA, Elasticsearch).

---

## Framework Integrations

All framework integration modules are JVM-only:

| Module | Description | Documentation |
|--------|-------------|---------------|
| `typed-value-jackson` | JSON serialization | [Jackson](/integrations/jackson) |
| `typed-value-spring` | Spring MVC support | [Spring](/integrations/spring) |
| `typed-value-querydsl` | QueryDSL expressions | [QueryDSL](/integrations/querydsl) |
| `typed-value-hibernate` | Hibernate/JPA persistence | [Hibernate](/integrations/hibernate) |
| `typed-value-spring-data-elasticsearch` | Elasticsearch mapping | [Elasticsearch](/integrations/elasticsearch) |

### Installation (All Integrations)

```kotlin-vue
dependencies {
    // Core (required)
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")

    // Choose the integrations you need
    implementation("com.ekino.oss:typed-value-jackson:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-querydsl:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-hibernate:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:{{ v.typedValue }}")
}
```

---

## Performance Considerations

- TypedValue instances are immutable and lightweight
- No reflection is performed at runtime for basic operations
- Type information is stored as KClass reference (not string)
- Safe to use in hot paths
- Convenience types add no overhead over generic TypedValue

## Next Steps

- [JavaScript Platform](/platforms/javascript) - JS-specific features
- [Native Platform](/platforms/native) - Native platform support
- [Jackson Integration](/integrations/jackson) - JSON support
- [Spring Integration](/integrations/spring) - Web framework support
