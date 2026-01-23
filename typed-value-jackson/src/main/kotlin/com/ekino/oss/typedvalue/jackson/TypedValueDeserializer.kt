/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import kotlin.reflect.KClass
import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.std.StdDeserializer

/**
 * Jackson deserializer for TypedValue with contextual type resolution.
 *
 * This deserializer uses Jackson's contextual deserialization to resolve the generic type
 * parameters of TypedValue from the property declaration.
 *
 * Supports:
 * - String values
 * - Long values
 * - UUID values
 * - Any other Comparable type that can be constructed from a String
 * - Custom TypedValue subtypes registered via TypedValueModule.registerCustomTypedValue()
 *
 * Example property declarations:
 * ```
 * data class UserDto(
 *   val id: TypedValue<String, User>,           // Resolved to String value, User type
 *   val productId: TypedValue<Long, Product>,   // Resolved to Long value, Product type
 *   val orderId: TypedValue<UUID, Order>        // Resolved to UUID value, Order type
 * )
 * ```
 */
class TypedValueDeserializer(
  private val customTypeRegistry:
    Map<Class<out TypedValue<*, *>>, TypedValueModule.TypeRegistration<*>> =
    emptyMap()
) : StdDeserializer<TypedValue<*, *>>(TypedValue::class.java) {
  private val deserializers: MutableMap<DeserializerKey, ValueDeserializer<*>> = mutableMapOf()

  override fun createContextual(
    context: DeserializationContext,
    property: BeanProperty?,
  ): ValueDeserializer<*> {
    val contextualType: JavaType = context.contextualType ?: return this

    // Get the target type class (TypedValue, TypedUuid, TypedString, etc.)
    val targetType = contextualType.rawClass
    val propertyName = property?.fullName?.toString() ?: "unknown"

    check(targetType.isKnownTypedValueType()) {
      """Unsupported TypedValue subtype: ${targetType.simpleName} at property '$propertyName'.
        |Ensure the type is one of the built-in types (TypedValue, TypedString, TypedLong, TypedUuid, TypedInt) 
        |or is registered via TypedValueModule.registerCustomTypedValue()."""
        .trimMargin()
    }
    val (valueType, entityType) = extractTypes(contextualType, propertyName)

    val key = DeserializerKey(targetType, valueType, entityType)
    return deserializers.getOrPut(key) {
      ValueDeserializerOf(targetType, valueType, entityType, customTypeRegistry, propertyName)
    }
  }

  private fun Class<*>.isKnownTypedValueType(): Boolean {
    return TypedValue::class.java == this ||
      TypedUuid::class.java == this ||
      TypedString::class.java == this ||
      TypedLong::class.java == this ||
      TypedInt::class.java == this ||
      customTypeRegistry.containsKey(this)
  }

  override fun deserialize(
    jsonParser: JsonParser,
    deserializationContext: DeserializationContext,
  ): TypedValue<*, *> {
    throw UnsupportedOperationException(
      "TypedValueDeserializer requires contextual type information. Ensure TypedValue properties have explicit generic type parameters."
    )
  }

  private fun extractTypes(javaType: JavaType, propertyName: String): Pair<Class<*>, Class<*>> {
    check(TypedValue::class.java.isAssignableFrom(javaType.rawClass)) {
      "Expected a TypedValue subtype for property '$propertyName', but got: ${javaType.rawClass.name}"
    }
    return getTypedValueClass(javaType).let {
      val valueType = it.containedType(0)
      require(valueType != null && valueType.rawClass != Any::class.java) {
        """Cannot resolve value type parameter for property '$propertyName' with TypedValue subtype: ${javaType.rawClass.simpleName}. 
          |Please provide a valid type parameter (not Any or *)."""
          .trimMargin()
      }
      val entityType = it.containedType(1)
      require(entityType != null && entityType.rawClass != Any::class.java) {
        """Cannot resolve entity type parameter for property '$propertyName' with TypedValue subtype: ${javaType.rawClass.simpleName}.
            |Please provide a valid type parameter (not Any or *)."""
          .trimMargin()
      }
      valueType.rawClass to entityType.rawClass
    }
  }

  private fun getTypedValueClass(javaType: JavaType): JavaType =
    when {
      javaType.rawClass == TypedValue::class.java -> javaType
      else ->
        javaType.superClass?.let { getTypedValueClass(it) }
          ?: error("Failed to resolve TypedValue in hierarchy for ${javaType.rawClass.name}")
    }

  private data class DeserializerKey(
    val targetType: Class<*>,
    val valueType: Class<*>,
    val entityType: Class<*>,
  )

  private class ValueDeserializerOf(
    val targetType: Class<*>,
    val valueType: Class<*>,
    val entityType: Class<*>,
    val customTypeRegistry: Map<Class<out TypedValue<*, *>>, TypedValueModule.TypeRegistration<*>>,
    val propertyName: String,
  ) : StdDeserializer<TypedValue<*, *>>(TypedValue::class.java) {

    @Suppress("UNCHECKED_CAST", "LongMethod")
    override fun deserialize(
      jsonParser: JsonParser,
      deserializationContext: DeserializationContext,
    ): TypedValue<*, *> {
      val entityKClass = (entityType as Class<Any>).kotlin

      // Check registry for exact type match FIRST
      val registration = customTypeRegistry[targetType]
      if (registration != null) {
        // Convert JSON value to the registered value type
        val rawValue = convertJsonToValueType(jsonParser, registration.valueType.java)

        // Validate type match
        check(rawValue::class == registration.valueType) {
          "Type mismatch for ${targetType.name} at property '$propertyName': " +
            "expected raw value type ${registration.valueType.simpleName} " +
            "but got ${rawValue::class.simpleName} with value: $rawValue"
        }

        // Call custom constructor
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            val constructor =
              registration.constructor as (Comparable<*>, KClass<Any>) -> TypedValue<*, *>
            constructor(rawValue, entityKClass)
          }
          .getOrElse { e ->
            throw IllegalStateException(
              "Failed to construct TypedValue of type ${targetType.simpleName} " +
                "for entity ${entityKClass.simpleName} at property '$propertyName' " +
                "with value: $rawValue",
              e,
            )
          }
      }

      // Fall back to existing built-in type handling
      val rawValue: Comparable<*> = convertJsonToValueType(jsonParser, valueType)

      // Return the appropriate TypedValue subtype based on targetType
      return when {
        TypedUuid::class.java.isAssignableFrom(targetType) ->
          TypedUuid.of(rawValue as UUID, entityKClass)
        TypedString::class.java.isAssignableFrom(targetType) ->
          TypedString.of(rawValue as String, entityKClass)
        TypedLong::class.java.isAssignableFrom(targetType) ->
          TypedLong.of(rawValue as Long, entityKClass)
        TypedInt::class.java.isAssignableFrom(targetType) ->
          TypedInt.of(rawValue as Int, entityKClass)
        else -> TypedValue(rawValue as Comparable<Any>, entityKClass)
      }
    }

    /**
     * Convert JSON value to the appropriate value type.
     *
     * @param jsonParser The JSON parser
     * @param targetValueType The target value type class
     * @return The converted value
     */
    private fun convertJsonToValueType(
      jsonParser: JsonParser,
      targetValueType: Class<*>,
    ): Comparable<*> {
      return when (targetValueType) {
        String::class.java -> jsonParser.string
        Long::class.java,
        Long::class.javaObjectType -> jsonParser.longValue
        Int::class.java,
        Int::class.javaObjectType -> jsonParser.intValue
        UUID::class.java -> UUID.fromString(jsonParser.string)
        else -> jsonParser.string // Default to string for other Comparable types
      }
    }
  }
}
