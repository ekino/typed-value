/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.elasticsearch

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty
import org.springframework.data.elasticsearch.core.mapping.PropertyValueConverter
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity
import org.springframework.data.mapping.model.Property
import org.springframework.data.mapping.model.SimpleTypeHolder

/**
 * Custom Elasticsearch mapping context that handles TypedValue serialization.
 *
 * This mapping context automatically configures PropertyValueConverters for TypedValue fields,
 * enabling seamless storage and retrieval of TypedValues in Elasticsearch documents.
 *
 * Features:
 * - Automatically detects TypedValue fields
 * - Resolves generic type parameters at compile time
 * - Converts TypedValue to raw ID for storage
 * - Reconstructs TypedValue from raw ID on retrieval
 * - Supports Collection<TypedValue> (Lists only, not Sets or Arrays)
 *
 * Usage:
 * ```kotlin
 * @Configuration
 * class ElasticsearchConfig : ElasticsearchConfigurationSupport() {
 *
 *   override fun elasticsearchMappingContext(): ElasticsearchMappingContext {
 *     return TypedValueElasticsearchMappingContext()
 *   }
 * }
 * ```
 *
 * Or with Spring Boot autoconfiguration, register as a Bean:
 * ```kotlin
 * @Bean
 * fun elasticsearchMappingContext(): ElasticsearchMappingContext {
 *   return TypedValueElasticsearchMappingContext()
 * }
 * ```
 *
 * Example document:
 * ```kotlin
 * @Document(indexName = "users")
 * data class UserDocument(
 *   val id: TypedValue<String, User>,
 *   val friendIds: List<TypedValue<String, User>>
 * )
 * ```
 */
class TypedValueElasticsearchMappingContext : SimpleElasticsearchMappingContext() {

  init {
    // Register TypedValue and its subclasses as simple types
    // This prevents Spring Data from trying to create PersistentEntity for them
    setSimpleTypeHolder(
      SimpleTypeHolder(
        setOf(
          TypedValue::class.java,
          TypedString::class.java,
          TypedInt::class.java,
          TypedLong::class.java,
          TypedUuid::class.java,
        ),
        true,
      )
    )
  }

  @Suppress("LongMethod", "CyclomaticComplexMethod")
  override fun createPersistentProperty(
    property: Property,
    owner: SimpleElasticsearchPersistentEntity<*>,
    simpleTypeHolder: SimpleTypeHolder,
  ): ElasticsearchPersistentProperty {
    val persistentProperty: ElasticsearchPersistentProperty =
      super.createPersistentProperty(property, owner, simpleTypeHolder)

    if (
      !persistentProperty.hasPropertyValueConverter() &&
        persistentProperty.isTypedValueOrCollectionOf()
    ) {
      // Validate: reject Arrays
      if (persistentProperty.isArray) {
        throw UnsupportedOperationException(
          "Arrays of TypedValue are not supported. " +
            "Caused by property: ${owner.type.name}#${property.name}"
        )
      }

      // Validate: reject Sets
      if (
        persistentProperty.isCollectionLike &&
          Set::class.java.isAssignableFrom(persistentProperty.rawType)
      ) {
        throw UnsupportedOperationException(
          "Sets of TypedValue are not supported. " +
            "Caused by property: ${owner.type.name}#${property.name}"
        )
      }

      val propertyTypeInformation = owner.typeInformation.getRequiredProperty(property.name)

      // Determine the actual TypedValue type (handling collections)
      val typedValueType =
        if (persistentProperty.isCollectionLike) {
          propertyTypeInformation.actualType
        } else {
          propertyTypeInformation
        }

      val typeArgs = typedValueType?.typeArguments ?: emptyList()
      val rawClass = typedValueType?.type

      // Convenience types (TypedUuid, TypedString, TypedLong, TypedInt) have 1 type param: T
      // Generic TypedValue has 2 type params: ID, T
      val isConvenienceType =
        rawClass != null &&
          (TypedUuid::class.java.isAssignableFrom(rawClass) ||
            TypedString::class.java.isAssignableFrom(rawClass) ||
            TypedLong::class.java.isAssignableFrom(rawClass) ||
            TypedInt::class.java.isAssignableFrom(rawClass))

      // Extract entity type (T)
      val entityTypeIndex = if (isConvenienceType) 0 else 1
      val typedIdEntityType =
        typeArgs.getOrNull(entityTypeIndex)?.type?.takeUnless { it == Any::class.java }
          ?: throw UnsupportedOperationException(
            "Actual type of TypedValue could not be resolved for property: " +
              "${owner.type.name}#${property.name}"
          )

      // Extract ID type - for convenience types, it's known from the class
      val typedIdType =
        when {
          rawClass != null && TypedUuid::class.java.isAssignableFrom(rawClass) -> UUID::class.java
          rawClass != null && TypedString::class.java.isAssignableFrom(rawClass) ->
            String::class.java
          rawClass != null && TypedLong::class.java.isAssignableFrom(rawClass) -> Long::class.java
          rawClass != null && TypedInt::class.java.isAssignableFrom(rawClass) -> Int::class.java
          else -> typeArgs.getOrNull(0)?.type ?: String::class.java
        }

      return TypedValueElasticPersistentPropertyWithConverter(
        persistentProperty,
        typedIdEntityType,
        typedIdType,
        rawClass,
      )
    }

    return persistentProperty
  }

