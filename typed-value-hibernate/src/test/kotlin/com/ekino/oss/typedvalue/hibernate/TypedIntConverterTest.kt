/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ekino.oss.typedvalue.TypedInt
import org.junit.jupiter.api.Test

class TypedIntConverterTest {

  private class TestEntity

  private class TestConverter : TypedIntConverter<TestEntity>(TestEntity::class)

  private val converter = TestConverter()

  @Test
  fun `should convert TypedInt to database column`() {
    val typedInt = TypedInt.of(42, TestEntity::class)

    val result = converter.convertToDatabaseColumn(typedInt)

    assertThat(result).isEqualTo(42)
  }

  @Test
  fun `should convert null TypedInt to null database column`() {
    val result = converter.convertToDatabaseColumn(null)

    assertThat(result).isNull()
  }

  @Test
  fun `should convert database column to TypedInt`() {
    val result = converter.convertToEntityAttribute(42)

    assertThat(result).isEqualTo(TypedInt.of(42, TestEntity::class))
  }

  @Test
  fun `should convert null database column to null TypedInt`() {
    val result = converter.convertToEntityAttribute(null)

    assertThat(result).isNull()
  }
}
