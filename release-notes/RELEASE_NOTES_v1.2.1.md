# Typed-Value v1.2.1

Minor release adding a BOM module for simplified dependency management, along with build improvements.

## What's New

### BOM (Bill of Materials)
- New `typed-value-bom` module — declare a single versioned `platform()` dependency and omit versions on individual modules:
  ```kotlin
  dependencies {
      implementation(platform("com.ekino.oss:typed-value-bom:1.2.1"))
      implementation("com.ekino.oss:typed-value-core")
      implementation("com.ekino.oss:typed-value-jackson")
  }
  ```

### Build & Infrastructure
- Enabled detekt static analysis for the integration-tests module
- Removed redundant `allOpen` configuration now handled by `kotlin.plugin.jpa`

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.2.1"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.2.1")
implementation("com.ekino.oss:typed-value-jackson:1.2.1")
implementation("com.ekino.oss:typed-value-spring:1.2.1")
implementation("com.ekino.oss:typed-value-hibernate:1.2.1")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.2.0...v1.2.1)
