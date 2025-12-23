# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

**Typed-Value** is a Kotlin Multiplatform library providing type-safe value wrappers with framework integrations. It prevents mixing incompatible values (IDs, quantities, money, etc.) at compile time. It's a multi-module Gradle project designed for reusability and minimal coupling.

**Use Cases:**
- Type-safe identifiers (User ID vs Product ID)
- Type-safe quantities (bananas vs apples)
- Type-safe money (cents vs euros)
- Any value that should not be mixed with others of the same primitive type

**Key Design Principles:**
- **Multiplatform Core**: JVM, JS, and Native support
- Generic value types (String, Long, Int, UUID, or any Comparable)
- Zero dependencies in core module
- Framework integrations as optional JVM-only modules
- Kotlin-first with Java interoperability
- Type safety at compile time

**Important: Package Naming**

The correct package name is `com.ekino.oss.typedvalue` (not `typedid`). The project was recently renamed from "typed-id" to "typed-value".

**Always use:**
- ✅ `import com.ekino.oss.typedvalue.*`
- ❌ NOT `import com.ekino.oss.typedid.*` (outdated)

**Important: API Changes for Multiplatform**

The library uses `KClass<T>` (Kotlin reflection) instead of `Class<T>` (Java reflection) for multiplatform compatibility.

**Always use:**
- ✅ `TypedValue.typedValueFor("id", User::class)` (multiplatform)
- ❌ NOT `TypedValue.typedValueFor("id", User::class.java)` (JVM-only, outdated)

## Project Structure

### Module Organization

```
typed-value/
├── typed-value-core/                    # Multiplatform core (JVM, JS, Native)
│   ├── src/
│   │   ├── commonMain/kotlin/           # Platform-independent code
│   │   ├── commonTest/kotlin/           # Platform-independent tests
│   │   ├── jvmMain/kotlin/              # JVM-specific code (TypedUuid)
│   │   └── jvmTest/kotlin/              # JVM-specific tests
├── typed-value-jackson/                 # JVM-only: Jackson JSON support
├── typed-value-hibernate/               # JVM-only: Hibernate/JPA entity support
├── typed-value-spring/                  # JVM-only: Spring MVC converters
├── typed-value-querydsl/                # JVM-only: QueryDSL integration
└── typed-value-spring-data-elasticsearch/  # JVM-only: Elasticsearch mapping
```

### Module Architecture

**Multiplatform Module:**
- **typed-value-core**: Kotlin Multiplatform module
  - `commonMain`: Platform-independent TypedValue, TypedString, TypedInt, TypedLong
  - `jvmMain`: JVM-specific TypedUuid (uses java.util.UUID)
  - Zero dependencies (not even Kotlin stdlib beyond multiplatform stdlib)

**JVM-Only Integration Modules:**
- **typed-value-jackson**: Depends on core + Jackson
- **typed-value-hibernate**: Depends on core + Hibernate (entity base classes, JPA converters)
- **typed-value-spring**: Depends on core + Spring
- **typed-value-querydsl**: Depends on core + QueryDSL
- **typed-value-spring-data-elasticsearch**: Depends on core + Spring Data ES
- These modules convert between `Class<T>` (framework APIs) and `KClass<T>` (core APIs)

**Important**: Core module MUST remain dependency-free and multiplatform-compatible. All framework integrations go in separate JVM-only modules.

## Common Development Commands

### Building and Testing

**General Commands:**
```bash
# Full build with all tests (JVM + JS for core)
./gradlew build

# Build specific module
./gradlew typed-value-core:build
./gradlew typed-value-jackson:build

# Run all tests
./gradlew test
```

**Multiplatform-Specific Commands (typed-value-core):**
```bash
# Compile for specific platforms
./gradlew typed-value-core:compileKotlinJvm
./gradlew typed-value-core:compileKotlinJs

# Build platform-specific artifacts
./gradlew typed-value-core:jvmJar
./gradlew typed-value-core:jsJar

# Run tests for specific platforms
./gradlew typed-value-core:jvmTest       # Run JVM tests
./gradlew typed-value-core:jsTest        # Run JS tests (Node.js)
./gradlew typed-value-core:jsBrowserTest # Run JS tests (browser)

# Run specific test class (platform-specific)
./gradlew typed-value-core:jvmTest --tests "TypedValueTest"
./gradlew typed-value-core:jsNodeTest --tests "TypedValueTest"
```

