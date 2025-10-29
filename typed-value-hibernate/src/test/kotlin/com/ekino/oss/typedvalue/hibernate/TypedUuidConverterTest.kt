/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ekino.oss.typedvalue.TypedUuid
import java.util.UUID
import org.junit.jupiter.api.Test

class TypedUuidConverterTest {

  private class TestEntity

  private class TestConverter : TypedUuidConverter<TestEntity>(TestEntity::class)

  private val converter = TestConverter()

  @Test
  fun `should convert TypedUuid to database column`() {
    val uuid = UUID.randomUUID()
    val typedUuid = TypedUuid.of(uuid, TestEntity::class)

    val result = converter.convertToDatabaseColumn(typedUuid)

    assertThat(result).isEqualTo(uuid)
  }

  @Test
  fun `should convert null TypedUuid to null database column`() {
    val result = converter.convertToDatabaseColumn(null)

    assertThat(result).isNull()
  }

  @Test
  fun `should convert database column to TypedUuid`() {
    val uuid = UUID.randomUUID()

    val result = converter.convertToEntityAttribute(uuid)

    assertThat(result).isEqualTo(TypedUuid.of(uuid, TestEntity::class))
  }

  @Test
  fun `should convert null database column to null TypedUuid`() {
    val result = converter.convertToEntityAttribute(null)

    assertThat(result).isNull()
  }
}
