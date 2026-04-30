# Typed-Value v1.3.1

Patch release with dependency updates.

## What's Changed

### Dependencies
- Kotlin 2.3.20 → 2.3.21
- Spring Boot 4.0.5 → 4.0.6
- Spring Framework 7.0.6 → 7.0.7
- Spring Data JPA 4.0.4 → 4.0.5
- Spring Data Elasticsearch 6.0.4 → 6.0.5
- Hibernate ORM 7.3.0.Final → 7.3.2.Final
- Jackson 3.1.1 → 3.1.2
- Testcontainers PostgreSQL 2.0.4 → 2.0.5

### Build & Infrastructure
- actions/upload-pages-artifact 3 → 5 (CI)
- softprops/action-gh-release 2 → 3 (CI)

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.3.1"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.1")
implementation("com.ekino.oss:typed-value-jackson:1.3.1")
implementation("com.ekino.oss:typed-value-spring:1.3.1")
implementation("com.ekino.oss:typed-value-hibernate:1.3.1")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.3.0...v1.3.1)
