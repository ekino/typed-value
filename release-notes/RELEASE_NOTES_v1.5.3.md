# Typed-Value v1.5.3

Patch release with dependency updates and build maintenance.

## What's Changed

### Dependencies
- Spring Boot 4.1.2 → 4.2.0 (spring group, 13 updates)
- Spring Framework 7.0.10 → 7.1.0 (`spring-core`, `spring-context`, `spring-webmvc`)
- Spring Data JPA 4.1.2 → 4.2.0
- Spring Data Elasticsearch 6.1.2 → 6.2.0
- Jackson 3.2.3 → 3.3.0
- Hibernate ORM core 7.4.13.Final → 7.4.17.Final
- MockK 1.14.11 → 1.14.12

### Build & Infrastructure
- Gradle wrapper 9.8.2 → 9.9.2
- Spotless (`com.diffplug.spotless`) 8.10.4 → 8.11.0

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.5.3"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.5.3")
implementation("com.ekino.oss:typed-value-jackson:1.5.3")
implementation("com.ekino.oss:typed-value-spring:1.5.3")
implementation("com.ekino.oss:typed-value-hibernate:1.5.3")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.5.2...v1.5.3)
