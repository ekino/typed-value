/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.spring

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import org.junit.jupiter.api.Test

class TypedValueToStringConverterTest {

  class User

  class Product

  private val converter = TypedValueToStringConverter()

  @Test
  fun `should convert TypedString to its raw String value`() {
    val result = converter.convert(TypedString("user-123", User::class))

    assertThat(result).isEqualTo("user-123")
  }

  @Test
  fun `should convert TypedLong to its raw String value`() {
    val result = converter.convert(TypedLong(42L, Product::class))

    assertThat(result).isEqualTo("42")
  }

  @Test
  fun `should convert TypedInt to its raw String value`() {
    val result = converter.convert(TypedInt(7, Product::class))

    assertThat(result).isEqualTo("7")
  }

  @Test
  fun `should convert TypedUuid to its raw String value`() {
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

    val result = converter.convert(TypedUuid(uuid, User::class))

    assertThat(result).isEqualTo(uuid.toString())
  }

  @Test
  fun `should convert generic TypedValue to its raw String value`() {
    val result = converter.convert(TypedValue.typedValueFor(99L, Product::class))

    assertThat(result).isEqualTo("99")
  }
}
