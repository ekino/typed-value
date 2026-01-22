/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.ValueDeserializerModifier
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
 *
 * ## Custom TypedValue Types
 *
 * You can register custom TypedValue subtypes for deserialization:
 *
 * Kotlin example with reified generics:
 * ```kotlin
 * class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
 *
 * @Bean
 * fun typedValueModule(): TypedValueModule {
 *   return TypedValueModule().apply {
 *     registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
 *       TypedId(value, entityKClass)
 *     }
 *   }
 * }
 * ```
 *
 * Java example with functional interface:
 * ```java
 * @Bean
 * public TypedValueModule typedValueModule() {
 *   TypedValueModule module = new TypedValueModule();
 *   module.registerCustomTypedValue(
 *     TypedId.class,
 *     String.class,
 *     (value, entityClass) -> new TypedId<>(
 *       value,
 *       JvmClassMappingKt.getKotlinClass(entityClass)
 *     )
 *   );
 *   return module;
 * }
 * ```
 *
 * **Important:** Registration must happen before the module is registered with ObjectMapper. Once
 * the ObjectMapper is constructed, it becomes immutable.
 */
@Suppress("UNCHECKED_CAST")
class TypedValueModule : SimpleModule("TypedValueModule") {

  /**
   * Holds registration info for a custom TypedValue type.
   *
   * @property valueType The KClass of the raw ID type (String, Long, UUID, etc.)
   * @property constructor Function to construct TypedValue from raw ID and entity KClass
   */
  data class TypeRegistration<VALUE : Comparable<VALUE>>(
    val valueType: KClass<VALUE>,
    val constructor: (VALUE, KClass<Any>) -> TypedValue<VALUE, *>,
  )

  /** Registry of custom TypedValue constructors. Maps exact type Class to registration info. */
  private val customTypeRegistry =
    ConcurrentHashMap<Class<out TypedValue<*, *>>, TypeRegistration<*>>()

  /** Built-in types that cannot be overridden. */
  private val builtInTypes =
    setOf(
      TypedValue::class.java,
      TypedString::class.java,
      TypedInt::class.java,
      TypedLong::class.java,
      TypedUuid::class.java,
    )

  init {
    // Serializer handles all TypedValue subtypes via polymorphism
    addSerializer(TypedValue::class.java, TypedValueSerializer())

    // Register deserializer for base type and all convenience types
    // The deserializer uses contextual type info to return the correct subtype
    val deserializer = TypedValueDeserializer(customTypeRegistry)
    addDeserializer(TypedValue::class.java, deserializer)
    addDeserializer(TypedUuid::class.java, deserializer as ValueDeserializer<TypedUuid<*>>)
    addDeserializer(TypedString::class.java, deserializer as ValueDeserializer<TypedString<*>>)
    addDeserializer(TypedLong::class.java, deserializer as ValueDeserializer<TypedLong<*>>)
    addDeserializer(TypedInt::class.java, deserializer as ValueDeserializer<TypedInt<*>>)

    // Add deserializer modifier to handle all TypedValue subtypes (including custom ones)
    setDeserializerModifier(
      object : ValueDeserializerModifier() {
        override fun modifyDeserializer(
          config: DeserializationConfig,
          beanDescSupplier: BeanDescription.Supplier,
          deserializer: ValueDeserializer<*>,
        ): ValueDeserializer<*> {
          val beanDesc = beanDescSupplier.get()
          val beanClass = beanDesc.beanClass
          // If this is a TypedValue subtype (but not one we've already registered),
          // use our custom deserializer
          if (
            TypedValue::class.java.isAssignableFrom(beanClass) && !builtInTypes.contains(beanClass)
          ) {
            @Suppress("UNCHECKED_CAST")
            return TypedValueDeserializer(customTypeRegistry) as ValueDeserializer<*>
          }
          return deserializer
        }
      }
    )
  }

  /**
   * Registers a custom TypedValue subclass for Jackson deserialization with type-safe value type.
   *
   * Must be called BEFORE registering the module with ObjectMapper. Jackson's ObjectMapper is
   * immutable after construction, so all registration must happen during module configuration.
   *
   * This method registers a constructor function that will be used to reconstruct custom TypedValue
   * instances from JSON.
   *
   * Example:
   * ```kotlin
   * class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
   *
   * val objectMapper = jsonMapper {
   *   addModule(kotlinModule())
   *   addModule(TypedValueModule().apply {
   *     registerCustomTypedValue(TypedId::class.java, String::class) { value, entityKClass ->
   *       TypedId(value, entityKClass)
   *     }
   *   })
   * }
   * ```
   *
   * @param typedValueClass The Class of the custom TypedValue subclass
   * @param valueType The KClass of the raw value type (String, Long, UUID, etc.)
   * @param constructor Function that constructs an instance from typed raw value and entity KClass
   * @throws IllegalArgumentException if attempting to register a built-in type or duplicate
   *   registration
   */
  fun <T : TypedValue<VALUE, *>, VALUE : Comparable<VALUE>> registerCustomTypedValue(
    typedValueClass: Class<T>,
    valueType: KClass<VALUE>,
    constructor: (value: VALUE, entityKClass: KClass<Any>) -> T,
  ) {
    require(!builtInTypes.contains(typedValueClass)) {
      """
        Cannot register built-in TypedValue type ${typedValueClass.name}. 
        Built-in types (TypedValue, TypedString, TypedInt, TypedLong, TypedUuid) cannot be overridden.
        """
        .trimIndent()
    }

    require(!customTypeRegistry.containsKey(typedValueClass)) {
      "TypedValue class ${typedValueClass.simpleName} is already registered."
    }

    customTypeRegistry[typedValueClass] = TypeRegistration(valueType, constructor)
  }

  /**
   * Kotlin DSL version with reified generics and type-safe ID type.
   *
   * Example:
   * ```kotlin
   * val objectMapper = jsonMapper {
   *   addModule(kotlinModule())
   *   addModule(TypedValueModule().apply {
   *     registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
   *       TypedId(value, entityKClass)
   *     }
   *   })
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
     * @param value The typed raw value from JSON
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
   * TypedValueModule module = new TypedValueModule();
   * module.registerCustomTypedValue(
   *   TypedId.class,
   *   String.class,
   *   (value, entityClass) -> new TypedId<>(
   *     value,
   *     JvmClassMappingKt.getKotlinClass(entityClass)
   *   )
   * );
   *
   * ObjectMapper mapper = JsonMapper.builder()
   *   .addModule(new KotlinModule.Builder().build())
   *   .addModule(module)
   *   .build();
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
}
