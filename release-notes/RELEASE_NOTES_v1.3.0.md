# Typed-Value v1.3.0

Feature release adding `java.io.Serializable` support to `TypedValue`, along with dependency updates.

## What's New

### Serializable Support

`TypedValue` now implements `java.io.Serializable` on JVM, enabling compatibility with frameworks that require serializable object graphs — such as Hypersistence Utils for JPA JSON column deep copy operations.

This is implemented using the Kotlin Multiplatform `expect`/`actual` pattern:
- **JVM**: `TypedValue` implements `java.io.Serializable` with a stable `serialVersionUID`
- **JS**: empty marker interface (no-op)

All subclasses (`TypedString`, `TypedInt`, `TypedLong`, `TypedUuid`) inherit `Serializable` automatically.

```kotlin
// TypedValue instances are now Serializable on JVM
val userId: TypedString<User> = "user-123".toTypedString()
assertThat(userId).isInstanceOf(java.io.Serializable::class.java) // passes
```

## Dependencies

- Jackson 3.1.0 → 3.1.1
- Spring group: 2 rounds of updates (16 total)
- Hibernate ORM updated
- Testcontainers PostgreSQL updated
- Spotless (code-quality group) updated
- Gradle Wrapper 9.4.0 → 9.4.1
- GitHub Actions: configure-pages 5 → 6, deploy-pages 4 → 5, test-reporter 2 → 3

## Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.0")
```

### JVM with Framework Integrations
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.0")
implementation("com.ekino.oss:typed-value-jackson:1.3.0")
implementation("com.ekino.oss:typed-value-spring:1.3.0")
implementation("com.ekino.oss:typed-value-hibernate:1.3.0")
implementation("com.ekino.oss:typed-value-querydsl:1.3.0")
implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.3.0")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.2.1...v1.3.0)
