/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

/**
 * Jackson module for TypedValue serialization and deserialization.
 *
 * Supports all TypedValue types including convenience types:
 * - TypedValue<ID, T>
 * - TypedUuid<T>
 * - TypedString<T>
 * - TypedLong<T>
 * - TypedInt<T>
 *
 * ## Spring Boot
 *
 * Simply register the module as a bean. Spring Boot auto-configures Jackson and picks up any
 * [tools.jackson.databind.JacksonModule] beans:
 * ```kotlin
 * @Configuration
 * class JacksonConfiguration {
 *     @Bean
 *     fun typedValueModule() = TypedValueModule()
 * }
 * ```
 *
 * ## Standalone (non-Spring Boot)
 *
 * Jackson 3.x uses immutable mappers with a builder pattern.
 *
 * Using the Kotlin DSL:
 * ```kotlin
 * val objectMapper = jsonMapper {
 *   addModule(kotlinModule())
 *   addModule(TypedValueModule())
 * }
 * ```
 *
 * Using the builder directly:
 * ```kotlin
 * val objectMapper = JsonMapper.builder()
 *   .addModule(kotlinModule())
 *   .addModule(TypedValueModule())
 *   .build()
 * ```
 */
@Suppress("UNCHECKED_CAST")
class TypedValueModule : SimpleModule("TypedValueModule") {
  init {
    // Serializer handles all TypedValue subtypes via polymorphism
    addSerializer(TypedValue::class.java, TypedValueSerializer())

    // Register deserializer for base type and all convenience types
    // The deserializer uses contextual type info to return the correct subtype
    val deserializer = TypedValueDeserializer()
    addDeserializer(TypedValue::class.java, deserializer)
    addDeserializer(TypedUuid::class.java, deserializer as ValueDeserializer<TypedUuid<*>>)
    addDeserializer(TypedString::class.java, deserializer as ValueDeserializer<TypedString<*>>)
    addDeserializer(TypedLong::class.java, deserializer as ValueDeserializer<TypedLong<*>>)
    addDeserializer(TypedInt::class.java, deserializer as ValueDeserializer<TypedInt<*>>)
  }
}
