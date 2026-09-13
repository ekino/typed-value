# Typed-Value v1.5.1

Patch release with dependency updates.

## What's Changed

### Dependencies
- Kotlin 2.4.10 → 2.4.20 (kotlin group, 10 updates)
- Hibernate ORM core 7.4.5.Final → 7.4.7.Final

### Build & Infrastructure
- Spotless (`com.diffplug.spotless`) 8.10.0 → 8.10.2

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.5.1"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.5.1")
implementation("com.ekino.oss:typed-value-jackson:1.5.1")
implementation("com.ekino.oss:typed-value-spring:1.5.1")
implementation("com.ekino.oss:typed-value-hibernate:1.5.1")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.5.0...v1.5.1)
