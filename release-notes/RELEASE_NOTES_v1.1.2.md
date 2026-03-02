# Typed-Value v1.1.2

Patch release with dependency updates and a Jackson 3.1.0 compatibility fix.

## What's Changed

### Bug Fixes
- Handle Jackson 3.1.0 wildcard type resolution change in `TypedValueDeserializer` — Jackson 3.1.0 now resolves star-projected bounded type parameters to their upper bound (`Comparable`) instead of `Any`, which required an additional check when validating unresolved value type parameters.

### Dependencies
- Jackson 3.0.4 → 3.1.0
- Hibernate ORM 7.2.5.Final → 7.2.6.Final
- actions/upload-artifact 6 → 7 (CI)

## Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)
```kotlin
implementation("com.ekino.oss:typed-value-core:1.1.2")
```

### JVM with Framework Integrations
```kotlin
implementation("com.ekino.oss:typed-value-core:1.1.2")
implementation("com.ekino.oss:typed-value-jackson:1.1.2")
implementation("com.ekino.oss:typed-value-spring:1.1.2")
implementation("com.ekino.oss:typed-value-hibernate:1.1.2")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.1.1...v1.1.2)
