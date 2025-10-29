/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.querydsl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.ekino.oss.typedvalue.TypedValue
import com.ekino.oss.typedvalue.querydsl.TypedValueExpression.Companion.typedValueExpressionOf
import com.querydsl.core.types.dsl.ComparablePath
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.core.types.dsl.StringPath
import java.util.UUID
import org.junit.jupiter.api.Test

class TypedValueExpressionTest {

  // Test entity classes
  class User

  class Product

  class Order

  // Mock Q-classes for testing
  class QUser(variable: String) : EntityPathBase<User>(User::class.java, variable) {
    val id: StringPath = createString("id")

    companion object {
      val user = QUser("user")
    }
  }

  class QProduct(variable: String) : EntityPathBase<Product>(Product::class.java, variable) {
    val id: NumberPath<Long> = createNumber("id", Long::class.java)

    companion object {
      val product = QProduct("product")
    }
  }

  class QOrder(variable: String) : EntityPathBase<Order>(Order::class.java, variable) {
    val id: ComparablePath<UUID> = createComparable("id", UUID::class.java)

    companion object {
      val order = QOrder("order")
    }
  }

  @Test
  fun `should create TypedValueExpression for String ID entity`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    assertThat(expression).isNotNull()
    assertThat(expression.type).isEqualTo(TypedValue::class.java)
  }

  @Test
  fun `should create TypedValueExpression for Long ID entity`() {
    val expression = QProduct.product.typedValueExpressionOf { it.id }

    assertThat(expression).isNotNull()
    assertThat(expression.type).isEqualTo(TypedValue::class.java)
  }

  @Test
  fun `should create TypedValueExpression for UUID ID entity`() {
    val expression = QOrder.order.typedValueExpressionOf { it.id }

    assertThat(expression).isNotNull()
    assertThat(expression.type).isEqualTo(TypedValue::class.java)
  }

  @Test
  fun `should create equality predicate for String ID`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val userId = TypedValue.typedValueFor("user-123", User::class)
    val predicate = expression.eq(userId)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id = user-123")
  }

  @Test
  fun `should create inequality predicate for String ID`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val userId = TypedValue.typedValueFor("user-123", User::class)
    val predicate = expression.ne(userId)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id != user-123")
  }

  @Test
  fun `should create IN predicate`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val userIds =
      listOf(
        TypedValue.typedValueFor("user-1", User::class),
        TypedValue.typedValueFor("user-2", User::class),
        TypedValue.typedValueFor("user-3", User::class),
      )

    val predicate = expression.isIn(userIds)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id in [user-1, user-2, user-3]")
  }

  @Test
  fun `should create NOT IN predicate`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val userIds =
      listOf(
        TypedValue.typedValueFor("user-1", User::class),
        TypedValue.typedValueFor("user-2", User::class),
      )

    val predicate = expression.notIn(userIds)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id not in [user-1, user-2]")
  }

  @Test
  fun `should create IS NULL predicate`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val predicate = expression.isNull()

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id is null")
  }

  @Test
  fun `should create IS NOT NULL predicate`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    val predicate = expression.isNotNull()

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("user.id is not null")
  }

  @Test
  fun `should create TypedValue instance from query result`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    // Simulate QueryDSL result row
    val result = expression.newInstance("user-456")

    assertThat(result).isNotNull()
    assertThat(result!!.value).isEqualTo("user-456")
    assertThat(result.type).isEqualTo(User::class)
  }

  @Test
  fun `should create equality predicate for Long ID`() {
    val expression = QProduct.product.typedValueExpressionOf { it.id }

    val productId = TypedValue.typedValueFor(42L, Product::class)
    val predicate = expression.eq(productId)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("product.id = 42")
  }

  @Test
  fun `should create equality predicate for UUID ID`() {
    val expression = QOrder.order.typedValueExpressionOf { it.id }

    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val orderId = TypedValue.typedValueFor(uuid, Order::class)
    val predicate = expression.eq(orderId)

    assertThat(predicate).isNotNull()
    assertThat(predicate.toString()).isEqualTo("order.id = 123e4567-e89b-12d3-a456-426614174000")
  }

  @Test
  fun `should expose underlying path`() {
    val expression = QUser.user.typedValueExpressionOf { it.id }

    assertThat(expression.path()).isEqualTo(QUser.user.id)
  }
}
