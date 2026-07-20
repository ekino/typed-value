# Typed-Value v1.4.0

Feature release adding a Spring converter for rendering typed values, along with dependency updates.

## What's New

### Rendering `TypedValue` as a path variable / request param

The `typed-value-spring` module now includes `TypedValueToStringConverter`, a Spring `Converter<TypedValue<*, *>, String>` that renders a `TypedValue` as its raw id. This complements the existing `StringToTypedValueConverter` (`String` → `TypedValue`), so typed values now convert cleanly in **both directions** when used with Spring MVC and HTTP interface clients.

It is registered automatically by `TypedValueAutoConfiguration` — no manual setup required.

```kotlin
val userId: TypedString<User> = "user-123".toTypedString()

// Spring now renders userId as "user-123" when used as a
// @PathVariable / @RequestParam (e.g. in an HTTP interface client call)
```

Works with all supported id types: `TypedString`, `TypedInt`, `TypedLong`, `TypedUuid`, and generic `TypedValue`.

## Dependencies

- Kotlin group: 10 updates
- Jackson 3.2.0 → 3.2.1
- Hibernate ORM: 2 rounds of updates
- Testing group: 2 updates
- Spotless (code-quality group) updated
- GitHub Actions: `setup-node` 4 → 7

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.4.0"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.4.0")
implementation("com.ekino.oss:typed-value-jackson:1.4.0")
implementation("com.ekino.oss:typed-value-spring:1.4.0")
implementation("com.ekino.oss:typed-value-hibernate:1.4.0")
implementation("com.ekino.oss:typed-value-querydsl:1.4.0")
implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.4.0")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.3.4...v1.4.0)
