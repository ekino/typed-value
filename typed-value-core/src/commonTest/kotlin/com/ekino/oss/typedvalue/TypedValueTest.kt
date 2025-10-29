/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class TypedValueTest {

  // Test entities
  class User

  open class Product

  class Order : Product()

  @Test
  fun should_create_TypedValue_with_String_ID() {
    val userId = TypedValue.typedValueFor("user-123", User::class)

    assertThat(userId.value).isEqualTo("user-123")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun should_create_TypedValue_with_Long_ID() {
    val productId = TypedValue.typedValueFor(42L, Product::class)

    assertThat(productId.value).isEqualTo(42L)
    assertThat(productId.type).isEqualTo(Product::class)
  }

  @Test
  fun should_create_TypedValue_using_reified_extension() {
    val userId = "user-123".toTypedValueFor<String, User>()

    assertThat(userId.value).isEqualTo("user-123")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun should_return_null_for_typedValueOrNullFor_with_null_raw_ID() {
    val result = TypedValue.typedValueOrNullFor(null, User::class)

    assertThat(result).isNull()
  }

  @Test
  fun should_return_TypedValue_for_typedValueOrNullFor_with_non_null_raw_ID() {
    val result = TypedValue.typedValueOrNullFor("user-789", User::class)

    assertThat(result).isNotNull()
    assertThat(result!!.value).isEqualTo("user-789")
  }

  @Test
  fun should_be_equal_when_same_ID_and_compatible_types() {
    val id1 = TypedValue.typedValueFor("abc", User::class)
    val id2 = TypedValue.typedValueFor("abc", User::class)

    assertThat(id1 == id2).isTrue()
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode())
  }

  @Test
  fun should_not_be_equal_when_different_IDs() {
    val id1 = TypedValue.typedValueFor("abc", User::class)
    val id2 = TypedValue.typedValueFor("xyz", User::class)

    assertThat(id1 == id2).isFalse()
  }

  @Test
  fun should_not_be_equal_when_incompatible_types() {
    val userId = TypedValue.typedValueFor("123", User::class)
    val productId = TypedValue.typedValueFor("123", Product::class)

    assertThat(userId == productId).isFalse()
  }

  @Test
  fun should_not_be_equal_when_types_differ_even_in_inheritance_hierarchy() {
    val productId = TypedValue.typedValueFor("123", Product::class)
    val orderId = TypedValue.typedValueFor("123", Order::class)

    // In multiplatform, inheritance checks are not supported - exact type matching only
    assertThat(productId == orderId).isFalse()
  }

  @Test
  fun should_compare_by_ID_value_first() {
    val id1 = TypedValue.typedValueFor("aaa", User::class)
    val id2 = TypedValue.typedValueFor("bbb", User::class)

    assertThat(id1).isLessThan(id2)
    assertThat(id2).isGreaterThan(id1)
  }

  @Test
  fun should_compare_by_type_hashCode_when_IDs_are_equal() {
    val orderId = TypedValue.typedValueFor("123", Order::class)
    val productId = TypedValue.typedValueFor("123", Product::class)

    // When IDs are equal, comparison uses type hashCode for stable ordering
    // (type names not available on JS, so hashCode is used instead)
    @Suppress("UNCHECKED_CAST")
    val comparison =
      (orderId as TypedValue<String, Any>).compareTo(productId as TypedValue<String, Any>)
    // The result depends on hashCode ordering, just verify it's deterministic (not zero since types
    // differ)
    assertThat(comparison != 0 || Order::class.hashCode() == Product::class.hashCode()).isTrue()
  }

  @Test
  fun should_compare_Long_IDs_correctly() {
    val id1 = TypedValue.typedValueFor(1L, User::class)
    val id2 = TypedValue.typedValueFor(100L, User::class)

    assertThat(id1).isLessThan(id2)
  }

  @Test
  fun should_check_type_with_isAboutType() {
    val userId = "user-123".toTypedValueFor<String, User>()

    assertThat(userId.isAboutType<User>()).isTrue()
    assertThat(userId.isAboutType<Product>()).isFalse()
  }

  @Test
  fun isAboutType_uses_exact_type_matching_not_inheritance() {
    val orderId = "order-123".toTypedValueFor<String, Order>()

    assertThat(orderId.isAboutType<Order>()).isTrue()
    // In multiplatform, inheritance checks are not supported - exact type matching only
    assertThat(orderId.isAboutType<Product>()).isFalse()
  }

  @Test
  fun should_return_TypedValue_with_takeIfAboutType_when_type_matches() {
    val userId = "user-123".toTypedValueFor<String, User>()

    val result = userId.takeIfAboutType<String, User>()

    assertThat(result).isNotNull()
    assertThat(result!!.value).isEqualTo("user-123")
  }

  @Test
  fun should_return_null_with_takeIfAboutType_when_type_does_not_match() {
    val userId = "user-123".toTypedValueFor<String, User>()

    val result = userId.takeIfAboutType<String, Product>()

    assertThat(result).isNull()
  }

  @Test
  fun should_extract_raw_IDs_from_collection() {
    val ids =
      listOf(
        TypedValue.typedValueFor("id1", User::class),
        TypedValue.typedValueFor("id2", User::class),
        TypedValue.typedValueFor("id3", User::class),
      )

    val rawIds = ids.toRawIds()

    assertThat(rawIds).isEqualTo(listOf("id1", "id2", "id3"))
  }

  @Test
  fun should_create_builder_function() {
    val builder = TypedValue.typedValueBuilderFor<String, User>(User::class)

    val userId = builder("user-999")

    assertThat(userId.value).isEqualTo("user-999")
    assertThat(userId.type).isEqualTo(User::class)
  }

  @Test
  fun toString_should_show_type_and_ID() {
    val userId = TypedValue.typedValueFor("user-123", User::class)

    assertThat(userId.toString()).isEqualTo("User(user-123)")
  }

  @Test
  fun should_sort_collection_of_TypedValues() {
    val ids =
      listOf(
        TypedValue.typedValueFor("z", User::class),
        TypedValue.typedValueFor("a", User::class),
        TypedValue.typedValueFor("m", User::class),
      )

    val sorted = ids.sorted()

    assertThat(sorted.map { it.value }).isEqualTo(listOf("a", "m", "z"))
  }
}