**Integration Module Tests (JVM-only):**
```bash
# Run specific test class
./gradlew typed-value-jackson:test --tests "TypedValueJacksonTest"

# Run specific test method
./gradlew typed-value-jackson:test --tests "TypedValueJacksonTest.should serialize TypedValue with String ID"
```

### Code Quality

```bash
# Format all code (REQUIRED before commit)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Run static analysis
./gradlew detekt

# Run all quality checks
./gradlew spotlessCheck detekt test
```

### Working with Individual Modules

```bash
# Build only one module and its dependencies
./gradlew :typed-value-jackson:build

# Clean and rebuild
./gradlew clean build

# Assemble JARs without running tests
./gradlew assemble
```

## Development Guidelines

### When Adding New Features

1. **Identify the right module**:
   - Core functionality? → `typed-value-core`
   - JSON serialization? → `typed-value-jackson`
   - Hibernate/JPA entities? → `typed-value-hibernate`
   - Spring MVC converters? → `typed-value-spring`
   - QueryDSL queries? → `typed-value-querydsl`
   - Elasticsearch? → `typed-value-spring-data-elasticsearch`

2. **Write tests FIRST**: This is a library. Every public API needs tests.

3. **Update documentation**: Add examples to README.md

4. **Run quality checks**:
   ```bash
   ./gradlew spotlessApply
   ./gradlew detekt
   ./gradlew test
   ```

### Code Style

- **Language**: Prefer Kotlin over Java for all new code
- **Formatting**: ktfmt Google Style (enforced by Spotless)
- **Null safety**: Leverage Kotlin's null safety, avoid platform types
- **Immutability**: Prefer `val` over `var`, immutable collections
- **Visibility**: Use most restrictive visibility possible
- **Documentation**: KDoc on all public APIs

### Testing Standards

**Test File Naming:**
- `<ClassName>Test.kt` for unit tests
- Test classes should be in the same package as the code they test

**Test Structure:**
```kotlin
class TypedValueTest {
  @Test
  fun `should do something specific`() {
    // Arrange
    val input = ...

    // Act
    val result = ...

    // Assert
    assertThat(result).isEqualTo(expected)
  }
}
```

**Assertions:**
- Use **AssertK** for Kotlin tests (preferred)
- Use **AssertJ** for Java tests (if any)
- Prefer descriptive test names with backticks

**Coverage:**
- Aim for 80%+ coverage on core module
- Test all public APIs
- Test edge cases (null, empty, boundary conditions)
- Test error cases (exceptions, validation)

## Module-Specific Guidelines

### typed-value-core

**Purpose**: Multiplatform core TypedValue abstraction with zero dependencies

**Key Files:**
- `commonMain/kotlin/`:
  - `TypedValue.kt` - Main open class (uses `KClass<T>`)
  - `TypedString.kt`, `TypedInt.kt`, `TypedLong.kt` - Convenience classes extending TypedValue
- `jvmMain/kotlin/`:
  - `TypedUuid.kt` - JVM-only UUID support (uses `java.util.UUID`)

**Multiplatform Rules:**
- NO external dependencies (not even Spring or Jackson)
- NO JVM-specific APIs in `commonMain` (use `KClass`, not `Class`)
- NO platform-specific types in `commonMain` (UUID must be in `jvmMain`)
- Keep it focused on the domain model
- NO `@JvmStatic` in `commonMain` (not needed, breaks multiplatform)

**Source Set Guidelines:**

**When to put code in `commonMain`:**
- ✅ Works with `KClass<T>` (Kotlin reflection)
- ✅ Uses only Kotlin stdlib (String, Int, Long, etc.)
- ✅ No platform-specific APIs (no java.*, no JS-specific APIs)
- ✅ Generic, portable logic

