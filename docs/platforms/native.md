<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Native Platform

typed-value supports Kotlin/Native for building native applications on macOS, Linux, Windows, and iOS.

## Installation

For Kotlin Multiplatform projects targeting Native:

```kotlin-vue
kotlin {
    // Choose your targets
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()  // Windows
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
            }
        }
    }
}
```

## Available Types

| Type | Status | Notes |
|------|--------|-------|
| `TypedString<T>` | Available | Full support |
| `TypedLong<T>` | Available | Full 64-bit support |
| `TypedInt<T>` | Available | Full support |
| `TypedUuid<T>` | Not available | Use TypedString with UUID strings |

## Usage

Native platforms use the same API as `commonMain`:

```kotlin
import com.ekino.oss.typedvalue.*

class User
class Product

// Create typed IDs
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()
val categoryId = 5.toTypedInt<Category>()

// Use in data classes
data class UserDto(
    val id: TypedString<User>,
    val name: String
)
```

## No UUID Support

Like JavaScript, `TypedUuid` is not available on Native because there's no `java.util.UUID`.

**Workaround**: Use `TypedString` with UUID strings:

```kotlin
// Generate UUID using platform-specific API or library
val uuidString = generateUuid() // Your implementation
val orderId = uuidString.toTypedString<Order>()
```

### Generating UUIDs on Native

For iOS/macOS, you can use Foundation:

```kotlin
// iosMain or macosMain
import platform.Foundation.NSUUID

fun generateUuid(): String = NSUUID().UUIDString()
```

For Linux/Windows, you may need a third-party library or implement UUID generation.

## Full Long Support

Unlike JavaScript, Native platforms have full 64-bit integer support:

```kotlin
// Safe on Native (and JVM)
val largeId = 9223372036854775807L.toTypedLong<Entity>() // Long.MAX_VALUE
```

## Reflection Behavior

Native has similar reflection limitations to JavaScript:

| Operation | JVM | Native |
|-----------|-----|--------|
| `KClass.hashCode()` | Available | Available |
| `T::class` | Available | Available |
| `KClass.qualifiedName` | Available | Limited |
| `KClass.simpleName` | Available | Limited |

::: tip
The core TypedValue functionality works without relying on these reflection features.
:::

## iOS Integration

For iOS apps, TypedValue can be used in shared Kotlin code:

```kotlin
// shared/src/commonMain/kotlin/Models.kt
data class UserProfile(
    val id: TypedString<User>,
    val name: String,
    val avatarUrl: String?
)

// shared/src/commonMain/kotlin/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: TypedString<User>): UserProfile?
}
```

Then access from Swift:

```swift
// Swift
let userId = TypedStringCompanion.shared.of(value: "u-123", type: User.self)
let user = try await userRepository.getUser(id: userId)
```

## Testing on Native

Tests in `commonTest` run on Native targets:

```bash
./gradlew typed-value-core:macosX64Test
./gradlew typed-value-core:linuxX64Test
./gradlew typed-value-core:iosSimulatorArm64Test
```

## Framework Integrations

::: warning
All framework integrations (Jackson, Spring, QueryDSL, JPA, Elasticsearch) are **JVM-only**.

For Native applications, you'll need to implement serialization and persistence manually or use Native-compatible libraries.
:::

## Memory Management

TypedValue instances follow Kotlin/Native's memory management model:

- Immutable by design
- No special considerations needed
- Safe to share between threads (with new memory manager)

## Next Steps

- [JVM Platform](/platforms/jvm) - Full JVM features
- [JavaScript Platform](/platforms/javascript) - JS platform support
