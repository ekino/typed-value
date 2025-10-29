<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Convenience Types

typed-value provides specialized classes for the most common ID types, offering cleaner type signatures and dedicated factory methods.

## Platform Availability

| Type | JVM | JavaScript | Native |
|------|:---:|:----------:|:------:|
| `TypedString<T>` | Yes | Yes | Yes |
| `TypedInt<T>` | Yes | Yes | Yes |
| `TypedLong<T>` | Yes | Yes* | Yes |
| `TypedUuid<T>` | Yes | No | No |

*JavaScript has precision limitations for Long values > 2^53

---

## TypedString

For string-based identifiers (UUIDs as strings, slugs, external IDs).

### Class Definition

```kotlin
open class TypedString<T : Any>(value: String, type: KClass<out T>) :
  TypedValue<String, T>(value, type)
```

### API

| Method | Signature | Description |
|--------|-----------|-------------|
| `TypedString.of()` | `of<T>(value: String, type: KClass<T>): TypedString<T>` | Factory method |
| `String.toTypedString()` | `inline fun <reified T> String.toTypedString(): TypedString<T>` | Extension function |

### Usage

```kotlin
import com.ekino.oss.typedvalue.TypedString

class User

// Create with extension function (recommended)
val userId = "user-abc123".toTypedString<User>()

// Create with factory method
val userId = TypedString.of("user-abc123", User::class)

// Access the raw value
val raw: String = userId.value // "user-abc123"
```

---

## TypedLong

For long integer identifiers (database auto-increment, large sequences).

### Class Definition

```kotlin
open class TypedLong<T : Any>(value: Long, type: KClass<out T>) :
  TypedValue<Long, T>(value, type)
```

### API

| Method | Signature | Description |
|--------|-----------|-------------|
| `TypedLong.of()` | `of<T>(value: Long, type: KClass<T>): TypedLong<T>` | Factory method |
| `Long.toTypedLong()` | `inline fun <reified T> Long.toTypedLong(): TypedLong<T>` | Extension function |

### Usage

```kotlin
import com.ekino.oss.typedvalue.TypedLong

class Product

// Create with extension function
val productId = 1234567890L.toTypedLong<Product>()

// Create with factory method
val productId = TypedLong.of(1234567890L, Product::class)

// Access the raw value
val raw: Long = productId.value // 1234567890L
```

::: warning JavaScript Precision
JavaScript uses 64-bit floating-point numbers. Long values larger than `2^53 - 1` (9,007,199,254,740,991) may lose precision on the JS platform. For IDs that may exceed this range, use `TypedString` instead.
:::

---

## TypedInt

For integer identifiers (small sequences, enum-like IDs).

### Class Definition

```kotlin
open class TypedInt<T : Any>(value: Int, type: KClass<out T>) :
  TypedValue<Int, T>(value, type)
```

### API

| Method | Signature | Description |
|--------|-----------|-------------|
| `TypedInt.of()` | `of<T>(value: Int, type: KClass<T>): TypedInt<T>` | Factory method |
| `Int.toTypedInt()` | `inline fun <reified T> Int.toTypedInt(): TypedInt<T>` | Extension function |

### Usage

```kotlin
import com.ekino.oss.typedvalue.TypedInt

class Category

// Create with extension function
val categoryId = 42.toTypedInt<Category>()

// Create with factory method
val categoryId = TypedInt.of(42, Category::class)

// Access the raw value
val raw: Int = categoryId.value // 42
```

---

## TypedUuid

For native UUID identifiers (distributed systems, guaranteed uniqueness).

::: tip JVM Only
`TypedUuid` is only available on JVM as it uses `java.util.UUID`. For multiplatform projects, use `TypedString` with UUID strings.
:::

### Class Definition

```kotlin
open class TypedUuid<T : Any>(value: UUID, type: KClass<out T>) :
  TypedValue<UUID, T>(value, type)
```

### API

