/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.controller

import com.ekino.oss.typedvalue.integrationtests.AbstractIntegrationTest
import java.util.UUID
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Integration tests for the `typed-value-spring` module autoconfiguration.
 *
 * These tests verify that [com.ekino.oss.typedvalue.spring.TypedValueAutoConfiguration] correctly
 * registers converters that automatically convert String path variables and request parameters to
 * TypedValue instances in Spring MVC controllers.
 *
 * Tests cover all supported typed value types:
 * - [com.ekino.oss.typedvalue.TypedUuid] (UUID-based IDs)
 * - [com.ekino.oss.typedvalue.TypedString] (String-based IDs)
 * - [com.ekino.oss.typedvalue.TypedInt] (Int-based IDs)
 * - [com.ekino.oss.typedvalue.TypedLong] (Long-based IDs)
 * - Generic [com.ekino.oss.typedvalue.TypedValue] with various ID types
 */
@AutoConfigureMockMvc
class PersonControllerTest : AbstractIntegrationTest() {

  @Autowired private lateinit var mockMvc: MockMvc

  @Nested
  inner class TypedUuidConversion {

    @Test
    fun `should convert String path variable to TypedUuid`() {
      val uuid = UUID.randomUUID()

      mockMvc.get("/api/persons/uuid/$uuid").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(uuid.toString()) }
        jsonPath("$.name") { value("Person $uuid") }
      }
    }

    @Test
    fun `should convert String request param to TypedUuid`() {
      val uuid = UUID.randomUUID()

      mockMvc
        .get("/api/persons/uuid") { param("id", uuid.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(uuid.toString()) }
          jsonPath("$.name") { value("Person $uuid") }
        }
    }
  }

  @Nested
  inner class TypedStringConversion {

    @Test
    fun `should convert String path variable to TypedString`() {
      val stringId = "person-abc-123"

      mockMvc.get("/api/persons/string/$stringId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(stringId) }
        jsonPath("$.name") { value("Person $stringId") }
      }
    }

    @Test
    fun `should convert String request param to TypedString`() {
      val stringId = "person-xyz-456"

      mockMvc
        .get("/api/persons/string") { param("id", stringId) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(stringId) }
          jsonPath("$.name") { value("Person $stringId") }
        }
    }
  }

  @Nested
  inner class TypedIntConversion {

    @Test
    fun `should convert String path variable to TypedInt`() {
      val intId = 42

      mockMvc.get("/api/persons/int/$intId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(intId.toString()) }
        jsonPath("$.name") { value("Person $intId") }
      }
    }

    @Test
    fun `should convert String request param to TypedInt`() {
      val intId = 99

      mockMvc
        .get("/api/persons/int") { param("id", intId.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(intId.toString()) }
          jsonPath("$.name") { value("Person $intId") }
        }
    }
  }

  @Nested
  inner class TypedLongConversion {

    @Test
    fun `should convert String path variable to TypedLong`() {
      val longId = 9876543210L

      mockMvc.get("/api/persons/long/$longId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(longId.toString()) }
        jsonPath("$.name") { value("Person $longId") }
      }
    }

    @Test
    fun `should convert String request param to TypedLong`() {
      val longId = 1234567890L

      mockMvc
        .get("/api/persons/long") { param("id", longId.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(longId.toString()) }
          jsonPath("$.name") { value("Person $longId") }
        }
    }
  }

  @Nested
  inner class GenericTypedValueWithStringId {

    @Test
    fun `should convert String path variable to TypedValue with String ID`() {
      val stringId = "generic-string-id"

      mockMvc.get("/api/persons/generic-string/$stringId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(stringId) }
        jsonPath("$.name") { value("Person $stringId") }
      }
    }

    @Test
    fun `should convert String request param to TypedValue with String ID`() {
      val stringId = "generic-param-id"

      mockMvc
        .get("/api/persons/generic-string") { param("id", stringId) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(stringId) }
          jsonPath("$.name") { value("Person $stringId") }
        }
    }
  }

  @Nested
  inner class GenericTypedValueWithIntId {

    @Test
    fun `should convert String path variable to TypedValue with Int ID`() {
      val intId = 12345

      mockMvc.get("/api/persons/generic-int/$intId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(intId.toString()) }
        jsonPath("$.name") { value("Person $intId") }
      }
    }

    @Test
    fun `should convert String request param to TypedValue with Int ID`() {
      val intId = 67890

      mockMvc
        .get("/api/persons/generic-int") { param("id", intId.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(intId.toString()) }
          jsonPath("$.name") { value("Person $intId") }
        }
    }
  }

  @Nested
  inner class GenericTypedValueWithLongId {

    @Test
    fun `should convert String path variable to TypedValue with Long ID`() {
      val longId = 1122334455L

      mockMvc.get("/api/persons/generic-long/$longId").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(longId.toString()) }
        jsonPath("$.name") { value("Person $longId") }
      }
    }

    @Test
    fun `should convert String request param to TypedValue with Long ID`() {
      val longId = 5544332211L

      mockMvc
        .get("/api/persons/generic-long") { param("id", longId.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(longId.toString()) }
          jsonPath("$.name") { value("Person $longId") }
        }
    }
  }

  @Nested
  inner class GenericTypedValueWithUuidId {

    @Test
    fun `should convert String path variable to TypedValue with UUID ID`() {
      val uuid = UUID.randomUUID()

      mockMvc.get("/api/persons/generic-uuid/$uuid").andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(uuid.toString()) }
        jsonPath("$.name") { value("Person $uuid") }
      }
    }

    @Test
    fun `should convert String request param to TypedValue with UUID ID`() {
      val uuid = UUID.randomUUID()

      mockMvc
        .get("/api/persons/generic-uuid") { param("id", uuid.toString()) }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.id") { value(uuid.toString()) }
          jsonPath("$.name") { value("Person $uuid") }
        }
    }
  }
}
