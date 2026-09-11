/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SerializableTest {

  class User

  class Order

  /** A downstream subclass with a non-uniform constructor, as users commonly write. */
  class UserId(id: String) : TypedString<User>(id, User::class)

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

  @Test
  fun `generic TypedValue should survive Java serialization round trip`() {
    val original = TypedValue.typedValueFor("id-123", User::class)

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(TypedValue::class.java)
  }

  @Test
  fun `TypedString should survive Java serialization round trip`() {
    val original = "id-123".toTypedString<User>()

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(TypedString::class.java)
  }

  @Test
  fun `TypedInt should survive Java serialization round trip`() {
    val original = 42.toTypedInt<User>()

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(TypedInt::class.java)
  }

  @Test
  fun `TypedLong should survive Java serialization round trip`() {
    val original = 42L.toTypedLong<User>()

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(TypedLong::class.java)
  }

  @Test
  fun `TypedUuid should survive Java serialization round trip`() {
    val original = UUID.randomUUID().toTypedUuid<Order>()

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(TypedUuid::class.java)
  }

  @Test
  fun `user-defined subclass should survive Java serialization round trip`() {
    val original = UserId("id-123")

    val copy = roundTrip(original)

    assertRoundTripped(original, copy)
    assertThat(copy).isExactlyInstanceOf(UserId::class.java)
  }

  @Test
  fun `deserialized TypedValue should not be equal to same value with another type`() {
    val copy = roundTrip("id-123".toTypedString<User>())

    assertThat(copy).isNotEqualTo("id-123".toTypedString<Order>())
    assertThat(copy.isAboutType<User>()).isTrue()
    assertThat(copy.isAboutType<Order>()).isFalse()
  }

  @Test
  fun `TypedValues inside collections should survive Java serialization round trip`() {
    val list = arrayListOf("a".toTypedString<User>(), "b".toTypedString<User>())
    val set = hashSetOf(1L.toTypedLong<Order>(), 2L.toTypedLong<Order>())

    val listCopy = roundTrip(list)
    val setCopy = roundTrip(set)

    assertThat(listCopy).isEqualTo(list)
    assertThat(setCopy).isEqualTo(set)
    assertThat(setCopy).contains(1L.toTypedLong<Order>(), 2L.toTypedLong<Order>())
  }

  private fun <V : Comparable<V>, T : Any> assertRoundTripped(
    original: TypedValue<V, T>,
    copy: TypedValue<V, T>,
  ) {
    assertThat(copy).isNotSameAs(original)
    assertThat(copy).isEqualTo(original)
    assertThat(copy.hashCode()).isEqualTo(original.hashCode())
    assertThat(copy.value).isEqualTo(original.value)
    assertThat(copy.type).isEqualTo(original.type)
    assertThat(copy.compareTo(original)).isZero()
    assertThat(copy.toString()).isEqualTo(original.toString())
  }

  @Suppress("UNCHECKED_CAST")
  private fun <S : Serializable> roundTrip(value: S): S {
    val bytes =
      ByteArrayOutputStream().use { bos ->
        ObjectOutputStream(bos).use { it.writeObject(value) }
        bos.toByteArray()
      }
    return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as S
  }
}