| Method | Signature | Description |
|--------|-----------|-------------|
| `TypedUuid.of()` | `of<T>(value: UUID, type: KClass<T>): TypedUuid<T>` | Factory method |
| `UUID.toTypedUuid()` | `inline fun <reified T> UUID.toTypedUuid(): TypedUuid<T>` | Extension function |

### Usage

```kotlin
import com.ekino.oss.typedvalue.TypedUuid
import java.util.UUID

class Order

// Create from random UUID
val orderId = UUID.randomUUID().toTypedUuid<Order>()

// Create with factory method
val orderId = TypedUuid.of(UUID.randomUUID(), Order::class)

// Create from string
val orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    .toTypedUuid<Order>()

// Access the raw value
val raw: UUID = orderId.value
```

---

## Kotlin vs Java API

### Kotlin (Recommended)

Use extension functions with reified generics for the cleanest API:

```kotlin
// Extension functions with reified types
val userId = "u-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()
val categoryId = 5.toTypedInt<Category>()
val orderId = UUID.randomUUID().toTypedUuid<Order>()
```

### Java

For Java code, use the `TypedValues` utility class (see [JVM Platform](/platforms/jvm) for details):

```java
import com.ekino.oss.typedvalue.TypedValues;

// Static factory methods with Class parameter
TypedString<User> userId = TypedValues.typedString("u-123", User.class);
TypedLong<Product> productId = TypedValues.typedLong(42L, Product.class);
TypedInt<Category> categoryId = TypedValues.typedInt(5, Category.class);
TypedUuid<Order> orderId = TypedValues.typedUuid(UUID.randomUUID(), Order.class);
```

---

## Type Hierarchy

All convenience types extend `TypedValue<ID, T>` directly:

```
TypedValue<VALUE, T> (open class)
    ├── TypedString<T>  (open class extending TypedValue<String, T>)
    ├── TypedLong<T>    (open class extending TypedValue<Long, T>)
    ├── TypedInt<T>     (open class extending TypedValue<Int, T>)
    └── TypedUuid<T>    (open class extending TypedValue<UUID, T>) [JVM only]
```

This means all convenience types are fully compatible with the generic `TypedValue<ID, T>` class:

```kotlin
// TypedString is assignable to TypedValue
val userId: TypedString<User> = "u-123".toTypedString()
val genericId: TypedValue<String, User> = userId // OK
```

---

## Choosing Between Types

| Scenario | Recommended Type | Reason |
|----------|-----------------|--------|
| External API IDs | `TypedString` | Flexible, widely compatible |
| Database auto-increment | `TypedLong` | Matches database sequence types |
| Small lookup tables | `TypedInt` | Memory efficient |
| Distributed systems (JVM) | `TypedUuid` | Native UUID, guaranteed uniqueness |
| Distributed systems (multiplatform) | `TypedString` with UUID strings | Cross-platform compatible |
| Custom Comparable types | Generic `TypedValue` | Maximum flexibility |

---

## Using with Data Classes

```kotlin
data class UserDto(
    val id: TypedString<User>,
    val name: String,
    val email: String
)

data class OrderDto(
    val id: TypedUuid<Order>,
    val userId: TypedString<User>,
    val items: List<OrderItemDto>
)

data class OrderItemDto(
    val productId: TypedLong<Product>,
    val quantity: Int
)
```

---

## API Summary

| Type | Factory Method | Extension Function |
|------|---------------|-------------------|
| `TypedString<T>` | `TypedString.of(value, type)` | `"value".toTypedString<T>()` |
| `TypedInt<T>` | `TypedInt.of(value, type)` | `42.toTypedInt<T>()` |
| `TypedLong<T>` | `TypedLong.of(value, type)` | `42L.toTypedLong<T>()` |
| `TypedUuid<T>` | `TypedUuid.of(value, type)` | `uuid.toTypedUuid<T>()` |

## Next Steps

- [Utilities](/guide/utilities) - Extension functions and helper methods
- [JVM Platform](/platforms/jvm) - Full JVM features including TypedUuid and Java interop
- [JavaScript Platform](/platforms/javascript) - JS-specific types and considerations
