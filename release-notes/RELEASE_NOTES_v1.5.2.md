# Typed-Value v1.5.2

Patch release with dependency updates and build maintenance.

## What's Changed

### Dependencies
- Kotlin 2.4.20 → 2.4.21 (kotlin group, 10 updates)
- Spring Boot 4.1.1 → 4.1.2 (spring group, 13 updates)
- Spring Framework 7.0.9 → 7.0.10 (`spring-core`, `spring-context`, `spring-webmvc`)
- Spring Data JPA 4.1.1 → 4.1.2
- Spring Data Elasticsearch 6.1.1 → 6.1.2
- Jackson 3.2.2 → 3.2.3
- Hibernate ORM core 7.4.7.Final → 7.4.13.Final
- QueryDSL Feign 7.6 → 7.7

### Build & Infrastructure
- Gradle wrapper 9.7.1 → 9.8.2
- Spotless (`com.diffplug.spotless`) 8.10.2 → 8.10.4
- Fixed Gradle 10 deprecations and dropped an unused compiler flag

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.5.2"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.5.2")
implementation("com.ekino.oss:typed-value-jackson:1.5.2")
implementation("com.ekino.oss:typed-value-spring:1.5.2")
implementation("com.ekino.oss:typed-value-hibernate:1.5.2")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.5.1...v1.5.2)
