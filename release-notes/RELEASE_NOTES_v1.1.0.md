# Typed-Value v1.1.0

Feature release adding custom TypedValue registration support across all framework integrations, enabling domain-specific strongly-typed ID types beyond the built-in convenience types.

## 🎯 What's New

### Custom TypedValue Registration

Version 1.1.0 introduces a powerful registration mechanism that allows you to create and use custom TypedValue subtypes throughout your application. This enables domain-specific ID types with custom behavior while maintaining full framework integration support.

**Key Benefits:**
- ✅ Define domain-specific ID types (e.g., `PersonNameTyped`, `CompanyId`)
- ✅ Preserve custom type information through serialization/deserialization
- ✅ Type-safe registration with compile-time validation
- ✅ Seamless integration with Jackson, Elasticsearch, and QueryDSL
- ✅ Zero runtime overhead for built-in types

## 🚀 Major Features

### Jackson Integration (Custom Type Registration)

Register custom TypedValue subtypes with Jackson for proper JSON serialization/deserialization:

```kotlin
// Define custom type
class CompanyId(id: String, type: KClass<T>) : TypedString<T>(id, type)

// Register with Jackson
val mapper = jsonMapper {
    addModule(kotlinModule())
    addModule(TypedValueModule().apply {
        registerCustomTypedValue<CompanyId, String> { value, entityKClass ->
            CompanyId(value, entityKClass)
        }
    })
}

// Use in DTOs
data class CompanyDto(val id: CompanyId<Company>)

// Deserializes correctly as CompanyId, not generic TypedString
val dto = mapper.readValue<CompanyDto>(json)
assertThat(dto.id).isInstanceOf<CompanyId>()
```

**Jackson Improvements:**
- Improved type hierarchy resolution using generic superclass walking
- Better validation with clear error messages for unsupported types
- Defensive null checking in type resolution
- Three registration API variants (Kotlin DSL, Java-style, functional interface)
- Comprehensive test coverage (45+ tests including edge cases)

### Elasticsearch Integration (Custom Type Registration)

Register custom TypedValue types for proper Elasticsearch document mapping:

```kotlin
@Configuration
class ElasticsearchConfig : ElasticsearchConfiguration() {
    override fun elasticsearchMappingContext(): TypedValueElasticsearchMappingContext {
        return TypedValueElasticsearchMappingContext().apply {
            registerCustomTypedValue<PersonNameTyped, String> { value, entityKClass ->
                PersonNameTyped(value, entityKClass)
            }
        }
    }
}
```

**Elasticsearch Features:**
- Thread-safe registry with ConcurrentHashMap
- Compile-time type safety with generic VALUE parameter
- Validation of incoming value types
- No order dependencies - registration works before or after entity scanning
- Full backward compatibility with existing code

### QueryDSL Integration (Constructor-Based Support)

Use custom constructors in QueryDSL expressions:

```kotlin
class PersonNameTyped(name: String, type: KClass<T>) : TypedString<T>(name, type)

// Create QueryDSL expression with custom constructor
val qPerson = QPerson.person
val nameExpression = qPerson.name.typedValueExpressionOf(Person::class) { value, kClass ->
    PersonNameTyped(value, kClass)
}

// Use in queries
val results = queryFactory
    .selectFrom(qPerson)
    .where(nameExpression.eq(PersonNameTyped.of("John Doe", Person::class)))
    .fetch()
```

**QueryDSL Features:**
- Per-expression custom type support
- Constructor-based instantiation
- Type-safe factory method `typedValueExpressionOf()`
- Integration tests with custom types

## 🔧 Improvements

### Jackson Module
- **Better Type Resolution**: Rewrote `createContextual()` to use generic type hierarchy walking instead of hardcoded type checks
- **Enhanced Validation**: Upfront validation of TypedValue subtypes with helpful error messages
- **Defensive Programming**: Added null safety checks in `getTypedValueClass()` recursion
- **Error Messages**: Improved formatting using `trimMargin()` for better readability
- **Test Coverage**: Added 8 comprehensive tests:
  - Unregistered custom TypedValue subtype validation
  - Nullable TypedValue fields (single and multiple)
  - Nested DTOs with TypedValue (simple and deeply nested)
  - Type hierarchy resolution for all convenience types
  - Custom TypedValue extending convenience types

### Elasticsearch Module
- Removed order-dependent `SimpleTypeHolder` manipulation
- Added thread-safe registry locking at Spring initialization
- Override `setSimpleTypeHolder()` to safely merge user types
- Comprehensive unit tests (25 tests) and integration tests

### QueryDSL Module
- Added `Path.typedValueExpressionOf()` factory method
- Per-expression custom TypedValue instantiation
- 5 new unit tests + integration tests

### Core Module
- Added `@JvmStatic` to `TypedValue.rawIds()` methods for better Java interop

## 📚 Documentation

- **New Sections**: Comprehensive custom type documentation in all integration guides
- **Examples**: Complete examples for Kotlin and Java usage
- **Best Practices**: Guidelines for registration, validation, and error handling
- **API Comparison**: Side-by-side comparison of registration API variants
- **Design Rationale**: Explanation of architectural decisions

## 🔄 Migration Guide

### From v1.0.x to v1.1.0

**No Breaking Changes** - This is a fully backward-compatible feature release.

**Optional: Migrate to Custom Types**

If you were using generic `TypedString<T>` or subclassing convenience types, you can now register them for proper type preservation:

```kotlin
// Before v1.1.0 (still works, but loses type information)
data class Order(val userId: TypedString<User>)
// Deserializes as TypedString

// After v1.1.0 (with registration)
class UserId(id: String, type: KClass<T>) : TypedString<T>(id, type)

TypedValueModule().apply {
    registerCustomTypedValue<UserId, String> { value, entityKClass ->
        UserId(value, entityKClass)
    }
}

data class Order(val userId: UserId<User>)
// Deserializes as UserId (preserves custom type)
```

## 📦 Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)
```kotlin
implementation("com.ekino.oss:typed-value-core:1.1.0")
```

### JVM with Framework Integrations
```kotlin
implementation("com.ekino.oss:typed-value-core:1.1.0")
implementation("com.ekino.oss:typed-value-jackson:1.1.0")
implementation("com.ekino.oss:typed-value-spring:1.1.0")
implementation("com.ekino.oss:typed-value-hibernate:1.1.0")
implementation("com.ekino.oss:typed-value-querydsl:1.1.0")
implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.1.0")
```

## 🔗 Links

- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)

## 🙏 Contributors

Thanks to all contributors who made this release possible!

---

**Full Changelog**: https://github.com/ekino/typed-value/compare/v1.0.1...v1.1.0
