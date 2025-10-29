<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# JavaScript Platform

typed-value supports JavaScript (browser and Node.js) through Kotlin/JS compilation, with TypeScript-friendly types that provide **compile-time type safety** via phantom type parameters.

## Installation

For Kotlin Multiplatform projects targeting JS:

```kotlin-vue
kotlin {
    js {
        browser()  // or nodejs()
    }

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
| `TypedLong<T>` | Available | Limited precision (see below) |
| `TypedInt<T>` | Available | Full support |
| `TypedUuid<T>` | Not available | Use TypedString with UUID strings |

---

## Type Safety Approach

The JavaScript/TypeScript API uses **phantom type parameters** to provide compile-time type safety without runtime overhead. This means:

- **Compile-time**: TypeScript enforces that `TypedString<User>` and `TypedString<Product>` are incompatible types
- **Runtime**: Only the value is stored (no type metadata)

```typescript
interface User {}
interface Product {}

const userId = createTypedString<User>('user-123')
const productId = createTypedString<Product>('prod-456')

// TypeScript ERROR: Type 'TypedString<Product>' is not assignable to type 'TypedString<User>'
function getUser(id: TypedString<User>) { ... }
getUser(productId)  // Compile error!
```

---

## TypeScript Usage

### Basic Usage

```typescript
import {
    createTypedString,
    createTypedInt,
    createTypedLong,
    TypedString,
    TypedInt,
    TypedLong
} from '@ekino/typed-value'

// Define entity marker types
interface User {}
interface Product {}
interface Order {}

// Create typed IDs with compile-time type safety
const userId: TypedString<User> = createTypedString<User>('user-123')
const productId: TypedInt<Product> = createTypedInt<Product>(42)
const orderId: TypedLong<Order> = createTypedLong<Order>(123456789)

// Access values
console.log(userId.value)     // 'user-123'
console.log(productId.value)  // 42

// Equality comparison
const userId2 = createTypedString<User>('user-123')
console.log(userId.equals(userId2))  // true

// Comparison for sorting
const comparison = userId.compareTo(userId2)  // 0, negative, or positive
```

### TypeScript Type Definitions

```typescript
// Auto-generated with phantom type parameters
export declare class TypedString<T = unknown> {
    private readonly __type?: T;  // Phantom type marker
    constructor(value: string);
    get value(): string;
    toString(): string;
    equals(other: any): boolean;
    hashCode(): number;
    compareTo(other: TypedString<T>): number;
}

export declare class TypedInt<T = unknown> {
    private readonly __type?: T;
    constructor(value: number);
    get value(): number;
    toString(): string;
    equals(other: any): boolean;
    hashCode(): number;
    compareTo(other: TypedInt<T>): number;
}

export declare class TypedLong<T = unknown> {
    private readonly __type?: T;
    constructor(value: number);  // Double in JS
    get value(): number;
    toString(): string;
    equals(other: any): boolean;
    hashCode(): number;
    compareTo(other: TypedLong<T>): number;
}
```

### Domain Modeling

```typescript
interface User {}
interface Product {}
interface Order {}

// Type-safe entity definitions
interface UserEntity {
    id: TypedString<User>
    name: string
    email: string
}

interface OrderEntity {
    id: TypedLong<Order>
    userId: TypedString<User>
    productIds: TypedInt<Product>[]
}

// Type-safe function signatures
function getUserById(id: TypedString<User>): UserEntity {
    // Only accepts User IDs - enforced at compile time
}

function processOrder(
    orderId: TypedLong<Order>,
    userId: TypedString<User>,
    productIds: TypedInt<Product>[]
): void {
    // Mixed ID types with full type safety
}
```

### Using with React/Vue/Angular

```typescript
// React example
interface UserProps {
    userId: TypedString<User>
}

function UserCard({ userId }: UserProps) {
    return <div>User ID: {userId.value}</div>
}

// Usage - type-safe props
const userId = createTypedString<User>('user-123')
<UserCard userId={userId} />

// This would NOT compile:
const productId = createTypedString<Product>('prod-456')
<UserCard userId={productId} />  // TypeScript Error!
```

### Collections

```typescript
// Type-safe arrays
const userIds: TypedString<User>[] = [
    createTypedString<User>('user-1'),
    createTypedString<User>('user-2'),
    createTypedString<User>('user-3')
]

// Filter and map with type safety
const activeIds = userIds.filter(id => id.value.startsWith('active'))
const rawValues: string[] = userIds.map(id => id.value)
```

---

## Long Precision Limitation

::: danger JavaScript Number Precision
JavaScript uses 64-bit floating-point numbers (IEEE 754 double). This means:

- Safe integer range: `-2^53 + 1` to `2^53 - 1`
- Maximum safe integer: `9,007,199,254,740,991`
- Integers larger than this **will lose precision**
:::

```typescript
// Safe on JVM, potentially lossy on JS
const largeId = createTypedLong<Order>(9007199254740993)
console.log(largeId.value)  // May not be exact!

// Safe on all platforms - use strings for large IDs
const safeId = createTypedString<Order>('9007199254740993')
```

**Recommendation:** For IDs that may exceed 2^53, use `TypedString` instead of `TypedLong`.

---

## No UUID Support

`TypedUuid` is not available on JavaScript because there's no `java.util.UUID` class.

**Workaround:** Use `TypedString` with UUID strings:

```typescript
import { v4 as uuidv4 } from 'uuid'
import { createTypedString, TypedString } from '@ekino/typed-value'

interface Order {}

// Generate UUID and wrap as TypedString
const orderId: TypedString<Order> = createTypedString<Order>(uuidv4())
console.log(orderId.value)  // '550e8400-e29b-41d4-a716-446655440000'
```

---

## Key Differences from Kotlin API

| Aspect | Kotlin (JVM) | TypeScript |
|--------|--------------|------------|
| Type identification | `KClass<T>` | Phantom type parameter `<T>` |
| Type safety | Compile-time generics | Compile-time phantom types |
| Long storage | `Long` (64-bit integer) | `number` (64-bit float) |
| UUID support | `TypedUuid<T>` | Not available |
| Factory syntax | `"id".toTypedString<User>()` | `createTypedString<User>("id")` |
| Runtime type info | Available via `type` property | Not available (compile-time only) |

---

## Browser vs Node.js

The library works in both environments:

```kotlin
kotlin {
    js {
        browser {
            // Browser-specific configuration
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs {
            // Node.js-specific configuration
            testTask {
                useMocha()
            }
        }
    }
}
```

---

## Testing on JS

Tests in `commonTest` run on both JVM and JS:

```kotlin
// commonTest/kotlin/TypedValueTest.kt
import kotlin.test.Test
import kotlin.test.assertEquals

class TypedValueTest {
    @Test
    fun `should create typed string`() {
        val userId = "u-123".toTypedString<User>()
        assertEquals("u-123", userId.value)
    }
}
```

Run JS tests:

```bash
./gradlew typed-value-core:jsTest        # Node.js
./gradlew typed-value-core:jsBrowserTest # Browser
```

---

## API Summary

| Function | Signature | Description |
|----------|-----------|-------------|
| `createTypedString` | `<T>(value: string) => TypedString<T>` | Create TypedString with type parameter |
| `createTypedInt` | `<T>(value: number) => TypedInt<T>` | Create TypedInt with type parameter |
| `createTypedLong` | `<T>(value: number) => TypedLong<T>` | Create TypedLong with type parameter |

## Next Steps

- [Native Platform](/platforms/native) - Native platform support
- [JVM Platform](/platforms/jvm) - Full JVM features