**When to put code in `jvmMain`:**
- Platform-specific types: `java.util.UUID`, `java.time.*`
- JVM-only convenience methods
- Java interop helpers (if needed)

**When to put code in `jsMain`:**
- JavaScript-specific implementations (rarely needed)
- JS-specific optimizations

**Reflection Limitations:**
- ❌ `KClass.qualifiedName` and `KClass.simpleName` not available on JS
- ✅ `KClass.hashCode()` works on all platforms
- ❌ `KClass.isSuperclassOf()` is JVM-only extension
- ✅ Use exact type matching (`T::class == type`) instead of inheritance checks

**When to modify:**
- Adding new core functionality to TypedValue
- Fixing bugs in core logic
- Adding utility functions that work for ANY ID type
- Adding new convenience types (String/Int/Long in commonMain, UUID in jvmMain)

### typed-value-jackson

**Purpose**: JSON serialization/deserialization (JVM-only)

**Key Files:**
- `TypedValueSerializer.kt` - Writes TypedValue to JSON
- `TypedValueDeserializer.kt` - Reads TypedValue from JSON
- `TypedValueModule.kt` - Jackson module for registration

**Rules:**
- Support all Comparable ID types (String, Long, UUID, etc.)
- Use contextual deserialization for type resolution
- Test with various DTO structures
- **Convert Class to KClass**: Jackson gives `Class<T>`, core needs `KClass<T>`

**Class/KClass Boundary Pattern:**
```kotlin
// Deserializer receives Class from Jackson type system
val entityType: Class<*> = contextualType.rawClass

// Convert to KClass when creating TypedValue
TypedValue(rawId as Comparable<Any>, (entityType as Class<Any>).kotlin)
```

**When to modify:**
- Adding support for new ID types
- Fixing serialization bugs
- Improving error messages

### typed-value-spring

**Purpose**: Spring MVC integration (JVM-only)

**Key Files:**
- `StringToTypedValueConverter.kt` - Converts @PathVariable/@RequestParam
- `TypedValueAutoConfiguration.kt` - Spring Boot auto-config

**Rules:**
- Converters should be as generic as possible
- Support common ID types (String, Long, Int, UUID)
- Auto-configuration should work out-of-the-box
- **Convert Class to KClass**: Spring gives `Class<T>`, core needs `KClass<T>`

**Class/KClass Boundary Pattern:**
```kotlin
// Spring TypeDescriptor uses Class
val entityType: Class<*> = targetType.resolvableType.resolveGeneric(1)!!

// Convert to KClass when creating TypedValue
TypedValue(rawId as Comparable<Any>, (entityType as Class<Any>).kotlin)

// When calling Spring APIs, use .java
TypeDescriptor.valueOf(String::class.java)
```

**When to modify:**
- Adding new converter types
- Fixing path variable conversion bugs
- Improving Spring Boot integration

### typed-value-hibernate

**Purpose**: Hibernate/JPA entity support with proper equals/hashCode implementation (JVM-only)

**Key Files:**
- `entity/HibernateEntityUtils.kt` - Static utility methods for entity equals/hashCode
- `entity/AbstractUuidEntity.kt` - Base class for UUID primary keys (auto-generated)
- `entity/AbstractStringEntity.kt` - Base class for String primary keys (assigned)
- `entity/AbstractLongEntity.kt` - Base class for Long primary keys (auto-increment)
- `entity/AbstractIntEntity.kt` - Base class for Int primary keys (auto-increment)
- `TypedStringConverter.kt`, `TypedIntConverter.kt`, `TypedLongConverter.kt`, `TypedUuidConverter.kt` - JPA AttributeConverters
- `spring/TypedValueJpaRepository.kt` - Custom Spring Data JPA repository
- `spring/TypedValueJpaRepositoryFactory.kt` - Factory for typed repositories
- `spring/TypedValueJpaRepositoryFactoryBean.kt` - Spring factory bean