  private fun ElasticsearchPersistentProperty.isTypedValueOrCollectionOf(): Boolean {
    val targetType = if (isCollectionLike) actualType else type
    return TypedValue::class.java.isAssignableFrom(targetType)
  }
}

/**
 * Persistent property with custom PropertyValueConverter for TypedValue fields.
 *
 * This class wraps an ElasticsearchPersistentProperty and adds bidirectional conversion between
 * TypedValue and raw ID values for Elasticsearch storage.
 */
class TypedValueElasticPersistentPropertyWithConverter(
  private val property: ElasticsearchPersistentProperty,
  private val typedIdEntityType: Class<*>,
  private val typedIdType: Class<*>,
  private val typedValueClass: Class<*>?,
) : ElasticsearchPersistentProperty by property, PropertyValueConverter {

  override fun hasPropertyValueConverter(): Boolean = true

  override fun getPropertyValueConverter(): PropertyValueConverter = this

  /**
   * Convert TypedValue to raw ID value for storage in Elasticsearch.
   *
   * Converts to types that Elasticsearch natively supports:
   * - String, Long, Int, Short, Byte, Double, Float, Boolean are passed through
   * - UUID and other types are converted to String
   *
   * @param value The TypedValue instance
   * @return The raw ID value compatible with Elasticsearch
   */
  override fun write(value: Any): Any {
    val typedId = value as TypedValue<*, *>
    return when (val rawValue = typedId.value) {
      is String -> rawValue
      is Long -> rawValue
      is Int -> rawValue
      is Short -> rawValue
      is Byte -> rawValue
      is Double -> rawValue
      is Float -> rawValue
      is Boolean -> rawValue
      else -> rawValue.toString()
    }
  }

  /**
   * Convert raw ID value from Elasticsearch back to TypedValue.
   *
   * @param value The raw ID value from Elasticsearch
   * @return A TypedValue instance with the correct type information
   */
  @Suppress("UNCHECKED_CAST", "LongMethod", "CyclomaticComplexMethod")
  override fun read(value: Any): Any {
    // Convert value to appropriate ID type using KClass for cleaner comparison
    val rawId: Comparable<*> =
      when (typedIdType.kotlin) {
        String::class -> value.toString()
        Long::class -> {
          when (value) {
            is Number -> value.toLong()
            is String -> value.toLong()
            else -> throw illegalConversion(value, "Long")
          }
        }
        Int::class -> {
          when (value) {
            is Number -> value.toInt()
            is String -> value.toInt()
            else -> throw illegalConversion(value, "Int")
          }
        }
        Short::class -> {
          when (value) {
            is Number -> value.toShort()
            is String -> value.toShort()
            else -> throw illegalConversion(value, "Short")
          }
        }
        Byte::class -> {
          when (value) {
            is Number -> value.toByte()
            is String -> value.toByte()
            else -> throw illegalConversion(value, "Byte")
          }
        }
        Double::class -> {
          when (value) {
            is Number -> value.toDouble()
            is String -> value.toDouble()
            else -> throw illegalConversion(value, "Double")
          }
        }
        Float::class -> {
          when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloat()
            else -> throw illegalConversion(value, "Float")
          }
        }
        Boolean::class -> {
          when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> throw illegalConversion(value, "Boolean")
          }
        }
        UUID::class -> {
          when (value) {
            is UUID -> value
            is String -> UUID.fromString(value)
            else -> throw illegalConversion(value, "UUID")
          }
        }
        else -> value.toString() // Default to string for other types
      }

    val entityKClass = (typedIdEntityType as Class<Any>).kotlin

    // Create the correct concrete type based on the property's TypedValue class
    return when {
      typedValueClass != null && TypedUuid::class.java.isAssignableFrom(typedValueClass) ->
        TypedUuid(rawId as UUID, entityKClass)
      typedValueClass != null && TypedString::class.java.isAssignableFrom(typedValueClass) ->
        TypedString(rawId as String, entityKClass)
      typedValueClass != null && TypedLong::class.java.isAssignableFrom(typedValueClass) ->
        TypedLong(rawId as Long, entityKClass)
      typedValueClass != null && TypedInt::class.java.isAssignableFrom(typedValueClass) ->
        TypedInt(rawId as Int, entityKClass)
      else -> TypedValue.typedValueFor(rawId as Comparable<Any>, entityKClass)
    }
  }

  private fun illegalConversion(value: Any, targetType: String) =
    IllegalArgumentException(
      "Cannot convert value of type ${value.javaClass} to $targetType for TypedValue"
    )
}
