# Typed-Value v1.4.1

Patch release with dependency updates.

## What's Changed

### Dependencies
- OpenFeign QueryDSL 7.4.0 → 7.5

### Build & Infrastructure
- Gradle 9.6.1 → 9.7.0
- Spotless (`com.diffplug.spotless`) bump in the code-quality group

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.4.1"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.4.1")
implementation("com.ekino.oss:typed-value-jackson:1.4.1")
implementation("com.ekino.oss:typed-value-spring:1.4.1")
implementation("com.ekino.oss:typed-value-hibernate:1.4.1")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.4.0...v1.4.1)
