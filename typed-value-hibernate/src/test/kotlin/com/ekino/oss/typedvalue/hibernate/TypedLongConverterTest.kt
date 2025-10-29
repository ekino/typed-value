/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ekino.oss.typedvalue.TypedLong
import org.junit.jupiter.api.Test

class TypedLongConverterTest {

  private class TestEntity

  private class TestConverter : TypedLongConverter<TestEntity>(TestEntity::class)

  private val converter = TestConverter()

  @Test
  fun `should convert TypedLong to database column`() {
    val typedLong = TypedLong.of(9876543210L, TestEntity::class)

    val result = converter.convertToDatabaseColumn(typedLong)

    assertThat(result).isEqualTo(9876543210L)
  }

  @Test
  fun `should convert null TypedLong to null database column`() {
    val result = converter.convertToDatabaseColumn(null)

    assertThat(result).isNull()
  }

  @Test
  fun `should convert database column to TypedLong`() {
    val result = converter.convertToEntityAttribute(9876543210L)

    assertThat(result).isEqualTo(TypedLong.of(9876543210L, TestEntity::class))
  }

  @Test
  fun `should convert null database column to null TypedLong`() {
    val result = converter.convertToEntityAttribute(null)

    assertThat(result).isNull()
  }
}
