/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.serialization

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.integrationtests.AbstractIntegrationTest
import com.ekino.oss.typedvalue.toTypedUuid
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

/**
 * Integration tests demonstrating Jackson serialization/deserialization of TypedValue types.
 *
 * These tests showcase how the [com.ekino.oss.typedvalue.jackson.TypedValueModule] integrates with
 * Spring Boot's ObjectMapper to provide seamless JSON handling for:
 * - [TypedUuid] - UUID-based typed identifiers
 * - [TypedString] - String-based typed identifiers
 * - Custom TypedValue subclasses (e.g., [MyId])
 *
 * ## Configuration
 *
 * The TypedValueModule must be registered as a Spring bean:
 * ```kotlin
 * @Configuration
 * class JacksonConfiguration {
 *   @Bean
 *   fun typedValueModule() = TypedValueModule()
 * }
 * ```
 *
 * ## Key Features Demonstrated
 *
 * ### Direct Serialization
 * TypedValue instances serialize to their raw value:
 * ```kotlin
 * val id = TypedUuid.of(uuid, User::class)
 * objectMapper.writeValueAsString(id) // "550e8400-e29b-41d4-a716-446655440000"
 * ```
 *
 * ### DTO Serialization
 * TypedValue fields in DTOs serialize naturally:
 * ```kotlin
 * data class UserDto(val id: TypedUuid<User>, val name: String)
 * // Serializes to: {"id":"550e8400-e29b-41d4-a716-446655440000","name":"John"}
 * ```
 *
 * ### Deserialization with Type Resolution
 * The deserializer uses contextual type information to resolve the entity type:
 * ```kotlin
 * val json = """{"id":"550e8400-e29b-41d4-a716-446655440000","name":"John"}"""
 * val user = objectMapper.readValue(json, UserDto::class.java)
 * // user.id.type == User::class
 * ```
 *
 * ### Custom TypedValue Subclasses
 * You can create domain-specific ID types by extending TypedString/TypedUuid:
 * ```kotlin
 * class UserId(id: String) : TypedString<UserId>(id, UserId::class)
 * ```
 *
 * @see com.ekino.oss.typedvalue.jackson.TypedValueModule
 * @see com.ekino.oss.typedvalue.jackson.TypedValueSerializer
 * @see com.ekino.oss.typedvalue.jackson.TypedValueDeserializer
 */
class TypedValueSerializationTest : AbstractIntegrationTest() {

  @Autowired private lateinit var objectMapper: ObjectMapper

  /** Example of a custom TypedString subclass for domain-specific IDs. */
  class MyId(id: String) : TypedString<MyId>(id, MyId::class)

  data class PersonDto2(val name: String? = null, val someId: MyId)

  @Test
  fun `should serialize TypedString to string`() {
    val myId = MyId("test-id-123")

    val json = objectMapper.writeValueAsString(myId)

    assertThat(json).isEqualTo("\"test-id-123\"")
  }

  @Test
  fun `should deserialize MyId from JSON string`() {
    val json = "\"test-id-456\""

    val myId: MyId = objectMapper.readValue(json, MyId::class.java)

    assertThat(myId.value).isEqualTo("test-id-456")
  }

  @Test
  fun `should deserialize custom TypedString subclass in DTO`() {
    val id = "custom-id-789"
    val json = """{"name":"Jane Doe","someId":"$id"}"""

    val person = objectMapper.readValue(json, PersonDto2::class.java)

    assertThat(person.name).isEqualTo("Jane Doe")
    assertThat(person.someId).isNotNull()
    assertThat(person.someId.value).isEqualTo(id)
  }

  @Test
  fun `should serialize TypedUuid to UUID string`() {
    val uuid = UUID.randomUUID()
    val typedUuid: TypedUuid<String> = uuid.toTypedUuid()

    val json = objectMapper.writeValueAsString(typedUuid)

    assertThat(json).isEqualTo("\"$uuid\"")
  }

  @Test
  fun `should serialize object with TypedUuid field`() {
    val uuid = UUID.randomUUID()
    val data = mapOf("name" to "John Doe", "id" to uuid.toTypedUuid<String>())

    val json = objectMapper.writeValueAsString(data)

    assertThat(json).contains(""""name":"John Doe"""")
    assertThat(json).contains(""""id":"$uuid"""")
  }

  @Test
  fun `should handle null TypedUuid in serialization`() {
    val data = mapOf<String, Any?>("name" to "Test", "id" to null)

    val json = objectMapper.writeValueAsString(data)

    assertThat(json).contains(""""name":"Test"""")
    assertThat(json).contains(""""id":null""")
  }

  // DTO for deserialization tests
  data class PersonDto(val name: String? = null, val someId: TypedUuid<PersonDto>? = null)

  @Test
  fun `should deserialize TypedUuid from JSON string`() {
    val uuid = UUID.randomUUID()
    val json = """{"name":"Jane Doe","someId":"$uuid"}"""

    val person = objectMapper.readValue(json, PersonDto::class.java)

    assertThat(person.name).isEqualTo("Jane Doe")
    assertThat(person.someId).isNotNull()
    assertThat(person.someId?.value).isEqualTo(uuid)
  }

  @Test
  fun `should deserialize null TypedUuid from JSON`() {
    val json = """{"name":"No Id Person","someId":null}"""

    val person = objectMapper.readValue(json, PersonDto::class.java)

    assertThat(person.name).isEqualTo("No Id Person")
    assertThat(person.someId).isNull()
  }

  @Test
  fun `should round-trip serialize and deserialize`() {
    val uuid = UUID.randomUUID()
    val original = PersonDto(name = "Round-trip Test", someId = uuid.toTypedUuid())

    val json = objectMapper.writeValueAsString(original)
    val deserialized = objectMapper.readValue(json, PersonDto::class.java)

    assertThat(deserialized.name).isEqualTo(original.name)
    assertThat(deserialized.someId?.value).isEqualTo(original.someId?.value)
  }

  @Test
  fun `should deserialize TypedUuid directly`() {
    val uuid = UUID.randomUUID()
    val json = "\"$uuid\""

    val typedUuid = objectMapper.readValue(json, object : TypeReference<TypedUuid<PersonDto>>() {})

    assertThat(typedUuid.value).isEqualTo(uuid)
  }
}
