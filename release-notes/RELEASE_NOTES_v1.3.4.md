# Typed-Value v1.3.4

Patch release with dependency updates.

## What's Changed

### Dependencies
- Hibernate ORM 7.4.2.Final → 7.4.3.Final
- OpenFeign QueryDSL 7.3.0 → 7.4.0
- Datafaker 2.6.0 → 2.7.0
- JUnit Jupiter 6.1.0 → 6.1.1

### Build & Infrastructure
- Gradle 9.6.0 → 9.6.1
- `actions/cache` v5 → v6

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.3.4"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.4")
implementation("com.ekino.oss:typed-value-jackson:1.3.4")
implementation("com.ekino.oss:typed-value-spring:1.3.4")
implementation("com.ekino.oss:typed-value-hibernate:1.3.4")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.3.3...v1.3.4)
