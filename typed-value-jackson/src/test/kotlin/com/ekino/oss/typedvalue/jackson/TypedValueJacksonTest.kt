/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue

class TypedValueJacksonTest {

  class User

  class Product

  class Order

  private val objectMapper = jsonMapper {
    addModule(kotlinModule())
    addModule(TypedValueModule())
  }

  @Test
  fun `should serialize TypedValue with String ID`() {
    val dto = UserDto(id = TypedValue.typedValueFor("user-123", User::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"user-123"}""")
  }

  @Test
  fun `should deserialize TypedValue with String ID`() {
    val json = """{"id":"user-456"}"""

    val dto = objectMapper.readValue<UserDto>(json)

    assertThat(dto.id.value).isEqualTo("user-456")
    assertThat(dto.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize TypedValue with Int ID`() {
    val dto = GenericIntDto(id = TypedValue.typedValueFor(42, User::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":42}""")
  }

  @Test
  fun `should deserialize TypedValue with Int ID`() {
    val json = """{"id":999}"""

    val dto = objectMapper.readValue<GenericIntDto>(json)

    assertThat(dto.id.value).isEqualTo(999)
    assertThat(dto.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize TypedValue with Long ID`() {
    val dto = ProductDto(id = TypedValue.typedValueFor(42L, Product::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":42}""")
  }

  @Test
  fun `should deserialize TypedValue with Long ID`() {
    val json = """{"id":999}"""

    val dto = objectMapper.readValue<ProductDto>(json)

    assertThat(dto.id.value).isEqualTo(999L)
    assertThat(dto.id.type).isEqualTo(Product::class)
  }

  @Test
  fun `should serialize TypedValue with UUID`() {
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val dto = OrderDto(id = TypedValue.typedValueFor(uuid, Order::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"123e4567-e89b-12d3-a456-426614174000"}""")
  }

  @Test
  fun `should deserialize TypedValue with UUID`() {
    val json = """{"id":"123e4567-e89b-12d3-a456-426614174000"}"""

    val dto = objectMapper.readValue<OrderDto>(json)

    assertThat(dto.id.value).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    assertThat(dto.id.type).isEqualTo(Order::class)
  }

  @Test
  fun `should serialize and deserialize DTO with multiple TypedValue fields`() {
    val dto =
      MultiIdDto(
        userId = TypedValue.typedValueFor("user-123", User::class),
        productId = TypedValue.typedValueFor(42L, Product::class),
        orderId =
          TypedValue.typedValueFor(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            Order::class,
          ),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<MultiIdDto>(json)

    assertThat(deserialized.userId.value).isEqualTo("user-123")
    assertThat(deserialized.userId.type).isEqualTo(User::class)
    assertThat(deserialized.productId.value).isEqualTo(42L)
    assertThat(deserialized.productId.type).isEqualTo(Product::class)
    assertThat(deserialized.orderId.value)
      .isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    assertThat(deserialized.orderId.type).isEqualTo(Order::class)
  }

  @Test
  fun `should serialize and deserialize list of TypedValues`() {
    val dto =
      UserListDto(
        userIds =
          listOf(
            TypedValue.typedValueFor("user-1", User::class),
            TypedValue.typedValueFor("user-2", User::class),
            TypedValue.typedValueFor("user-3", User::class),
          )
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<UserListDto>(json)

    assertThat(deserialized.userIds.map { it.value })
      .isEqualTo(listOf("user-1", "user-2", "user-3"))
    assertThat(deserialized.userIds.all { it.type == User::class }).isEqualTo(true)
  }

  // ==================== TypedString Tests ====================

  @Test
  fun `should serialize TypedString`() {
    val dto = TypedStringUserDto(id = TypedString.of("user-abc", User::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"user-abc"}""")
  }

  @Test
  fun `should deserialize TypedString`() {
    val json = """{"id":"user-xyz"}"""

    val dto = objectMapper.readValue<TypedStringUserDto>(json)

    assertThat(dto.id).isInstanceOf(TypedString::class)
    assertThat(dto.id.value).isEqualTo("user-xyz")
    assertThat(dto.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize and deserialize list of TypedString`() {
    val dto =
      TypedStringListDto(
        ids =
          listOf(
            TypedString.of("id-1", User::class),
            TypedString.of("id-2", User::class),
            TypedString.of("id-3", User::class),
          )
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<TypedStringListDto>(json)

    assertThat(deserialized.ids.map { it.value }).isEqualTo(listOf("id-1", "id-2", "id-3"))
    assertThat(deserialized.ids.all { it.type == User::class }).isEqualTo(true)
  }

  // ==================== TypedLong Tests ====================

  @Test
  fun `should serialize TypedLong`() {
    val dto = TypedLongProductDto(id = TypedLong.of(12345L, Product::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":12345}""")
  }

  @Test
  fun `should deserialize TypedLong`() {
    val json = """{"id":67890}"""

    val dto = objectMapper.readValue<TypedLongProductDto>(json)

    assertThat(dto.id).isInstanceOf(TypedLong::class)
    assertThat(dto.id.value).isEqualTo(67890L)
    assertThat(dto.id.type).isEqualTo(Product::class)
  }

  @Test
  fun `should serialize and deserialize list of TypedLong`() {
    val dto =
      TypedLongListDto(
        ids =
          listOf(
            TypedLong.of(100L, Product::class),
            TypedLong.of(200L, Product::class),
            TypedLong.of(300L, Product::class),
          )
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<TypedLongListDto>(json)

    assertThat(deserialized.ids.map { it.value }).isEqualTo(listOf(100L, 200L, 300L))
    assertThat(deserialized.ids.all { it.type == Product::class }).isEqualTo(true)
  }

  // ==================== TypedInt Tests ====================

  @Test
  fun `should serialize TypedInt`() {
    val dto = TypedIntDto(id = TypedInt.of(42, User::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":42}""")
  }

  @Test
  fun `should deserialize TypedInt`() {
    val json = """{"id":99}"""

    val dto = objectMapper.readValue<TypedIntDto>(json)

    assertThat(dto.id).isInstanceOf(TypedInt::class)
    assertThat(dto.id.value).isEqualTo(99)
    assertThat(dto.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize and deserialize list of TypedInt`() {
    val dto =
      TypedIntListDto(
        ids =
          listOf(
            TypedInt.of(1, User::class),
            TypedInt.of(2, User::class),
            TypedInt.of(3, User::class),
          )
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<TypedIntListDto>(json)

    assertThat(deserialized.ids.map { it.value }).isEqualTo(listOf(1, 2, 3))
    assertThat(deserialized.ids.all { it.type == User::class }).isEqualTo(true)
  }

  // ==================== TypedUuid Tests ====================

  @Test
  fun `should serialize TypedUuid`() {
    val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val dto = TypedUuidOrderDto(id = TypedUuid.of(uuid, Order::class))

    val json = objectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"550e8400-e29b-41d4-a716-446655440000"}""")
  }

  @Test
  fun `should deserialize TypedUuid`() {
    val json = """{"id":"550e8400-e29b-41d4-a716-446655440000"}"""

    val dto = objectMapper.readValue<TypedUuidOrderDto>(json)

    assertThat(dto.id).isInstanceOf(TypedUuid::class)
    assertThat(dto.id.value).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
    assertThat(dto.id.type).isEqualTo(Order::class)
  }

  @Test
  fun `should serialize and deserialize list of TypedUuid`() {
    val dto =
      TypedUuidListDto(
        ids =
          listOf(
            TypedUuid.of(UUID.fromString("00000000-0000-0000-0000-000000000001"), Order::class),
            TypedUuid.of(UUID.fromString("00000000-0000-0000-0000-000000000002"), Order::class),
            TypedUuid.of(UUID.fromString("00000000-0000-0000-0000-000000000003"), Order::class),
          )
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<TypedUuidListDto>(json)

    assertThat(deserialized.ids.map { it.value })
      .isEqualTo(
        listOf(
          UUID.fromString("00000000-0000-0000-0000-000000000001"),
          UUID.fromString("00000000-0000-0000-0000-000000000002"),
          UUID.fromString("00000000-0000-0000-0000-000000000003"),
        )
      )
    assertThat(deserialized.ids.all { it.type == Order::class }).isEqualTo(true)
  }

  // ==================== Mixed Convenience Types Test ====================

  @Test
  fun `should serialize and deserialize DTO with all convenience types`() {
    val dto =
      AllConvenienceTypesDto(
        stringId = TypedString.of("user-123", User::class),
        longId = TypedLong.of(456L, Product::class),
        intId = TypedInt.of(789, User::class),
        uuidId = TypedUuid.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), Order::class),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<AllConvenienceTypesDto>(json)

    assertThat(deserialized.stringId).isInstanceOf(TypedString::class)
    assertThat(deserialized.stringId.value).isEqualTo("user-123")
    assertThat(deserialized.stringId.type).isEqualTo(User::class)

    assertThat(deserialized.longId).isInstanceOf(TypedLong::class)
    assertThat(deserialized.longId.value).isEqualTo(456L)
    assertThat(deserialized.longId.type).isEqualTo(Product::class)

    assertThat(deserialized.intId).isInstanceOf(TypedInt::class)
    assertThat(deserialized.intId.value).isEqualTo(789)
    assertThat(deserialized.intId.type).isEqualTo(User::class)

    assertThat(deserialized.uuidId).isInstanceOf(TypedUuid::class)
    assertThat(deserialized.uuidId.value)
      .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
    assertThat(deserialized.uuidId.type).isEqualTo(Order::class)
  }

  // Test DTOs
  data class UserDto(val id: TypedValue<String, User>)

  data class GenericIntDto(val id: TypedValue<Int, User>)

  data class ProductDto(val id: TypedValue<Long, Product>)

  data class OrderDto(val id: TypedValue<UUID, Order>)

  data class MultiIdDto(
    val userId: TypedValue<String, User>,
    val productId: TypedValue<Long, Product>,
    val orderId: TypedValue<UUID, Order>,
  )

  data class UserListDto(val userIds: List<TypedValue<String, User>>)

  // Convenience type DTOs
  data class TypedStringUserDto(val id: TypedString<User>)

  data class TypedStringListDto(val ids: List<TypedString<User>>)

  data class TypedLongProductDto(val id: TypedLong<Product>)

  data class TypedLongListDto(val ids: List<TypedLong<Product>>)

  data class TypedIntDto(val id: TypedInt<User>)

  data class TypedIntListDto(val ids: List<TypedInt<User>>)

  data class TypedUuidOrderDto(val id: TypedUuid<Order>)

  data class TypedUuidListDto(val ids: List<TypedUuid<Order>>)

  data class AllConvenienceTypesDto(
    val stringId: TypedString<User>,
    val longId: TypedLong<Product>,
    val intId: TypedInt<User>,
    val uuidId: TypedUuid<Order>,
  )
}
