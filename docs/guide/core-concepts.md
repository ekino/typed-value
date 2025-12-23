# Core Concepts

Understanding the fundamental concepts behind typed-value will help you use the library effectively.

## TypedValue Class

At the heart of the library is the `TypedValue<VALUE, T>` open class:

```kotlin
open class TypedValue<VALUE : Comparable<VALUE>, T : Any>(
    open val value: VALUE,
    open val type: KClass<out T>
) : Comparable<TypedValue<VALUE, T>>
```

### Type Parameters

- **VALUE : Comparable\<VALUE\>**: The raw value type (String, Long, Int, UUID, or any Comparable)
- **T : Any**: The marker type this value is tagged with (entity, quantity type, money type, etc.)

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `value` | `VALUE` | The underlying value (e.g., `"user-123"`, `42L`, `5`) |
| `type` | `KClass<out T>` | Runtime type information of the marker type |

### Override Methods

| Method | Description |
|--------|-------------|
| `toString()` | Returns `"TypeName(value)"` (e.g., `"User(user-123)"`) |
| `equals(other)` | Returns `true` if both `value` AND `type` match |
| `hashCode()` | Based on `value.hashCode()` only |
| `compareTo(other)` | Primary: by value, Secondary: by type hashCode |

All convenience types (`TypedString`, `TypedLong`, etc.) extend this class directly.

## Companion Object API

### typedValueFor

Creates a TypedValue from a raw value and marker type.

```kotlin
fun <ID : Comparable<ID>, T : Any> typedValueFor(
    rawId: ID,
    type: KClass<T>
): TypedValue<ID, T>
```

**Example:**
```kotlin
val userId = TypedValue.typedValueFor("user-123", User::class)
val bananaCount = TypedValue.typedValueFor(5, Banana::class)
```

### typedValueOrNullFor

Creates a TypedValue from a nullable value, returning null if the value is null.

```kotlin
fun <ID : Comparable<ID>, T : Any> typedValueOrNullFor(
    rawId: ID?,
    type: KClass<T>
): TypedValue<ID, T>?
```

**Example:**
```kotlin
val nullableId: String? = request.getParameter("userId")
val userId: TypedString<User>? = TypedValue.typedValueOrNullFor(nullableId, User::class)
```

### typedValueBuilderFor

Returns a function that builds TypedValues for a specific marker type. Useful for mapping collections.

```kotlin
fun <ID : Comparable<ID>, T : Any> typedValueBuilderFor(
    type: KClass<T>
): (ID) -> TypedValue<ID, T>
```

**Example:**
```kotlin
val toUserId = TypedValue.typedValueBuilderFor<String, User>(User::class)
val userIds = rawIds.map(toUserId)

// Or inline
val userIds = rawIds.map(TypedValue.typedValueBuilderFor(User::class))
```

### rawValues

Extracts the underlying raw values from a collection of TypedValues.

```kotlin
fun <ID : Comparable<ID>> rawIds(
    typedValues: Iterable<TypedValue<ID, *>>
): List<ID>
```

**Example:**
```kotlin
val userIds: List<TypedString<User>> = listOf(...)
val rawIds: List<String> = TypedValue.rawIds(userIds)
// Use rawIds with legacy API or database query
```

## Extension Functions

### toTypedValueFor

Creates a TypedValue using Kotlin reified generics. The idiomatic Kotlin way to create TypedValues.

```kotlin
inline fun <ID : Comparable<ID>, reified T : Any> ID.toTypedValueFor(): TypedValue<ID, T>
```

**Example:**
```kotlin
val userId = "user-123".toTypedValueFor<String, User>()
val productId = 42L.toTypedValueFor<Long, Product>()
```

### isAboutType

Checks if this TypedValue is tagged with the specified marker type. Uses exact type matching (not inheritance).

```kotlin
inline fun <reified T : Any> TypedValue<*, *>.isAboutType(): Boolean
```

**Example:**
```kotlin
val userId: TypedValue<String, User> = ...

userId.isAboutType<User>()    // true
userId.isAboutType<Product>() // false
```

### takeIfAboutType

