/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.security.InvalidParameterException
import java.util.UUID
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
 * - String IDs
 * - Long IDs
 * - UUID IDs
 * - Any other Comparable type that can be constructed from a String
 *
 * Example property declarations:
 * ```
 * data class UserDto(
 *   val id: TypedValue<String, User>,           // Resolved to String ID, User type
 *   val productId: TypedValue<Long, Product>,   // Resolved to Long ID, Product type
 *   val orderId: TypedValue<UUID, Order>        // Resolved to UUID ID, Order type
 * )
 * ```
 */
class TypedValueDeserializer : StdDeserializer<TypedValue<*, *>>(TypedValue::class.java) {
  private val deserializers: MutableMap<DeserializerKey, ValueDeserializer<*>> = mutableMapOf()

  override fun createContextual(
    ctxt: DeserializationContext,
    property: BeanProperty?,
  ): ValueDeserializer<*> {
    val contextualType: JavaType = ctxt.contextualType ?: return this

    // Get the target type class (TypedValue, TypedUuid, TypedString, etc.)
    val targetType = contextualType.rawClass

    // Convenience types (TypedUuid, TypedString, TypedLong, TypedInt) have only one type parameter
    // (the entity type), and their ID type is known from the class itself.
    // Generic TypedValue has two type parameters: ID and entity type.
    val (idType, entityType) =
      when {
        TypedUuid::class.java.isAssignableFrom(targetType) -> {
          UUID::class.java to extractEntityType(contextualType.containedType(0)?.rawClass, property)
        }
        TypedString::class.java.isAssignableFrom(targetType) -> {
          String::class.java to
            extractEntityType(contextualType.containedType(0)?.rawClass, property)
        }
        TypedLong::class.java.isAssignableFrom(targetType) -> {
          Long::class.java to extractEntityType(contextualType.containedType(0)?.rawClass, property)
        }
        TypedInt::class.java.isAssignableFrom(targetType) -> {
          Int::class.java to extractEntityType(contextualType.containedType(0)?.rawClass, property)
        }
        else -> {
          // Generic TypedValue<ID, T>
          val idClass =
            contextualType.containedType(0)?.rawClass
              ?: throw InvalidParameterException(
                "Cannot resolve ID type parameter for property: '${property?.fullName}'"
              )
          val entityClass = extractEntityType(contextualType.containedType(1)?.rawClass, property)
          idClass to entityClass
        }
      }

    val key = DeserializerKey(targetType, idType, entityType)
    return deserializers.getOrPut(key) { IdDeserializerOf(targetType, idType, entityType) }
  }

  private fun extractEntityType(rawClass: Class<*>?, property: BeanProperty?): Class<*> {
    return rawClass?.takeIf { it != Any::class.java }
      ?: throw InvalidParameterException(
        "Cannot resolve entity type parameter for property: '${property?.fullName}'. Please provide a valid type parameter (not Any or *)."
      )
  }

  override fun deserialize(
    jsonParser: JsonParser,
    deserializationContext: DeserializationContext,
  ): TypedValue<*, *> {
    throw UnsupportedOperationException(
      """TypedValueDeserializer requires contextual type information. Ensure TypedValue properties have explicit generic type parameters."""
    )
  }

  private data class DeserializerKey(
    val targetType: Class<*>,
    val idType: Class<*>,
    val entityType: Class<*>,
  )

  private class IdDeserializerOf(
    val targetType: Class<*>,
    val idType: Class<*>,
    val entityType: Class<*>,
  ) : StdDeserializer<TypedValue<*, *>>(TypedValue::class.java) {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
      jsonParser: JsonParser,
      deserializationContext: DeserializationContext,
    ): TypedValue<*, *> {
      val entityKClass = (entityType as Class<Any>).kotlin

      val rawId: Comparable<*> =
        when (idType) {
          String::class.java -> jsonParser.string
          Long::class.java,
          Long::class.javaObjectType -> jsonParser.longValue
          Int::class.java,
          Int::class.javaObjectType -> jsonParser.intValue
          UUID::class.java -> UUID.fromString(jsonParser.string)
          else -> jsonParser.string // Default to string for other Comparable types
        }

      // Return the appropriate TypedValue subtype based on targetType
      return when {
        TypedUuid::class.java.isAssignableFrom(targetType) ->
          TypedUuid.of(rawId as UUID, entityKClass)
        TypedString::class.java.isAssignableFrom(targetType) ->
          TypedString.of(rawId as String, entityKClass)
        TypedLong::class.java.isAssignableFrom(targetType) ->
          TypedLong.of(rawId as Long, entityKClass)
        TypedInt::class.java.isAssignableFrom(targetType) -> TypedInt.of(rawId as Int, entityKClass)
        else -> TypedValue(rawId as Comparable<Any>, entityKClass)
      }
    }
  }
}
