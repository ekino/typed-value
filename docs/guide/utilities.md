# Utilities

typed-value provides several utility functions to make working with typed IDs easier.

## Type Checking

### isAboutType

Check if a TypedValue is associated with a specific entity type:

```kotlin
val id = "user-123".toTypedString<User>()

id.isAboutType<User>()    // true
id.isAboutType<Product>() // false
```

This is useful when working with collections of mixed types or when the type is erased at runtime.

### takeIfAboutType

Safely cast a TypedValue to a specific type, returning null if types don't match:

```kotlin
val id: TypedValue<String, *> = getIdFromSomewhere()

// Returns TypedString<User>? - null if not a User ID
val userId: TypedString<User>? = id.takeIfAboutType<String, User>()

// Use with let for safe access
id.takeIfAboutType<String, User>()?.let { userId ->
    userService.findById(userId)
}
```

## Collection Utilities

### toRawIds

Extract raw ID values from a collection of TypedValues:

```kotlin
val userIds = listOf(
    "u-1".toTypedString<User>(),
    "u-2".toTypedString<User>(),
    "u-3".toTypedString<User>()
)

// Get List<String>
val rawIds: List<String> = userIds.toRawIds()
// ["u-1", "u-2", "u-3"]
```

This is particularly useful when interfacing with APIs or databases that expect raw values.

### rawIds (Static)

Static version for Java interoperability:

```kotlin
val rawIds = TypedValue.rawIds(userIds)
```

```java
// Java
List<String> rawIds = TypedValue.rawIds(userIds);
```

## Builder Functions

### typedValueBuilderFor

Create a reusable function for building TypedValues of a specific type:

```kotlin
// Create a builder function
val toUserId = TypedValue.typedValueBuilderFor<String, User>(User::class)

// Use with map
val rawIds = listOf("u-1", "u-2", "u-3")
val userIds: List<TypedString<User>> = rawIds.map(toUserId)
```

This is especially useful when mapping database results or API responses:

```kotlin
// Mapping from database
val users = jdbcTemplate.query(sql) { rs, _ ->
    UserDto(
        id = toUserId(rs.getString("id")),
        name = rs.getString("name")
    )
}
```

## Nullable Support

### typedValueOrNullFor

Create a TypedValue from a nullable raw value:

```kotlin
val nullableId: String? = request.getParameter("userId")

// Returns null if input is null
val userId: TypedString<User>? = TypedValue.typedValueOrNullFor(nullableId, User::class)
```

This avoids the need for manual null checks:

```kotlin
// Instead of this
val userId = nullableId?.let { TypedString.of(it, User::class) }

// Use this
val userId = TypedValue.typedValueOrNullFor(nullableId, User::class)
```

## Working with Maps

TypedValues can be used as Map keys:

```kotlin
val userCache = mutableMapOf<TypedString<User>, UserDto>()

val userId = "u-123".toTypedString<User>()
userCache[userId] = UserDto(userId, "Alice", "alice@example.com")

// Retrieve
val user = userCache[userId]
```

::: tip
TypedValues implement proper `equals()` and `hashCode()` based on both value and type, making them safe to use as Map keys.
:::

## Sorting

TypedValues are Comparable and can be sorted:

```kotlin
val ids = listOf(
    "c".toTypedString<User>(),
    "a".toTypedString<User>(),
    "b".toTypedString<User>()
)

val sorted = ids.sorted()
// ["a", "b", "c"] (TypedString<User>)
```

For mixed types, sorting is stable (by value first, then by type hashCode).

## String Representation

The `toString()` method provides a readable representation:

```kotlin
val userId = "u-123".toTypedString<User>()
println(userId) // "Value(value=u-123, type=User)"
```

## Extension Function Summary

| Function | Description | Return Type |
|----------|-------------|-------------|
| `toTypedString<T>()` | Convert String to TypedString | `TypedString<T>` |
| `toTypedLong<T>()` | Convert Long to TypedLong | `TypedLong<T>` |
| `toTypedInt<T>()` | Convert Int to TypedInt | `TypedInt<T>` |
| `toTypedUuid<T>()` | Convert UUID to TypedUuid (JVM) | `TypedUuid<T>` |
| `isAboutType<T>()` | Check entity type | `Boolean` |
| `takeIfAboutType<ID, T>()` | Safe type cast | `TypedValue<ID, T>?` |
| `toRawIds()` | Extract raw values from collection | `List<VALUE>` |

## Next Steps

- [JVM Platform](/platforms/jvm) - Full JVM features
- [Jackson Integration](/integrations/jackson) - JSON serialization
- [Spring Integration](/integrations/spring) - Web framework support
