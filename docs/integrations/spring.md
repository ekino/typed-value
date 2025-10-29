<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Spring MVC Integration

The `typed-value-spring` module provides automatic conversion of path variables and request parameters to TypedValue types in Spring MVC.

::: info Built with
- **Spring Framework** 6.2.x
- **Spring Boot** 4.0.x (for auto-configuration)
:::

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring:{{ v.typedValue }}")
}
```

---

## Classes

### StringToTypedValueConverter

Generic converter that converts String values from `@PathVariable` and `@RequestParam` to TypedValue.

```kotlin
class StringToTypedValueConverter : GenericConverter {
    override fun getConvertibleTypes(): Set<ConvertiblePair>
    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any?
}
```

**Conversion Logic:**
1. Extracts generic type parameters from the target type
2. Determines the ID type (String, Long, Int, UUID)
3. Parses the string value to the appropriate ID type
4. Creates the TypedValue with resolved entity type

### TypedValueAutoConfiguration

Spring Boot auto-configuration that registers the converter automatically.

```kotlin
@AutoConfiguration
class TypedValueAutoConfiguration : WebMvcConfigurer {
    @Bean
    fun stringToTypedValueConverter(): StringToTypedValueConverter

    override fun addFormatters(registry: FormatterRegistry)
}
```

---

## Supported Conversions

| Target Type | String Input | Conversion |
|-------------|--------------|------------|
| `TypedString<T>` | `"value"` | Direct use |
| `TypedLong<T>` | `"123"` | `toLong()` |
| `TypedInt<T>` | `"42"` | `toInt()` |
| `TypedUuid<T>` | `"uuid-string"` | `UUID.fromString()` |

---

## Auto-Configuration

With Spring Boot, the converter is registered automatically:

```kotlin
// No manual configuration needed!
// Just add the dependency and use TypedValue in controllers
```

::: tip
Just add the dependency and the converter works out-of-the-box.
:::

---

## Path Variables

Use TypedValue directly in `@PathVariable`:

```kotlin
@RestController
@RequestMapping("/api")
class ApiController(private val userService: UserService) {

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: TypedString<User>): UserDto {
        return userService.findById(id)
    }

    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: TypedLong<Product>): ProductDto {
        return productService.findById(id)
    }

    @GetMapping("/orders/{id}")
    fun getOrder(@PathVariable id: TypedUuid<Order>): OrderDto {
        return orderService.findById(id)
    }
}
```

**Request Examples:**
- `GET /api/users/user-123` → `id: TypedString<User>("user-123")`
- `GET /api/products/42` → `id: TypedLong<Product>(42L)`
- `GET /api/orders/550e8400-e29b-41d4-a716-446655440000` → `id: TypedUuid<Order>(...)`

---

## Request Parameters

Use TypedValue in `@RequestParam`:

```kotlin
@GetMapping("/search")
fun searchProducts(
    @RequestParam categoryId: TypedInt<Category>,
    @RequestParam(required = false) brandId: TypedLong<Brand>?
): List<ProductDto> {
    return productService.search(categoryId, brandId)
}
```

**Request:** `GET /search?categoryId=5&brandId=100`

---

## Multiple Path Variables

Handle multiple typed IDs in one endpoint:

```kotlin
@GetMapping("/users/{userId}/orders/{orderId}")
fun getUserOrder(
    @PathVariable userId: TypedString<User>,
    @PathVariable orderId: TypedUuid<Order>
): OrderDto {
    // Both IDs are type-safe
    return orderService.findByUserAndId(userId, orderId)
}
```

---

## Optional Parameters

Nullable TypedValue works with optional request params:

```kotlin
@GetMapping("/filter")
fun filter(
    @RequestParam(required = false) userId: TypedString<User>?,
    @RequestParam(required = false) status: String?
): List<ItemDto> {
    return itemService.filter(userId, status)
}
```

---

## Type Safety in Action

The converter resolves the entity type from the parameter's generic type:

```kotlin
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: TypedString<User>): UserDto { ... }

@GetMapping("/products/{id}")
fun getProduct(@PathVariable id: TypedString<Product>): ProductDto { ... }
```

Even though both endpoints receive the same path pattern, the IDs are different types:
- First endpoint: `TypedString<User>`
- Second endpoint: `TypedString<Product>`

---

## Error Handling

Invalid conversions throw `TypeMismatchException`:

```kotlin
// GET /api/products/not-a-number
// Throws: Failed to convert 'not-a-number' to TypedLong<Product>
```

Handle with `@ExceptionHandler`:

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TypeMismatchException::class)
    fun handleTypeMismatch(ex: TypeMismatchException): ResponseEntity<ErrorDto> {
        return ResponseEntity
            .badRequest()
            .body(ErrorDto("Invalid ID format: ${ex.value}"))
    }
}
```

---

## Manual Configuration

If auto-configuration doesn't work (non-Boot app), register manually:

```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToTypedValueConverter())
    }
}
```

---

## Combined with Jackson

For complete REST API support, combine with Jackson integration:

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-jackson:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring:{{ v.typedValue }}")
}
```

Now path variables, request params, and request/response bodies all work with TypedValue:

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    // Path variable conversion (Spring module)
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: TypedString<User>): UserDto {
        return userService.findById(id)
    }

    // Request body deserialization (Jackson module)
    @PostMapping
    fun createUser(@RequestBody dto: CreateUserDto): UserDto {
        return userService.create(dto)
    }

    // Both path variable and request body
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: TypedString<User>,
        @RequestBody dto: UpdateUserDto
    ): UserDto {
        return userService.update(id, dto)
    }

    // Path variable conversion
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: TypedString<User>) {
        userService.delete(id)
    }
}

// DTOs with TypedValue (serialized by Jackson)
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
| `StringToTypedValueConverter` | Converts String to TypedValue for @PathVariable/@RequestParam |
| `TypedValueAutoConfiguration` | Auto-registers converter in Spring Boot |

## Next Steps

- [Jackson Integration](/integrations/jackson) - JSON serialization
- [QueryDSL Integration](/integrations/querydsl) - Database queries
- [Hibernate Integration](/integrations/hibernate) - Persistence layer