**Why Hibernate-Specific:**
- Uses `HibernateProxy.extractLazyInitializer()` for proper proxy handling
- Avoids triggering lazy loading in equals/hashCode (unlike `Hibernate.getClass()`)
- Hibernate is the dominant JPA implementation (~90%+ of Spring Boot users)

**Entity Base Classes:**

| Class | ID Type | Generation Strategy |
|-------|---------|---------------------|
| `AbstractUuidEntity<T>` | `TypedUuid<T>` | `GenerationType.UUID` (auto) |
| `AbstractStringEntity<T>` | `TypedString<T>` | None (user must provide) |
| `AbstractLongEntity<T>` | `TypedLong<T>` | `GenerationType.IDENTITY` (auto) |
| `AbstractIntEntity<T>` | `TypedInt<T>` | `GenerationType.IDENTITY` (auto) |

**Usage with Abstract Classes:**
```kotlin
@Entity
class Person(var name: String) : AbstractUuidEntity<Person>(Person::class)

// ID is automatically available as TypedUuid<Person>
val person = personRepository.findById(typedId)
```

**Usage with HibernateEntityUtils (without abstract classes):**
```kotlin
@Entity
class Person(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private var _id: UUID? = null,
    var name: String
) {
    @get:Transient
    var id: TypedUuid<Person>?
        get() = _id?.toTypedUuid()
        set(value) { _id = value?.value }

    override fun equals(other: Any?) = HibernateEntityUtils.entityEquals(this, other) { it.id }
    override fun hashCode() = HibernateEntityUtils.entityHashCode(this)
}
```

**JPA Converter Usage (for non-ID fields):**
```kotlin
@Converter(autoApply = true)
class UserIdConverter : TypedUuidConverter<User>(User::class)

@Entity
class Order(
    @Id val id: UUID,
    @Convert(converter = UserIdConverter::class)
    var createdBy: TypedUuid<User>? = null
)
```

**Note**: JPA spec does NOT allow AttributeConverters on `@Id` fields. Use virtual typed properties for IDs.

**When to modify:**
- Adding new entity base classes
- Fixing equals/hashCode issues
- Adding JPA converter types
- Improving Spring Data JPA integration

### typed-value-querydsl

**Purpose**: QueryDSL type-safe query support

**Key Files:**
- `TypedValueExpression.kt` - QueryDSL expression wrapper

**Rules:**
- Map ID types to appropriate QueryDSL paths
- Support all common predicates (eq, in, isNull, etc.)
- Handle different path types (StringPath, NumberPath, etc.)

**When to modify:**
- Adding new predicate types
- Supporting new ID types
- Fixing query generation bugs

### typed-value-spring-data-elasticsearch

**Purpose**: Elasticsearch document mapping

**Key Files:**
- `TypedValueElasticsearchMappingContext.kt` - Custom mapping context
- `TypedValueElasticPersistentPropertyWithConverter.kt` - Property converter

**Rules:**
- Validate document structure (no Arrays/Sets)
- Support Lists of TypedValue
- Handle type resolution from property metadata

**When to modify:**
- Adding support for new collection types
- Fixing Elasticsearch serialization bugs
- Improving type resolution

## Multiplatform Development Guide

### Overview

The `typed-value-core` module is a Kotlin Multiplatform module targeting JVM, JS, and Native platforms. This section provides guidance on multiplatform development.

### Source Set Structure

```
typed-value-core/src/
├── commonMain/kotlin/          # Code that runs on ALL platforms
│   ├── TypedValue.kt          # Core open class (KClass-based)
│   ├── TypedString.kt         # String IDs (extends TypedValue)
│   ├── TypedInt.kt            # Int IDs (extends TypedValue)
│   └── TypedLong.kt           # Long IDs (extends TypedValue)
├── commonTest/kotlin/          # Tests that run on ALL platforms
│   ├── TypedValueTest.kt      # Core functionality tests
│   └── CommonTypesTest.kt     # Convenience type tests
├── jvmMain/kotlin/             # JVM-ONLY code
│   └── TypedUuid.kt           # UUID support (extends TypedValue)
└── jvmTest/kotlin/             # JVM-ONLY tests
    └── TypedUuidTest.kt       # UUID-specific tests
```

