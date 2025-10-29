/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import com.ekino.oss.typedvalue.TypedValue
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer

/**
 * Jackson serializer for TypedValue.
 *
 * Serializes a TypedValue to its raw ID value. The type information is not serialized.
 *
 * Example:
 * ```
 * TypedValue<String, User>("user-123") -> "user-123"
 * TypedValue<Long, Product>(42) -> 42
 * ```
 */
class TypedValueSerializer : StdSerializer<TypedValue<*, *>>(TypedValue::class.java) {
  override fun serialize(value: TypedValue<*, *>, gen: JsonGenerator, ctxt: SerializationContext) {
    when (val rawId = value.value) {
      is String -> gen.writeString(rawId)
      is Long -> gen.writeNumber(rawId)
      is Int -> gen.writeNumber(rawId)
      is Double -> gen.writeNumber(rawId)
      is Float -> gen.writeNumber(rawId)
      is Number -> gen.writeNumber(rawId.toLong())
      else -> {
        // For other Comparable types (like UUID), write as string
        gen.writeString(rawId.toString())
      }
    }
  }
}
