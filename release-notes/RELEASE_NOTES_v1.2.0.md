# Typed-Value v1.2.0

Minor release adding interface support for Jackson deserialization, along with dependency updates.

## What's New

### Jackson: Interface Entity Type Support

TypedValue fields using an interface as the entity type parameter (e.g., `TypedValue<String, SomeInterface>`) now deserialize correctly. Previously, the deserializer rejected interfaces because it only validated non-`Any` classes. This release adds an `isInterface` check so interfaces are accepted as valid entity types.

```kotlin
interface Identifiable

data class MyDto(val id: TypedString<Identifiable>)

// Now works — previously threw an error
val dto = mapper.readValue<MyDto>("""{"id":"abc"}""")
```

## Dependencies

- Kotlin group: 10 updates
- Spring group: 5 updates
- Hibernate ORM updated
- Spotless (code-quality group) updated
- Gradle Wrapper 9.3.1 → 9.4.0

## Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)
```kotlin
implementation("com.ekino.oss:typed-value-core:1.2.0")
```

### JVM with Framework Integrations
```kotlin
implementation("com.ekino.oss:typed-value-core:1.2.0")
implementation("com.ekino.oss:typed-value-jackson:1.2.0")
implementation("com.ekino.oss:typed-value-spring:1.2.0")
implementation("com.ekino.oss:typed-value-hibernate:1.2.0")
implementation("com.ekino.oss:typed-value-querydsl:1.2.0")
implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.2.0")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.1.2...v1.2.0)
