/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ekino.oss.typedvalue.TypedString
import org.junit.jupiter.api.Test

class TypedStringConverterTest {

  private class TestEntity

  private class TestConverter : TypedStringConverter<TestEntity>(TestEntity::class)

  private val converter = TestConverter()

  @Test
  fun `should convert TypedString to database column`() {
    val typedString = TypedString.of("test-value", TestEntity::class)

    val result = converter.convertToDatabaseColumn(typedString)

    assertThat(result).isEqualTo("test-value")
  }

  @Test
  fun `should convert null TypedString to null database column`() {
    val result = converter.convertToDatabaseColumn(null)

    assertThat(result).isNull()
  }

  @Test
  fun `should convert database column to TypedString`() {
    val result = converter.convertToEntityAttribute("test-value")

    assertThat(result).isEqualTo(TypedString.of("test-value", TestEntity::class))
  }

  @Test
  fun `should convert null database column to null TypedString`() {
    val result = converter.convertToEntityAttribute(null)

    assertThat(result).isNull()
  }
}
