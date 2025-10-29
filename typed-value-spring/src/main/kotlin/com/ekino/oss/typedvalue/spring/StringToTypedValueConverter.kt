/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.spring

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.lang.reflect.ParameterizedType
import java.util.UUID
import kotlin.reflect.KClass
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.support.DefaultConversionService

/**
 * Spring Converter that converts String values to TypedValue with resolved types.
 *
 * This converter is used for:
 * - @PathVariable TypedValue<String, Entity> parameters
 * - @RequestParam TypedValue<String, Entity> parameters
 *
 * The entity type is resolved from the parameter's generic type information.
 *
 * Example usage:
 * ```kotlin
 * @GetMapping("/users/{id}")
 * fun getUser(@PathVariable id: TypedValue<String, User>): User {
 *   // id will be automatically converted from the path variable string
 * }
 * ```
 */
class StringToTypedValueConverter : GenericConverter {
  private val defaultConversionService = DefaultConversionService()

  override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> =
    setOf(
      GenericConverter.ConvertiblePair(String::class.java, TypedValue::class.java),
      GenericConverter.ConvertiblePair(String::class.java, TypedString::class.java),
      GenericConverter.ConvertiblePair(String::class.java, TypedInt::class.java),
      GenericConverter.ConvertiblePair(String::class.java, TypedLong::class.java),
      GenericConverter.ConvertiblePair(String::class.java, TypedUuid::class.java),
    )

  @Suppress("UNCHECKED_CAST")
  override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
    if (source == null) return null
    if (source !is String) {
      throw UnsupportedOperationException("Source must be a String for TypedValue conversion")
    }

    return when (targetType.type.kotlin) {
      TypedUuid::class -> {
        val entityType = resolveEntityType(targetType, TypedUuid::class)
        TypedUuid(source.toId(UUID::class), entityType)
      }

      TypedString::class -> {
        val entityType = resolveEntityType(targetType, TypedString::class)
        TypedString(source, entityType)
      }

      TypedInt::class -> {
        val entityType = resolveEntityType(targetType, TypedInt::class)
        TypedInt(source.toId(Int::class), entityType)
      }

      TypedLong::class -> {
        val entityType = resolveEntityType(targetType, TypedLong::class)
        TypedLong(source.toId(Long::class), entityType)
      }

      TypedValue::class -> {
        // Extract ID type (first type parameter)
        val paramType =
          targetType.resolvableType.type as? ParameterizedType
            ?: throw IllegalArgumentException(
              "Conversion from String to TypedValue failed. " +
                "TypedValue must have explicit generic type parameters."
            )

        val idType =
          paramType.actualTypeArguments.getOrNull(0) as? Class<*>
            ?: throw IllegalArgumentException(
              "Conversion from String to TypedValue failed. " +
                "ID type parameter could not be resolved."
            )

        val entityType =
          paramType.actualTypeArguments.getOrNull(1) as? Class<*>
            ?: throw IllegalArgumentException(
              "Conversion from String to TypedValue failed. " +
                "Entity type parameter could not be resolved."
            )

        val rawId: Comparable<Any> = source.toId(idType.kotlin as KClass<Comparable<Any>>)

        TypedValue(rawId, (entityType as Class<Any>).kotlin)
      }

      else -> defaultConversionService.convert(source, sourceType, targetType)
    }
  }

  /**
   * Resolves the entity type (first type parameter) from a variant TypedValue class. Variants like
   * TypedUuid<T>, TypedString<T>, etc. have only one type parameter.
   */
  private fun resolveEntityType(targetType: TypeDescriptor, type: KClass<*>): KClass<out Any> {
    val paramType =
      targetType.resolvableType.type as? ParameterizedType
        ?: throw IllegalArgumentException(
          "Conversion from String to ${type.simpleName} failed. " +
            "${type.simpleName} must have explicit generic type parameter."
        )

    return (paramType.actualTypeArguments.getOrNull(0) as? Class<*>)?.kotlin
      ?: throw IllegalArgumentException(
        "Conversion from String to ${type.simpleName} failed. " +
          "Entity type parameter could not be resolved."
      )
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T : Comparable<T>> String.toId(expected: KClass<T>): T =
    when (expected) {
      String::class -> this
      Long::class -> this.toLongOrNull()
      Int::class -> this.toIntOrNull()
      UUID::class -> runCatching { UUID.fromString(this) }.getOrNull()
      else -> defaultConversionService.convert(this, expected.java)
    }
      as? T
      ?: throw IllegalArgumentException(
        "Conversion from String to TypedValue failed. " +
          "`$this` could not be converted to ${expected.simpleName}."
      )
}
