/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import java.io.Serializable
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SerializableTest {

  class User

  class Order

  @Test
  fun `TypedValue should implement Serializable`() {
    val typedValue = TypedValue.typedValueFor("id-123", User::class)
    assertThat(typedValue).isInstanceOf(Serializable::class.java)
  }

  @Test
  fun `TypedString should implement Serializable`() {
    val typedString = "id-123".toTypedString<User>()
    assertThat(typedString).isInstanceOf(Serializable::class.java)
  }

  @Test
  fun `TypedInt should implement Serializable`() {
    val typedInt = 42.toTypedInt<User>()
    assertThat(typedInt).isInstanceOf(Serializable::class.java)
  }

  @Test
  fun `TypedLong should implement Serializable`() {
    val typedLong = 42L.toTypedLong<User>()
    assertThat(typedLong).isInstanceOf(Serializable::class.java)
  }

  @Test
  fun `TypedUuid should implement Serializable`() {
    val typedUuid = UUID.randomUUID().toTypedUuid<Order>()
    assertThat(typedUuid).isInstanceOf(Serializable::class.java)
  }
}
