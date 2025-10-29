/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.test.Test

class CommonTypesTest {

  // Test entities
  class User

  class Product

  class Order

  class Counter

  @Test
  fun should_create_TypedString_using_factory_method() {
    val userId = TypedString.of("user-123", User::class)

    assertThat(userId).isInstanceOf<TypedString<User>>()
    assertThat(userId.value).isEqualTo("user-123")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun should_create_TypedString_using_extension_function() {
    val userId = "user-123".toTypedString<User>()

    assertThat(userId).isInstanceOf<TypedString<User>>()
    assertThat(userId.value).isEqualTo("user-123")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun TypedString_should_be_compatible_with_TypedValue_interface() {
    val userId: TypedValue<String, User> = "user-123".toTypedString()

    assertThat(userId.value).isEqualTo("user-123")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun should_create_TypedInt_using_factory_method() {
    val counterId = TypedInt.of(42, Counter::class)

    assertThat(counterId).isInstanceOf<TypedInt<Counter>>()
    assertThat(counterId.value).isEqualTo(42)
    assertThat(counterId.type).isEqualTo(Counter::class)
  }

  @Test
  fun should_create_TypedInt_using_extension_function() {
    val counterId = 42.toTypedInt<Counter>()

    assertThat(counterId).isInstanceOf<TypedInt<Counter>>()
    assertThat(counterId.value).isEqualTo(42)
    assertThat(counterId.type).isEqualTo(Counter::class)
  }

  @Test
  fun TypedInt_should_be_compatible_with_TypedValue_interface() {
    val counterId: TypedValue<Int, Counter> = 42.toTypedInt()

    assertThat(counterId.value).isEqualTo(42)
  }

  @Test
  fun should_create_TypedLong_using_factory_method() {
    val productId = TypedLong.of(42L, Product::class)

    assertThat(productId).isInstanceOf<TypedLong<Product>>()
    assertThat(productId.value).isEqualTo(42L)
    assertThat(productId.type).isEqualTo(Product::class)
  }

  @Test
  fun should_create_TypedLong_using_extension_function() {
    val productId = 42L.toTypedLong<Product>()

    assertThat(productId).isInstanceOf<TypedLong<Product>>()
    assertThat(productId.value).isEqualTo(42L)
    assertThat(productId.type).isEqualTo(Product::class)
  }

  @Test
  fun TypedLong_should_be_compatible_with_TypedValue_interface() {
    val productId: TypedValue<Long, Product> = 42L.toTypedLong()

    assertThat(productId.value).isEqualTo(42L)
  }

  @Test
  fun should_compare_TypedString_instances_correctly() {
    val id1 = "user-a".toTypedString<User>()
    val id2 = "user-b".toTypedString<User>()

    assertThat(id1 < id2).isTrue()
  }

  @Test
  fun should_compare_TypedLong_instances_correctly() {
    val id1 = 1L.toTypedLong<Product>()
    val id2 = 100L.toTypedLong<Product>()

    assertThat(id1 < id2).isTrue()
  }

  @Test
  fun should_use_TypedString_in_collections() {
    val userIds =
      listOf(
        "user-c".toTypedString<User>(),
        "user-a".toTypedString<User>(),
        "user-b".toTypedString<User>(),
      )

    val sorted = userIds.sorted()

    assertThat(sorted.map { it.value }).isEqualTo(listOf("user-a", "user-b", "user-c"))
  }

  @Test
  fun should_extract_raw_values_from_TypedString_collection() {
    val userIds =
      listOf(
        "user-1".toTypedString<User>(),
        "user-2".toTypedString<User>(),
        "user-3".toTypedString<User>(),
      )

    val rawIds = userIds.toRawIds()

    assertThat(rawIds).isEqualTo(listOf("user-1", "user-2", "user-3"))
  }

  @Test
  fun should_use_TypedString_toString_format() {
    val userId = "user-123".toTypedString<User>()

    assertThat(userId.toString()).isEqualTo("User(user-123)")
  }

  @Test
  fun should_check_type_with_isAboutType_for_TypedString() {
    val userId = "user-123".toTypedString<User>()

    assertThat(userId.isAboutType<User>()).isTrue()
  }
}
