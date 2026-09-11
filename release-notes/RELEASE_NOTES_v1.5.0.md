# Typed-Value v1.5.0

Bug-fix release making `TypedValue` actually Java-serializable, along with dependency and tooling updates.

## What's Changed

### `TypedValue` is now Java-serializable (#132, #133)

`TypedValue` has always declared `PlatformSerializable` (`java.io.Serializable` on the JVM), but serializing any instance failed at runtime with `NotSerializableException: kotlin.reflect.jvm.internal.KClassImpl`, because the entity `KClass` it holds is not `Serializable`.

This broke every JVM consumer relying on default Java serialization: Hypersistence Utils JSONB deep copies, JDK-serialized caches (JCache/Ehcache off-heap, Hazelcast, Redis with JDK serialization) and HTTP sessions holding a `TypedValue`.

The entity type is now stored in an internal, platform-specific holder. On the JVM it is backed by `java.lang.Class`, which is `Serializable`, and exposed back as a `KClass`. On JS nothing changes.

```kotlin
val id = TypedString.of("user-123", User::class)
ObjectOutputStream(out).writeObject(id) // now works
```

- Works for the **whole hierarchy**: `TypedValue`, `TypedString`, `TypedInt`, `TypedLong`, `TypedUuid` and your own subclasses such as `class UserId(id: String) : TypedString<User>(id, User::class)`.
- `equals`, `hashCode`, `compareTo` and `type` are preserved across a round trip.
- **No API change**: same constructor, `type` is still a `KClass`, source and binary compatible.

If you added a custom serializer as a workaround (e.g. a Jackson-based `JsonSerializer` for Hypersistence Utils), you can drop it.

### Dependencies

- Spring Boot 4.1.0 → 4.1.1, Spring Framework 7.0.8 → 7.0.9, Spring Data JPA 4.1.0 → 4.1.1, Spring Data Elasticsearch 6.1.0 → 6.1.1
- Jackson 3.2.1 → 3.2.2
- OpenFeign QueryDSL 7.5 → 7.6
- JUnit 6.1.2 → 6.1.3

### Build & Infrastructure

- Gradle 9.7.0 → 9.7.1
- Spotless 8.9.0 → 8.10.0
- `actions/setup-java` 5 → 6
- Fixed all open npm security alerts in `docs/` and `typescript-integration/` (vitest 4, vite 7, overrides for transitive dependencies); `typescript-integration` is now monitored by Dependabot

## Installation

### Using the BOM (recommended)
```kotlin
dependencies {
    implementation(platform("com.ekino.oss:typed-value-bom:1.5.0"))
    implementation("com.ekino.oss:typed-value-core")
    implementation("com.ekino.oss:typed-value-jackson")
    implementation("com.ekino.oss:typed-value-spring")
    implementation("com.ekino.oss:typed-value-hibernate")
}
```

### Without the BOM
```kotlin
implementation("com.ekino.oss:typed-value-core:1.5.0")
implementation("com.ekino.oss:typed-value-jackson:1.5.0")
implementation("com.ekino.oss:typed-value-spring:1.5.0")
implementation("com.ekino.oss:typed-value-hibernate:1.5.0")
implementation("com.ekino.oss:typed-value-querydsl:1.5.0")
implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:1.5.0")
```

## Links
- [Documentation](https://ekino.github.io/typed-value/)
- [GitHub](https://github.com/ekino/typed-value)
- [Maven Central](https://central.sonatype.com/search?q=com.ekino.oss.typed-value)
- [Full Changelog](https://github.com/ekino/typed-value/compare/v1.4.1...v1.5.0)