### When to Use Each Source Set

**commonMain - Platform-Independent Code:**
- ✅ Works with standard Kotlin types: String, Int, Long, Boolean, etc.
- ✅ Uses `KClass<T>` for type information
- ✅ No platform-specific APIs (no `java.*`, no browser APIs)
- ✅ Logic that should work everywhere

**jvmMain - JVM-Specific Code:**
- Use for JVM-specific types: `java.util.UUID`, `java.time.*`, `java.math.BigDecimal`
- Use for Java interop helpers
- Platform-specific optimizations

**jsMain - JavaScript-Specific Code:**
- Rarely needed for this library
- JS-specific implementations or optimizations
- Browser or Node.js specific code

### Multiplatform Testing

**commonTest - Tests for All Platforms:**
```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals

class TypedValueTest {
  @Test
  fun `should create typed value`() {
    val userId = TypedValue.typedValueFor("user-123", User::class)
    assertEquals("user-123", userId.value)
  }
}
```

**Important Test Notes:**
- Use `kotlin.test.Test`, NOT `org.junit.jupiter.api.Test`
- JUnit is JVM-only and won't work in commonTest
- Test names with backticks work on JVM but may need quotes for JS

**Platform-Specific Tests:**
```kotlin
// jvmTest/kotlin/TypedUuidTest.kt
import java.util.UUID
import org.junit.jupiter.api.Test

class TypedUuidTest {
  @Test
  fun `should work with UUID`() {
    val uuid = UUID.randomUUID()
    val orderId = TypedUuid.of(uuid, Order::class)
    // ...
  }
}
```

### Reflection Limitations

Kotlin reflection works differently across platforms:

**Available on All Platforms:**
- ✅ `KClass.hashCode()` - Use for comparisons
- ✅ `T::class` - Get KClass instance
- ✅ `instance::class` - Get KClass from instance
- ✅ Exact type equality: `T::class == type`

**JVM-Only:**
- ❌ `KClass.qualifiedName` - Returns full package.ClassName (JS: throws)
- ❌ `KClass.simpleName` - Returns short ClassName (JS: throws)
- ❌ `KClass.isSuperclassOf()` - Extension function (JVM-only)
- ❌ Inheritance checks

**Workarounds:**
```kotlin
// ❌ Don't use qualifiedName/simpleName
override fun compareTo(other: TypedValue<VALUE, T>): Int {
  // This breaks on JS:
  // return type.qualifiedName!!.compareTo(other.type.qualifiedName!!)

  // ✅ Use hashCode instead:
  return type.hashCode().compareTo(other.type.hashCode())
}

// ❌ Don't check inheritance
inline fun <reified T : Any> isAboutType(): Boolean {
  // This breaks on JS:
  // return T::class.isSuperclassOf(type)

  // ✅ Use exact match:
  return T::class == type
}
```

### Platform-Specific APIs

**When Adding New Functionality:**

1. **Can it use only Kotlin stdlib?** → Put in `commonMain`
   ```kotlin
   // commonMain - String, Int, Long work everywhere
   class TypedLong<T : Any>(value: Long, type: KClass<out T>) : TypedValue<Long, T>(value, type)
   ```

2. **Does it need java.* APIs?** → Put in `jvmMain`
   ```kotlin
   // jvmMain - UUID requires java.util.UUID
   class TypedUuid<T : Any>(value: UUID, type: KClass<out T>) : TypedValue<UUID, T>(value, type)
   ```

3. **Does it need JS/Native APIs?** → Put in respective source set

### Common Multiplatform Mistakes

**Mistake 1: Using `::class.java` in commonMain**
```kotlin
// ❌ Breaks - Class is JVM-only
fun create() = TypedValue.typedValueFor("id", User::class.java)

// ✅ Correct - KClass is multiplatform
fun create() = TypedValue.typedValueFor("id", User::class)
```

**Mistake 2: Using JUnit in commonTest**
```kotlin
// ❌ Breaks - JUnit is JVM-only
import org.junit.jupiter.api.Test

// ✅ Correct - kotlin.test is multiplatform
import kotlin.test.Test
```

