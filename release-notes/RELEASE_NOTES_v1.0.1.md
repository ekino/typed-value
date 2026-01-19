# Typed-Value v1.0.1

Patch release with dependency updates and build improvements.

## What's Changed

### Build & Infrastructure
- Upgraded Gradle from 8.14 to 9.3.0
- Bumped `com.vanniktech.maven.publish` plugin

### Dependencies
- Updated Spring dependencies (spring-web, spring-data-elasticsearch, spring-boot-autoconfigure)
- Updated Hibernate ORM core
- Updated Kotlin documentation references

## Installation

### Kotlin Multiplatform (Gradle Kotlin DSL)
```kotlin
implementation("com.ekino.oss:typed-value-core:1.0.1")
```

### JVM with Framework Integrations
```kotlin
implementation("com.ekino.oss:typed-value-core:1.0.1")
implementation("com.ekino.oss:typed-value-jackson:1.0.1")
implementation("com.ekino.oss:typed-value-spring:1.0.1")
implementation("com.ekino.oss:typed-value-hibernate:1.0.1")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
