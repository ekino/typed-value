# Typed-Value v1.3.2

Patch release with dependency updates.

## What's Changed

### Dependencies
- Hibernate ORM 7.3.2.Final → 7.4.0.Final
- Jackson 3.1.2 → 3.1.4
- OpenFeign QueryDSL 7.1 → 7.2
- JUnit Jupiter 6.0.3 → 6.1.0
- MockK 1.14.9 → 1.14.11

### Build & Infrastructure
- Gradle 9.4.1 → 9.5.1
- Spotless 8.4.0 → 8.6.0

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.3.2"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.3.2")
implementation("com.ekino.oss:typed-value-jackson:1.3.2")
implementation("com.ekino.oss:typed-value-spring:1.3.2")
implementation("com.ekino.oss:typed-value-hibernate:1.3.2")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.3.1...v1.3.2)
