/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.jackson

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import java.util.UUID
import kotlin.reflect.KClass
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue

@Suppress("LargeClass")
class TypedValueJacksonTest {

  class User

  class Product

  class Order

  // Custom TypedValue subclasses for testing
  class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

  class TypedId2<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

  class FixedTypedId(id: String) : TypedString<User>(id, User::class)

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

  // ==================== Custom TypedValue Registration Tests ====================

  @Test
  fun `should serialize and deserialize custom TypedValue subclass`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
        }
      )
    }

    val dto = CustomTypedIdDto(id = TypedId("custom-123", User::class))
    val json = customObjectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"custom-123"}""")

    val deserialized = customObjectMapper.readValue<CustomTypedIdDto>(json)
    assertThat(deserialized.id).isInstanceOf(TypedId::class)
    assertThat(deserialized.id.value).isEqualTo("custom-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize and deserialize list of custom TypedValue`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
        }
      )
    }

    val dto =
      CustomTypedIdListDto(
        ids =
          listOf(
            TypedId("id-1", User::class),
            TypedId("id-2", User::class),
            TypedId("id-3", User::class),
          )
      )

    val json = customObjectMapper.writeValueAsString(dto)
    val deserialized = customObjectMapper.readValue<CustomTypedIdListDto>(json)

    assertThat(deserialized.ids.map { it.value }).isEqualTo(listOf("id-1", "id-2", "id-3"))
    assertThat(deserialized.ids.all { it.type == User::class }).isEqualTo(true)
  }

  @Test
  fun `should serialize and deserialize custom FixedTypeId`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<FixedTypedId, String> { value, _ -> FixedTypedId(value) }
        }
      )
    }

    val dto = FixedTypedIdDto(id = FixedTypedId("custom-123"))
    val json = customObjectMapper.writeValueAsString(dto)

    assertThat(json).isEqualTo("""{"id":"custom-123"}""")

    val deserialized = customObjectMapper.readValue<FixedTypedIdDto>(json)
    assertThat(deserialized.id).isInstanceOf(FixedTypedId::class)
    assertThat(deserialized.id.value).isEqualTo("custom-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should fail when entity Class cannot be resolved`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
        }
      )
    }
    val json = """{"id":"custom-123"}"""

    assertFailure { customObjectMapper.readValue<MissingEntityTypeDto>(json) }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        """
        |Cannot resolve entity type parameter for property 'id' with TypedValue subtype: TypedId.
        |Please provide a valid type parameter (not Any or *).
        """
          .trimMargin()
      )
  }

  @Test
  fun `should fail when value Class cannot be resolved`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(TypedValueModule())
    }
    val json = """{"id":"custom-123"}"""

    assertFailure { customObjectMapper.readValue<MissingValueTypeDto>(json) }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        """
        |Cannot resolve value type parameter for property 'id' with TypedValue subtype: TypedValue. 
        |Please provide a valid type parameter (not Any or *).
        """
          .trimMargin()
      )
  }

  @Test
  fun `should throw error when registering built-in TypedString type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedString<*>, String> { value, entityKClass ->
            TypedString(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        "Cannot register built-in TypedValue type com.ekino.oss.typedvalue.TypedString"
      )
  }

  @Test
  fun `should throw error when registering built-in TypedValue type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedValue<String, *>, String> { value, entityKClass ->
            TypedValue(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        "Cannot register built-in TypedValue type com.ekino.oss.typedvalue.TypedValue"
      )
  }

  @Test
  fun `should throw error when registering built-in TypedUuid type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedUuid<*>, UUID> { value, entityKClass ->
            TypedUuid(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        "Cannot register built-in TypedValue type com.ekino.oss.typedvalue.TypedUuid"
      )
  }

  @Test
  fun `should throw error when registering built-in TypedLong type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedLong<*>, Long> { value, entityKClass ->
            TypedLong(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains(
        "Cannot register built-in TypedValue type com.ekino.oss.typedvalue.TypedLong"
      )
  }

  @Test
  fun `should throw error when registering built-in TypedInt type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedInt<*>, Int> { value, entityKClass ->
            TypedInt(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains("Cannot register built-in TypedValue type com.ekino.oss.typedvalue.TypedInt")
  }

  @Test
  fun `should throw error when registering duplicate custom type`() {
    assertFailure {
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains("TypedValue class TypedId is already registered")
  }

  @Test
  fun `should throw error with context when custom constructor fails`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { _, _ ->
            throw IllegalArgumentException("Constructor intentionally failed")
          }
        }
      )
    }

    val json = """{"id":"test-id"}"""

    assertFailure { customObjectMapper.readValue<CustomTypedIdDto>(json) }
      .transform { exception ->
        generateSequence(exception) { it.cause }.firstOrNull { it is IllegalStateException }
      }
      .isNotNull()
      .messageContains(
        "Failed to construct TypedValue of type TypedId for entity User at property 'id' with value: test-id"
      )
  }

  @Test
  fun `should use Java-style registration with Class parameters`() {
    val module = TypedValueModule()
    module.registerCustomTypedValue(TypedId::class.java, String::class.java) { value, entityClass ->
      TypedId(value, entityClass.kotlin)
    }

    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(module)
    }

    val dto = CustomTypedIdDto(id = TypedId("java-style-123", User::class))
    val json = customObjectMapper.writeValueAsString(dto)
    val deserialized = customObjectMapper.readValue<CustomTypedIdDto>(json)

    assertThat(deserialized.id.value).isEqualTo("java-style-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should use TypedValueConstructor functional interface`() {
    val module = TypedValueModule()
    val constructor =
      TypedValueModule.TypedValueConstructor<TypedId<*>, String> { value, entityClass ->
        TypedId(value, entityClass.kotlin)
      }

    module.registerCustomTypedValue(TypedId::class.java, String::class.java, constructor)

    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(module)
    }

    val dto = CustomTypedIdDto(id = TypedId("functional-123", User::class))
    val json = customObjectMapper.writeValueAsString(dto)
    val deserialized = customObjectMapper.readValue<CustomTypedIdDto>(json)

    assertThat(deserialized.id.value).isEqualTo("functional-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
  }

  @Test
  fun `should work with multiple custom types registered`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
            TypedId(value, entityKClass)
          }
          registerCustomTypedValue<TypedId2<*>, String> { value, entityKClass ->
            TypedId2(value, entityKClass)
          }
        }
      )
    }

    val dto =
      MixedDto(
        id1 = TypedId("user-123", User::class),
        id2 = TypedId2("product-456", Product::class),
      )

    val json = customObjectMapper.writeValueAsString(dto)
    val deserialized = customObjectMapper.readValue<MixedDto>(json)

    assertThat(deserialized.id1).isInstanceOf(TypedId::class)
    assertThat(deserialized.id1.value).isEqualTo("user-123")
    assertThat(deserialized.id2).isInstanceOf(TypedId2::class)
    assertThat(deserialized.id2.value).isEqualTo("product-456")
  }

  @Test
  fun `should fail when using unregistered custom TypedValue subtype`() {
    val json = """{"id":"test-123"}"""

    assertFailure { objectMapper.readValue<UnregisteredDto>(json) }
      .transform { exception ->
        generateSequence(exception) { it.cause }.firstOrNull { it is IllegalStateException }
      }
      .isNotNull()
      .all {
        messageContains("Unsupported TypedValue subtype: UnregisteredTypedId")
        messageContains("Ensure the type is one of the built-in types")
        messageContains("or is registered via TypedValueModule.registerCustomTypedValue()")
      }
  }

  // ==================== Nullable TypedValue Tests ====================

  @Test
  fun `should serialize and deserialize nullable TypedValue fields`() {
    // Test with null
    val dtoWithNull = NullableIdDto(id = null)
    val jsonNull = objectMapper.writeValueAsString(dtoWithNull)
    assertThat(jsonNull).isEqualTo("""{"id":null}""")

    val deserializedNull = objectMapper.readValue<NullableIdDto>(jsonNull)
    assertThat(deserializedNull.id).isEqualTo(null)

    // Test with value
    val dtoWithValue = NullableIdDto(id = TypedString.of("user-123", User::class))
    val jsonValue = objectMapper.writeValueAsString(dtoWithValue)
    assertThat(jsonValue).isEqualTo("""{"id":"user-123"}""")

    val deserializedValue = objectMapper.readValue<NullableIdDto>(jsonValue)
    assertThat(deserializedValue.id?.value).isEqualTo("user-123")
    assertThat(deserializedValue.id?.type).isEqualTo(User::class)
  }

  @Test
  fun `should serialize and deserialize DTO with multiple nullable TypedValue fields`() {
    val dto =
      MultiNullableDto(
        id1 = TypedString.of("user-123", User::class),
        id2 = null,
        id3 = TypedUuid.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), Order::class),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<MultiNullableDto>(json)

    assertThat(deserialized.id1?.value).isEqualTo("user-123")
    assertThat(deserialized.id2).isEqualTo(null)
    assertThat(deserialized.id3?.value)
      .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
  }

  // ==================== Nested DTOs Tests ====================

  @Test
  fun `should serialize and deserialize nested DTOs with TypedValue`() {
    val dto =
      UserWithAddressDto(
        id = TypedString.of("user-123", User::class),
        name = "John Doe",
        address = AddressDto(id = TypedString.of("addr-456", AddressDto::class), street = "Main St"),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<UserWithAddressDto>(json)

    assertThat(deserialized.id.value).isEqualTo("user-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
    assertThat(deserialized.address.id.value).isEqualTo("addr-456")
    assertThat(deserialized.address.id.type).isEqualTo(AddressDto::class)
    assertThat(deserialized.name).isEqualTo("John Doe")
    assertThat(deserialized.address.street).isEqualTo("Main St")
  }

  @Test
  fun `should serialize and deserialize deeply nested DTOs with TypedValue`() {
    val dto =
      PersonDto(
        id = TypedString.of("person-123", PersonDto::class),
        name = "Jane Doe",
        address =
          AddressNestedDto(
            id = TypedString.of("addr-456", AddressNestedDto::class),
            street = "Oak Ave",
            city =
              CityDto(
                id = TypedString.of("city-789", CityDto::class),
                name = "Paris",
                country =
                  CountryDto(id = TypedString.of("country-001", CountryDto::class), name = "France"),
              ),
          ),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<PersonDto>(json)

    assertThat(deserialized.id.value).isEqualTo("person-123")
    assertThat(deserialized.address.id.value).isEqualTo("addr-456")
    assertThat(deserialized.address.city.id.value).isEqualTo("city-789")
    assertThat(deserialized.address.city.country.id.value).isEqualTo("country-001")
    assertThat(deserialized.address.city.country.name).isEqualTo("France")
  }

  // ==================== Type Hierarchy Resolution Tests ====================

  @Test
  fun `should correctly resolve type hierarchy for all convenience types`() {
    val dto =
      AllTypesDto(
        uuid = TypedUuid.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), User::class),
        string = TypedString.of("prod-123", Product::class),
        long = TypedLong.of(999L, Order::class),
        int = TypedInt.of(42, User::class),
      )

    val json = objectMapper.writeValueAsString(dto)
    val deserialized = objectMapper.readValue<AllTypesDto>(json)

    // Verify correct types are instantiated (not just TypedValue)
    assertThat(deserialized.uuid).isInstanceOf(TypedUuid::class)
    assertThat(deserialized.uuid.value)
      .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
    assertThat(deserialized.uuid.type).isEqualTo(User::class)

    assertThat(deserialized.string).isInstanceOf(TypedString::class)
    assertThat(deserialized.string.value).isEqualTo("prod-123")
    assertThat(deserialized.string.type).isEqualTo(Product::class)

    assertThat(deserialized.long).isInstanceOf(TypedLong::class)
    assertThat(deserialized.long.value).isEqualTo(999L)
    assertThat(deserialized.long.type).isEqualTo(Order::class)

    assertThat(deserialized.int).isInstanceOf(TypedInt::class)
    assertThat(deserialized.int.value).isEqualTo(42)
    assertThat(deserialized.int.type).isEqualTo(User::class)
  }

  @Test
  fun `should correctly resolve type hierarchy for custom TypedValue extending convenience type`() {
    val customObjectMapper = jsonMapper {
      addModule(kotlinModule())
      addModule(
        TypedValueModule().apply {
          registerCustomTypedValue<CustomTypedString<*>, String> { value, entityKClass ->
            CustomTypedString(value, entityKClass)
          }
        }
      )
    }

    val dto = CustomStringDto(id = CustomTypedString("custom-str-123", User::class))
    val json = customObjectMapper.writeValueAsString(dto)
    val deserialized = customObjectMapper.readValue<CustomStringDto>(json)

    assertThat(deserialized.id).isInstanceOf(CustomTypedString::class)
    assertThat(deserialized.id.value).isEqualTo("custom-str-123")
    assertThat(deserialized.id.type).isEqualTo(User::class)
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

  // Custom type DTOs
  data class CustomTypedIdDto(val id: TypedId<User>)

  data class MissingEntityTypeDto(val id: TypedId<*>)

  data class MissingValueTypeDto(val id: TypedValue<*, User>)

  data class FixedTypedIdDto(val id: FixedTypedId)

  data class CustomTypedIdListDto(val ids: List<TypedId<User>>)

  data class MixedDto(val id1: TypedId<User>, val id2: TypedId2<Product>)

  // Unregistered type classes
  class UnregisteredTypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

  data class UnregisteredDto(val id: UnregisteredTypedId<User>)

  // Nullable DTOs
  data class NullableIdDto(val id: TypedString<User>?)

  data class MultiNullableDto(
    val id1: TypedString<User>?,
    val id2: TypedLong<Product>?,
    val id3: TypedUuid<Order>?,
  )

  // Nested DTOs
  data class AddressDto(val id: TypedString<AddressDto>, val street: String)

  data class UserWithAddressDto(
    val id: TypedString<User>,
    val name: String,
    val address: AddressDto,
  )

  // Deeply nested DTOs
  data class CountryDto(val id: TypedString<CountryDto>, val name: String)

  data class CityDto(val id: TypedString<CityDto>, val name: String, val country: CountryDto)

  data class AddressNestedDto(
    val id: TypedString<AddressNestedDto>,
    val street: String,
    val city: CityDto,
  )

  data class PersonDto(
    val id: TypedString<PersonDto>,
    val name: String,
    val address: AddressNestedDto,
  )

  // Type hierarchy DTOs
  data class AllTypesDto(
    val uuid: TypedUuid<User>,
    val string: TypedString<Product>,
    val long: TypedLong<Order>,
    val int: TypedInt<User>,
  )

  // Custom extending convenience type
  class CustomTypedString<T : Any>(value: String, type: KClass<T>) : TypedString<T>(value, type)

  data class CustomStringDto(val id: CustomTypedString<User>)
}
