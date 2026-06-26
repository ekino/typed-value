# Typed-Value v1.3.3

Patch release with dependency updates.

## What's Changed

### Dependencies
- Kotlin 2.3.21 → 2.4.0
- Spring Boot 4.0.6 → 4.1.0
- Spring Framework 7.0.7 → 7.0.8
- Spring Data JPA 4.0.5 → 4.1.0
- Spring Data Elasticsearch 6.0.5 → 6.1.0
- Hibernate ORM 7.4.0.Final → 7.4.2.Final
- Jackson 3.1.4 → 3.2.0
- OpenFeign QueryDSL 7.2 → 7.3.0
- Datafaker 2.5.4 → 2.6.0

### Build & Infrastructure
- Gradle 9.5.1 → 9.6.0
- Spotless 8.6.0 → 8.7.0
- Maven Publish plugin 0.36.0 → 0.37.0
- `actions/checkout` v6 → v7
- Pinned the JVM target (toolchain + detekt) so the project builds with any JDK ≥ 21, not just the JDK matching CI

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.3.3"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.3")
implementation("com.ekino.oss:typed-value-jackson:1.3.3")
implementation("com.ekino.oss:typed-value-spring:1.3.3")
implementation("com.ekino.oss:typed-value-hibernate:1.3.3")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.3.2...v1.3.3)
