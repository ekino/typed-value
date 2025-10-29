/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.spring

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.core.convert.TypeDescriptor

class StringToTypedValueConverterTest {

  class User

  class Product

  private val converter = StringToTypedValueConverter()

  @Test
  fun `should convert String to TypedValue with String ID`() {
    val sourceType = TypeDescriptor.valueOf(String::class.java)
    val targetType = createTypedValueTypeDescriptor<String, User>()

    val result = converter.convert("user-123", sourceType, targetType) as TypedValue<*, *>

    assertThat(result.value).isEqualTo("user-123")
    assertThat(result.type).isEqualTo(User::class)
  }

  @Test
  fun `should convert String to TypedValue with Long ID`() {
    val sourceType = TypeDescriptor.valueOf(String::class.java)
    val targetType = createTypedValueTypeDescriptor<Long, Product>()

    val result = converter.convert("42", sourceType, targetType) as TypedValue<*, *>

    assertThat(result.value).isEqualTo(42L)
    assertThat(result.type).isEqualTo(Product::class)
  }

  @Test
  fun `should convert String to TypedValue with UUID`() {
    val sourceType = TypeDescriptor.valueOf(String::class.java)
    val targetType = createTypedValueTypeDescriptor<UUID, User>()
    val uuidString = "123e4567-e89b-12d3-a456-426614174000"

    val result = converter.convert(uuidString, sourceType, targetType) as TypedValue<*, *>

    assertThat(result.value).isEqualTo(UUID.fromString(uuidString))
    assertThat(result.type).isEqualTo(User::class)
  }

  // Helper classes to create TypeDescriptor for TypedValue<ID, T>
  private class StringUserIdHolder {
    @Suppress("unused") val field: TypedValue<String, User>? = null
  }

  private class LongProductIdHolder {
    @Suppress("unused") val field: TypedValue<Long, Product>? = null
  }

  private class UuidUserIdHolder {
    @Suppress("unused") val field: TypedValue<UUID, User>? = null
  }

  private inline fun <
    reified ID : Comparable<ID>,
    reified T : Any,
  > createTypedValueTypeDescriptor(): TypeDescriptor {
    val holderClass =
      when {
        ID::class == String::class && T::class == User::class -> StringUserIdHolder::class
        ID::class == Long::class && T::class == Product::class -> LongProductIdHolder::class
        ID::class == UUID::class && T::class == User::class -> UuidUserIdHolder::class
        else -> error("No holder class for ${ID::class}/${T::class}")
      }

    val field = holderClass.java.getDeclaredField("field")
    return TypeDescriptor(field)
  }
}