**Mistake 3: Using qualifiedName in comparisons**
```kotlin
// ❌ Breaks on JS - qualifiedName throws
type.qualifiedName!!.compareTo(other.type.qualifiedName!!)

// ✅ Correct - hashCode works everywhere
type.hashCode().compareTo(other.type.hashCode())
```

**Mistake 4: Using @JvmStatic in commonMain**
```kotlin
// ❌ Breaks - @JvmStatic is JVM-only annotation
companion object {
  @JvmStatic
  fun of() = ...
}

// ✅ Correct - no annotation needed in multiplatform
companion object {
  fun of() = ...
}
```

### Integration Module Pattern

Integration modules (Jackson, Spring, etc.) remain JVM-only. They convert between framework APIs and core APIs:

**Pattern:**
```kotlin
// Framework gives Class<T>
val entityClass: Class<*> = frameworkApi.getType()

// Convert to KClass for core API
val entityKClass: KClass<*> = entityClass.kotlin

// Create TypedValue with KClass
TypedValue(rawId, entityKClass)

// When calling framework APIs, use .java
frameworkApi.register(MyType::class.java)
```

## Common Tasks

### Using Convenience Types vs Generic TypedValue

The library provides convenience classes for the most common ID types: `TypedString`, `TypedInt`, `TypedLong`, and `TypedUuid`.

**When to use convenience types:**
- ✅ Most entity IDs (users, products, orders, etc.)
- ✅ When writing Java code (better interoperability)
- ✅ When you want cleaner type signatures
- ✅ For consistency across a codebase

**When to use generic TypedValue:**
- Use `TypedValue<ID, T>` for less common ID types (BigDecimal, custom Comparable types)
- When you need to write generic functions that work with any ID type

**Examples:**

```kotlin
// Preferred: Use convenience types for common IDs
data class User(val id: TypedString<User>, val name: String)
data class Product(val id: TypedLong<Product>, val price: BigDecimal)
data class Order(val id: TypedUuid<Order>, val total: BigDecimal)

// Create with extension functions (Kotlin)
val userId = "user-123".toTypedString<User>()
val productId = 42L.toTypedLong<Product>()
val orderId = UUID.randomUUID().toTypedUuid<Order>()  // JVM-only

// Create with factory methods
val userId2 = TypedString.of("user-123", User::class)
```

```kotlin
// For uncommon types, use generic TypedValue
data class Transaction(val id: TypedValue<BigDecimal, Transaction>, val amount: Money)

val txId = TypedValue.typedValueFor(BigDecimal("12345.67"), Transaction::class)
```

**Key Points:**
- Convenience types extend `TypedValue<ID, T>` directly
- All existing APIs work with both convenience types and generic TypedValue
- Framework integrations (Jackson, Spring, QueryDSL, Elasticsearch) support both equally

### Adding Support for a New ID Type

Example: Adding support for `BigDecimal` IDs

1. **Update typed-value-core**:
   - If it uses only Kotlin stdlib → add to `commonMain`
   - If it needs java.* APIs → add to `jvmMain`
   - For BigDecimal (java.math.BigDecimal) → add to `jvmMain`

2. **Update typed-value-jackson** (JVM-only): Add case in `TypedValueDeserializer`:
   ```kotlin
   idType == BigDecimal::class.java -> BigDecimal(jsonParser.text)
   ```
   Note: Jackson uses `Class`, not `KClass`, so `::class.java` is correct here.

3. **Update typed-value-spring** (JVM-only): Add case in `StringToTypedValueConverter`:
   ```kotlin
   idType == BigDecimal::class.java -> BigDecimal(source)
   ```

4. **Update typed-value-querydsl** (JVM-only): Add case in `TypedValueExpression`:
   ```kotlin
   idType == BigDecimal::class.java ->
     pathBuilder.getNumber(ID_PATH_NAME, BigDecimal::class.java)
   ```
   Note: QueryDSL uses `Class`, so `::class.java` is correct here.

