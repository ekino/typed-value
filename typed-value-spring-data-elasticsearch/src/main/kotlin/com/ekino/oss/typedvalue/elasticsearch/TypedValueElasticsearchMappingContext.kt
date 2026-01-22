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
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import org.springframework.data.core.TypeInformation
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
 * - Automatically detects TypedValue fields through dynamic property inspection
 * - Resolves generic type parameters at runtime
 * - Converts TypedValue to raw value for storage
 * - Reconstructs TypedValue from raw value on retrieval
 * - Supports Collection<TypedValue> (Lists only, not Sets or Arrays)
 * - Supports custom TypedValue subclasses via registerCustomTypedValue()
 *
 * Usage:
 * ```kotlin
 * @Bean
 * fun elasticsearchMappingContext(): TypedValueElasticsearchMappingContext {
 *   val context = TypedValueElasticsearchMappingContext()
 *
 *   // Register custom TypedValue types (optional)
 *   context.registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
 *     TypedId(value, entityKClass)
 *   }
 *
 *   return context
 * }
 * ```
 *
 * Works seamlessly with ElasticsearchCustomConversions - no configuration order required:
 * ```kotlin
 * @Bean
 * fun elasticsearchMappingContext(
 *   elasticsearchCustomConversions: ElasticsearchCustomConversions
 * ): TypedValueElasticsearchMappingContext {
 *   return TypedValueElasticsearchMappingContext().apply {
 *     setInitialEntitySet(initialEntitySet)  // Configuration methods
 *     setSimpleTypeHolder(elasticsearchCustomConversions.simpleTypeHolder)
 *     setFieldNamingStrategy(fieldNamingStrategy())
 *     registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
 *       TypedId(value, entityKClass)
 *     }
 *   }
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

  /**
   * Holds registration info for a custom TypedValue type.
   *
   * @property valueType The KClass of the raw ID type (String, Long, UUID, etc.)
   * @property constructor Function to construct TypedValue from raw ID and entity KClass
   */
  internal data class TypeRegistration<VALUE : Comparable<VALUE>>(
    val valueType: KClass<VALUE>,
    val constructor: (VALUE, KClass<Any>) -> TypedValue<VALUE, *>,
  )

  /** Registry of custom TypedValue constructors. Maps exact type Class to registration info. */
  private val customTypeRegistry =
    ConcurrentHashMap<Class<out TypedValue<*, *>>, TypeRegistration<*>>()

  /** Prevents registration after initialization. */
  @Volatile private var registryLocked = false

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

  /**
   * Registers a custom TypedValue subclass for Elasticsearch serialization with type-safe value
   * type.
   *
   * Must be called BEFORE Spring initializes the mapping context (before initialize() or
   * afterPropertiesSet()). You can safely call setInitialEntitySet(), setSimpleTypeHolder(), and
   * other configuration methods before this.
   *
   * This method registers a constructor function that will be used to reconstruct custom TypedValue
   * instances from Elasticsearch. Detection happens dynamically through property inspection.
   *
   * Example:
   * ```kotlin
   * class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
   *
   * // This works - registration before Spring initialization
   * val context = TypedValueElasticsearchMappingContext().apply {
   *   setInitialEntitySet(initialEntitySet)  // OK - doesn't lock registry
   *   setSimpleTypeHolder(customConversions.simpleTypeHolder)  // OK
   *   registerCustomTypedValue(TypedId::class.java, String::class) { value, entityKClass ->
   *     TypedId(value, entityKClass)
   *   }
   * }
   * ```
   *
   * @param typedValueClass The Class of the custom TypedValue subclass
   * @param valueType The KClass of the raw value type (String, Long, UUID, etc.)
   * @param constructor Function that constructs an instance from typed raw value and entity KClass
   * @throws IllegalStateException if called after Spring initialization
   * @throws IllegalArgumentException if the type is already registered
   */
  fun <T : TypedValue<VALUE, *>, VALUE : Comparable<VALUE>> registerCustomTypedValue(
    typedValueClass: Class<T>,
    valueType: KClass<VALUE>,
    constructor: (value: VALUE, entityKClass: KClass<Any>) -> T,
  ) {
    check(!registryLocked) {
      "Cannot register custom TypedValue types after mapping context initialization. " +
        "Ensure registerCustomTypedValue() is called during bean construction, before Spring " +
        "calls initialize() or afterPropertiesSet()."
    }

    require(!customTypeRegistry.containsKey(typedValueClass)) {
      "TypedValue class ${typedValueClass.name} is already registered."
    }

    customTypeRegistry[typedValueClass] = TypeRegistration(valueType, constructor)
  }

  /**
   * Override to safely merge user-provided simple types with our required TypedValue types.
   *
   * This override ensures that TypedValue types remain registered even when users configure custom
   * conversions via ElasticsearchCustomConversions.
   *
   * Example usage (works seamlessly):
   * ```kotlin
   * @Bean
   * fun elasticsearchMappingContext(
   *   elasticsearchCustomConversions: ElasticsearchCustomConversions
   * ): TypedValueElasticsearchMappingContext {
   *   val context = TypedValueElasticsearchMappingContext()
   *   // This is safe - TypedValue types are automatically preserved
   *   context.setSimpleTypeHolder(elasticsearchCustomConversions.simpleTypeHolder)
   *   return context
   * }
   * ```
   */
  override fun setSimpleTypeHolder(simpleTypeHolder: SimpleTypeHolder) {
    // Merge user's types with our required TypedValue types
    val ourTypes =
      setOf(
        TypedValue::class.java,
        TypedString::class.java,
        TypedInt::class.java,
        TypedLong::class.java,
        TypedUuid::class.java,
      )

    // SimpleTypeHolder(Set, SimpleTypeHolder) merges both sources
    super.setSimpleTypeHolder(SimpleTypeHolder(ourTypes, simpleTypeHolder))
  }

  /**
   * Kotlin DSL version with reified generics and type-safe ID type.
   *
   * Example:
   * ```kotlin
   * context.registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
   *   TypedId(value, entityKClass)
   * }
   * ```
   *
   * @param constructor Function that constructs an instance from typed raw ID and entity KClass
   */
  inline fun <
    reified T : TypedValue<VALUE, *>,
    reified VALUE : Comparable<VALUE>,
  > registerCustomTypedValue(noinline constructor: (value: VALUE, entityKClass: KClass<Any>) -> T) {
    registerCustomTypedValue(T::class.java, VALUE::class, constructor)
  }

  /**
   * Functional interface for Java compatibility. Allows Java code to implement constructors using
   * lambdas or method references.
   */
  fun interface TypedValueConstructor<T : TypedValue<VALUE, *>, VALUE : Comparable<VALUE>> {
    /**
     * Constructs a TypedValue instance.
     *
     * @param value The typed raw value from Elasticsearch
     * @param entityClass The entity class (Java Class, not KClass)
     * @return A TypedValue instance
     */
    fun construct(value: VALUE, entityClass: Class<*>): T
  }

  /**
   * Java-friendly registration method using Class instead of KClass.
   *
   * Example (Java):
   * ```java
   * TypedValueElasticsearchMappingContext context = new TypedValueElasticsearchMappingContext();
   * context.registerCustomTypedValue(
   *   TypedId.class,
   *   String.class,
   *   (value, entityClass) -> new TypedId(
   *     value,
   *     JvmClassMappingKt.getKotlinClass(entityClass)
   *   )
   * );
   * ```
   *
   * @param typedValueClass The Class of the custom TypedValue subclass
   * @param valueType The Class of the raw ID type (String, Long, UUID, etc.)
   * @param constructor Function that constructs an instance from typed raw ID and entity Class
   */
  fun <T : TypedValue<VALUE, *>, VALUE : Comparable<VALUE>> registerCustomTypedValue(
    typedValueClass: Class<T>,
    valueType: Class<VALUE>,
    constructor: TypedValueConstructor<T, VALUE>,
  ) {
    // Convert Java-style constructor to Kotlin-style
    registerCustomTypedValue(typedValueClass, valueType.kotlin) { value, entityKClass ->
      constructor.construct(value, entityKClass.java)
    }
  }

  override fun initialize() {
    registryLocked = true
    super.initialize()
  }

  override fun afterPropertiesSet() {
    registryLocked = true
    super.afterPropertiesSet()
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
      val propertyTypeInformation = owner.typeInformation.getRequiredProperty(property.name)

      // Determine the actual TypedValue type (handling collections)
      val typedValueType =
        if (persistentProperty.isCollectionLike) {
          propertyTypeInformation.actualType
        } else {
          propertyTypeInformation
        }
      requireNotNull(typedValueType) {
        "Could not determine TypedValue type for property: ${owner.type.name}#${property.name}"
      }

      rejectArraysOrSets(persistentProperty, owner, property, typedValueType)

      val typeArgs = typedValueType.typeArguments ?: emptyList()
      val rawClass = typedValueType.type

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
            "Actual type of ${typedValueType.type.simpleName} could not be resolved for property: " +
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
        customTypeRegistry,
      )
    }

    return persistentProperty
  }

  private fun rejectArraysOrSets(
    persistentProperty: ElasticsearchPersistentProperty,
    owner: SimpleElasticsearchPersistentEntity<*>,
    property: Property,
    typedValueType: TypeInformation<out Any>,
  ) {
    // Validate: reject Arrays
    if (persistentProperty.isArray) {
      throw UnsupportedOperationException(
        "Arrays of ${typedValueType.type.simpleName} are not supported. " +
          "Caused by property: ${owner.type.name}#${property.name}"
      )
    }
    // Validate: reject Sets
    if (
      persistentProperty.isCollectionLike &&
        Set::class.java.isAssignableFrom(persistentProperty.rawType)
    ) {
      throw UnsupportedOperationException(
        "Sets of ${typedValueType.type.simpleName} are not supported. " +
          "Caused by property: ${owner.type.name}#${property.name}"
      )
    }
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
internal class TypedValueElasticPersistentPropertyWithConverter(
  private val property: ElasticsearchPersistentProperty,
  private val typedIdEntityType: Class<*>,
  private val typedIdType: Class<*>,
  private val typedValueClass: Class<*>?,
  private val customTypeRegistry:
    Map<Class<out TypedValue<*, *>>, TypedValueElasticsearchMappingContext.TypeRegistration<*>>,
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
    // Check if custom type is registered and validate raw value type BEFORE conversion
    val registration = typedValueClass?.let { customTypeRegistry[it] }
    if (registration != null) {
      // Validate incoming value type matches registered VALUE type
      if (value::class != registration.valueType) {
        throw IllegalStateException(
          "Type mismatch for ${typedValueClass.name}: expected raw value type " +
            "${registration.valueType.simpleName} from Elasticsearch but got ${value::class.simpleName} " +
            "with value: $value"
        )
      }
    }

    // Convert value to appropriate ID type using KClass for cleaner comparison
    val rawValue: Comparable<*> =
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

    // Step 1: Check registry for exact type match FIRST
    if (registration != null) {
      // Call constructor with type-safe cast
      @Suppress("UNCHECKED_CAST")
      val constructor = registration.constructor as (Comparable<*>, KClass<Any>) -> TypedValue<*, *>

      return runCatching { constructor(rawValue, entityKClass) }
        .getOrElse { e ->
          // Catch any exception from user constructor and wrap with context
          throw IllegalStateException(
            "Failed to construct TypedValue of type ${typedValueClass.name} " +
              "for entity ${entityKClass.simpleName} with value: $rawValue",
            e,
          )
        }
    }

    // Step 2: Fall back to existing isAssignableFrom logic for built-in types
    return when {
      typedValueClass != null && TypedUuid::class.java.isAssignableFrom(typedValueClass) ->
        TypedUuid(rawValue as UUID, entityKClass)
      typedValueClass != null && TypedString::class.java.isAssignableFrom(typedValueClass) ->
        TypedString(rawValue as String, entityKClass)
      typedValueClass != null && TypedLong::class.java.isAssignableFrom(typedValueClass) ->
        TypedLong(rawValue as Long, entityKClass)
      typedValueClass != null && TypedInt::class.java.isAssignableFrom(typedValueClass) ->
        TypedInt(rawValue as Int, entityKClass)
      else -> TypedValue.typedValueFor(rawValue as Comparable<Any>, entityKClass)
    }
  }

  private fun illegalConversion(value: Any, targetType: String) =
    IllegalArgumentException(
      "Cannot convert value of type ${value.javaClass} to $targetType for TypedValue"
    )
}
