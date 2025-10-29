<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Jackson Integration

The `typed-value-jackson` module provides JSON serialization and deserialization for TypedValue types using Jackson.

::: info Built with
- **Jackson** 3.0.x
:::

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-jackson:{{ v.typedValue }}")
}
```

---

## Classes

### TypedValueModule

Jackson module that registers TypedValue serializers and deserializers.

```kotlin
class TypedValueModule : SimpleModule()
```

Automatically registers:
- `TypedValueSerializer` for all TypedValue types
- `TypedValueDeserializer` for TypedValue, TypedUuid, TypedString, TypedLong, TypedInt

### TypedValueSerializer

Serializes TypedValue to its raw ID value.

| TypedValue Type | JSON Output |
|-----------------|-------------|
| `TypedString<T>` | `"string-value"` |
| `TypedLong<T>` | `12345` |
| `TypedInt<T>` | `42` |
| `TypedUuid<T>` | `"550e8400-e29b-41d4-a716-446655440000"` |
| `TypedValue<Double, T>` | `3.14` |
| `TypedValue<Float, T>` | `3.14` |
| Other Number types | `12345` (as Long) |
| Other Comparable | `"string-representation"` |

### TypedValueDeserializer

Deserializes JSON values to TypedValue using contextual type resolution.

**Key Features:**
- Uses `createContextual()` to resolve generic type parameters from property declarations
- Extracts entity type (T) and ID type from the target type
- Supports all convenience types and generic TypedValue

**Supported ID Types:**

| ID Type | JSON Input | Conversion |
|---------|------------|------------|
| String | `"value"` | Direct use |
| Long | `12345` or `"12345"` | Parsed to Long |
| Int | `42` or `"42"` | Parsed to Int |
| UUID | `"uuid-string"` | `UUID.fromString()` |
| Other Comparable | `"value"` | String representation |

---

## Configuration

### Spring Boot

With Spring Boot, simply register the module as a bean:

```kotlin
@Configuration
class JacksonConfig {

    @Bean
    fun typedValueModule(): Module = TypedValueModule()
}
```

The module is auto-detected by Spring Boot's Jackson auto-configuration.

### Manual Configuration

For non-Spring applications:

```kotlin
import com.fasterxml.jackson.module.kotlin.kotlinModule

val objectMapper = ObjectMapper()
    .registerModule(TypedValueModule())
    .registerModule(kotlinModule())

// Or using builder
val objectMapper = jsonMapper {
    addModule(kotlinModule())
    addModule(TypedValueModule())
}
```

---

## Serialization

TypedValue is serialized as its raw value:

```kotlin
data class UserDto(
    val id: TypedString<User>,
    val name: String
)

val user = UserDto(
    id = "user-123".toTypedString(),
    name = "Alice"
)

val json = objectMapper.writeValueAsString(user)
// {"id":"user-123","name":"Alice"}
```

### Serialization Examples

```kotlin
// String ID
val userId = "u-123".toTypedString<User>()
objectMapper.writeValueAsString(userId)  // "u-123"

// Long ID
val productId = 42L.toTypedLong<Product>()
objectMapper.writeValueAsString(productId)  // 42

// UUID ID
val orderId = UUID.randomUUID().toTypedUuid<Order>()
objectMapper.writeValueAsString(orderId)  // "550e8400-e29b-41d4-a716-446655440000"
```

---

## Deserialization

Jackson resolves the entity type from the property's generic type:

```kotlin
data class OrderDto(
    val id: TypedUuid<Order>,
    val userId: TypedString<User>,
    val productIds: List<TypedLong<Product>>
)

val json = """
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user-123",
    "productIds": [1, 2, 3]
}
"""

val order = objectMapper.readValue<OrderDto>(json)
// order.id is TypedUuid<Order>
// order.userId is TypedString<User>
// order.productIds is List<TypedLong<Product>>
```

### Type Resolution

The deserializer uses Jackson's type system to resolve generic parameters:

```kotlin
// Type information comes from:
// 1. Property declaration
// 2. Generic type parameters
// 3. Contextual type from parent

data class Dto(
    val userId: TypedString<User>,    // Resolved as TypedString<User>
    val productId: TypedLong<Product> // Resolved as TypedLong<Product>
)
```

---

## Collections

TypedValue works in collections:

```kotlin
data class TeamDto(
    val memberIds: List<TypedString<User>>,
    val projectIds: Set<TypedLong<Project>>
)

val json = """
{
    "memberIds": ["u-1", "u-2", "u-3"],
    "projectIds": [100, 200, 300]
}
"""

val team = objectMapper.readValue<TeamDto>(json)
```

---

## Nullable Fields

Nullable TypedValue fields are supported:

```kotlin
data class UserDto(
    val id: TypedString<User>,
    val managerId: TypedString<User>?  // Can be null
)

val json = """{"id": "u-1", "managerId": null}"""
val user = objectMapper.readValue<UserDto>(json)
// user.managerId is null
```

---

## Error Handling

Invalid JSON values throw descriptive exceptions:

```kotlin
// Invalid UUID format
val json = """{"id": "not-a-uuid"}"""

try {
    objectMapper.readValue<TypedUuid<Order>>(json)
} catch (e: JsonMappingException) {
    // "Invalid UUID string: not-a-uuid"
}
```

---

## Limitations

::: warning Type Parameters Required
The deserializer requires explicit generic type parameters. Wildcards or raw types won't work:

```kotlin
// OK - explicit type
data class GoodDto(val id: TypedString<User>)

// NOT OK - wildcard type
data class BadDto(val id: TypedString<*>)  // Cannot resolve entity type
```
:::

::: info Type Information Not Serialized
The entity type (T) is NOT included in the serialized JSON. It's resolved from the property declaration during deserialization.
:::

---

## Complete Example

### Spring Boot REST Controller

```kotlin
@Configuration
class JacksonConfiguration {
    @Bean
    fun typedValueModule() = TypedValueModule()
}

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: TypedString<User>): UserDto {
        return userService.findById(id)
    }

    @PostMapping
    fun createUser(@RequestBody dto: CreateUserDto): UserDto {
        return userService.create(dto)
    }
}

data class CreateUserDto(
    val name: String,
    val email: String
)

data class UserDto(
    val id: TypedString<User>,
    val name: String,
    val email: String
)
```

---

## API Summary

| Class | Purpose |
|-------|---------|
| `TypedValueModule` | Jackson module for registration |
| `TypedValueSerializer` | Serializes TypedValue to raw ID |
| `TypedValueDeserializer` | Deserializes JSON to TypedValue with contextual type resolution |

## Next Steps

- [Spring Integration](/integrations/spring) - Path variable and request param support
- [QueryDSL Integration](/integrations/querydsl) - Type-safe queries
