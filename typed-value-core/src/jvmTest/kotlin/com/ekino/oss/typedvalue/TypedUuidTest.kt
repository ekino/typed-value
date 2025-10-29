/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import java.util.UUID
import org.junit.jupiter.api.Test

class TypedUuidTest {

  class Order

  @Test
  fun `should create TypedUuid using factory method`() {
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val orderId = TypedUuid.of(uuid, Order::class)

    assertThat(orderId).isInstanceOf<TypedUuid<Order>>()
    assertThat(orderId.value).isEqualTo(uuid)
    assertThat(orderId.type).isEqualTo(Order::class)
  }

  @Test
  fun `should create TypedUuid using extension function`() {
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val orderId = uuid.toTypedUuid<Order>()

    assertThat(orderId).isInstanceOf<TypedUuid<Order>>()
    assertThat(orderId.value).isEqualTo(uuid)
    assertThat(orderId.type).isEqualTo(Order::class)
  }

  @Test
  fun `TypedUuid should be compatible with TypedValue interface`() {
    val uuid = UUID.randomUUID()
    val orderId: TypedValue<UUID, Order> = uuid.toTypedUuid()

    assertThat(orderId.value).isEqualTo(uuid)
  }
}