Returns this TypedValue cast to the specified type if it matches, null otherwise. Useful for safe type narrowing.

```kotlin
inline fun <ID : Comparable<ID>, reified T : Any> TypedValue<ID, *>.takeIfAboutType(): TypedValue<ID, T>?
```

**Example:**
```kotlin
val someId: TypedValue<String, *> = getIdFromSomewhere()

val userId: TypedValue<String, User>? = someId.takeIfAboutType<String, User>()
userId?.let { processUser(it) }
```

### toRawValues (Collection)

Extracts the underlying raw values from a collection of TypedValues.

```kotlin
fun <ID : Comparable<ID>> Collection<TypedValue<ID, *>>.toRawIds(): List<ID>
```

**Example:**
```kotlin
val userIds: List<TypedString<User>> = listOf(...)
val rawIds: List<String> = userIds.toRawIds()
```

## Type Safety Mechanism

The magic happens through Kotlin's type system:

```kotlin
// These are different types at compile time
val userId: TypedString<User> = "u-123".toTypedString()
val productId: TypedString<Product> = "p-456".toTypedString()

// The compiler treats them as incompatible types
fun findUser(id: TypedString<User>): User? = ...

findUser(userId)    // OK
findUser(productId) // Error: Type mismatch
                    // Required: TypedString<User>
                    // Found: TypedString<Product>
```

## Equality and Comparison

Two TypedValues are equal only if **both** the value and type match:

```kotlin
val id1 = "abc".toTypedString<User>()
val id2 = "abc".toTypedString<User>()
val id3 = "abc".toTypedString<Product>()

id1 == id2  // true - same value, same type
id1 == id3  // false - same value, different type!
```

### Comparable Implementation

TypedValues can be compared and sorted:

1. **Primary sort**: By value (natural ordering)
2. **Secondary sort**: By type hashCode (for stability across different types)

```kotlin
val ids = listOf(
    "b".toTypedString<User>(),
    "a".toTypedString<User>(),
    "c".toTypedString<Product>()
)

ids.sorted() // Sorted by value first, then by type
```

## KClass vs Class

The library uses `KClass<T>` (Kotlin reflection) instead of `Class<T>` (Java reflection) for multiplatform compatibility:

```kotlin
// Create using KClass (multiplatform)
TypedValue.typedValueFor("id", User::class)

// NOT Class (JVM only)
// TypedValue.typedValueFor("id", User::class.java) // Don't use this
```

::: warning
The `::class.java` syntax is JVM-only and won't work on JavaScript or Native platforms.
:::

## Immutability

TypedValue instances are immutable:

- `value` and `type` are `val` (read-only)
- No methods modify the instance
- Safe to share across threads
- Safe to use as Map keys or Set elements

## Package Structure

All classes are in the `com.ekino.oss.typedvalue` package:

```kotlin
import com.ekino.oss.typedvalue.TypedValue
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedUuid  // JVM only

// Or import everything
import com.ekino.oss.typedvalue.*
```

## API Summary Table

| Function | Description | Return Type |
|----------|-------------|-------------|
| `TypedValue.typedValueFor(id, type)` | Create TypedValue | `TypedValue<ID, T>` |
| `TypedValue.typedValueOrNullFor(id?, type)` | Create nullable | `TypedValue<ID, T>?` |
| `TypedValue.typedValueBuilderFor(type)` | Builder function | `(ID) -> TypedValue<ID, T>` |
| `TypedValue.rawIds(collection)` | Extract raw values | `List<ID>` |
| `id.toTypedValueFor<ID, T>()` | Reified creation | `TypedValue<ID, T>` |
| `typedValue.isAboutType<T>()` | Type check | `Boolean` |
| `typedValue.takeIfAboutType<ID, T>()` | Safe cast | `TypedValue<ID, T>?` |
| `collection.toRawIds()` | Extract raw values | `List<ID>` |

## Next Steps

- [Convenience Types](/guide/convenience-types) - Learn about TypedString, TypedInt, TypedLong, TypedUuid
- [Utilities](/guide/utilities) - Extension functions and helper methods