5. **Write tests** for each module affected
   - Core tests in appropriate source set (commonTest or jvmTest)
   - Integration tests in JVM-only modules

6. **Update README.md** with the new supported type and platform availability

### Adding a New Integration Module

Example: Adding `typed-value-mybatis` module

1. **Create module directory**:
   ```bash
   mkdir -p typed-value-mybatis/src/{main,test}/kotlin
   ```

2. **Add to `settings.gradle.kts`**:
   ```kotlin
   include("typed-value-mybatis")
   ```

3. **Create `build.gradle.kts`**:
   ```kotlin
   dependencies {
     api(project(":typed-value-core"))
     compileOnly("org.mybatis:mybatis:...")
   }
   ```

4. **Implement integration code**

5. **Write comprehensive tests**

6. **Document in README.md**

### Fixing a Bug

1. **Write a failing test** that reproduces the bug

2. **Fix the code** to make the test pass

3. **Run all tests** to ensure no regressions:
   ```bash
   ./gradlew test
   ```

4. **Format code**:
   ```bash
   ./gradlew spotlessApply
   ```

5. **Update CHANGELOG** (if exists)

## Architecture Decisions

### Why Generic ID Types?

The original implementation only supported `String` IDs. Generic types allow:
- Long IDs for database auto-increment
- UUID for distributed systems
- Custom comparable types

### Why Separate Modules?

- **Minimal dependencies**: Users only include what they need
- **Clear boundaries**: Framework code separate from domain
- **Independent versioning**: Could version modules separately
- **Easier testing**: Test each integration independently

### Why Kotlin-First and Multiplatform?

- **Multiplatform**: Share code across JVM, JS, Native targets
- **Better type safety**: Reified generics and compile-time checks
- **Extension functions**: Clean, idiomatic Kotlin APIs
- **Null safety**: Reduces bugs and runtime exceptions
- **KClass vs Class**: Multiplatform reflection instead of JVM-only reflection
- **Java interop**: Still usable from Java (with some conversion needed for KClass)

### Why Comparable Constraint?

`ID : Comparable<ID>` enables:
- Sorting collections of TypedValues
- Range queries in databases
- Natural ordering
- QueryDSL predicates (greater than, less than)

## Troubleshooting

### Build Fails with "Cannot resolve..."

**Cause**: Gradle didn't download dependencies properly

**Fix**:
```bash
./gradlew clean build --refresh-dependencies
```

### Spotless Check Fails

**Cause**: Code not formatted according to ktfmt Google Style

**Fix**:
```bash
./gradlew spotlessApply
```

### Detekt Fails

**Cause**: Static analysis found issues

**Fix**: Read the detekt output and fix the issues. Common ones:
- Complexity too high: Refactor large functions
- Unused imports: Remove them
- Missing documentation: Add KDoc

### Tests Fail After Changes

**Cause**: Code changes broke existing behavior

**Fix**:
1. Read the test output carefully
2. Either fix the code or update the test (if behavior change is intentional)
3. Never comment out failing tests

## Best Practices

### DO:
- ✅ Write tests for all public APIs
- ✅ Keep core module dependency-free
- ✅ Use descriptive test names with backticks
- ✅ Format code with `./gradlew spotlessApply` before committing
- ✅ Add KDoc to public APIs
- ✅ Use `@JvmStatic` for Java interop
- ✅ Update README when adding features

### DON'T:
- ❌ Add dependencies to typed-value-core
- ❌ Use mutable collections in public APIs
- ❌ Skip tests for "simple" code
- ❌ Commit without running `./gradlew build`
- ❌ Use `!!` (non-null assertion) without good reason
- ❌ Suppress warnings without explaining why
- ❌ Create circular dependencies between modules

## Release Process (Future)

When ready to release:

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Tag release: `git tag v0.1.0`
4. Build artifacts: `./gradlew build`
5. Publish to Maven Central (setup required)

## Getting Help

- **README.md**: User-facing documentation
- **This file (CLAUDE.md)**: Development guidance
- **KDoc**: API documentation in code
- **Tests**: Examples of how to use the API
