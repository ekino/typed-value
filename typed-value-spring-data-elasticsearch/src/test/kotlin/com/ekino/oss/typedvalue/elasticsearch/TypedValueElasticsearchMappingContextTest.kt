/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.elasticsearch

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.cause
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
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.mapping.MappingException

class TypedValueElasticsearchMappingContextTest {

  class User

  class Product

  class Order

  @Test
  fun `should create mapping context`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    assertThat(mappingContext).isNotNull()
  }

  @Test
  fun `should handle document with single TypedValue field`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(SimpleDocument::class.java))

    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(SimpleDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")

    assertThat(idProperty.hasPropertyValueConverter()).isEqualTo(true)
  }

  @Test
  fun `should handle document with List of TypedValues`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(DocumentWithList::class.java))

    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(DocumentWithList::class.java)
    val idsProperty = entity.getRequiredPersistentProperty("userIds")

    assertThat(idsProperty.hasPropertyValueConverter()).isEqualTo(true)
  }

  @Test
  fun `should write TypedValue to raw ID value`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(SimpleDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(SimpleDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val typedId = TypedValue.typedValueFor("user-123", User::class)
    val written = converter.write(typedId)

    assertThat(written).isEqualTo("user-123")
  }

  @Test
  fun `should read raw ID value to TypedValue`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(SimpleDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(SimpleDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val read = converter.read("user-456") as TypedValue<*, *>

    assertThat(read.value).isEqualTo("user-456")
    assertThat(read.type).isEqualTo(User::class)
  }

  @Test
  fun `should handle Long ID types`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(LongIdDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(LongIdDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("productId")
    val converter = idProperty.propertyValueConverter!!

    val typedId = TypedValue.typedValueFor(42L, Product::class)
    val written = converter.write(typedId)
    assertThat(written).isEqualTo(42L)

    val read = converter.read(42L) as TypedValue<*, *>
    assertThat(read.value).isEqualTo(42L)
    assertThat(read.type).isEqualTo(Product::class)
  }

  @Test
  fun `should handle UUID ID types`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(UuidIdDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(UuidIdDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("orderId")
    val converter = idProperty.propertyValueConverter!!

    val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val typedId = TypedValue.typedValueFor(uuid, Order::class)

    // Write should convert UUID to String for Elasticsearch
    val written = converter.write(typedId)
    assertThat(written).isEqualTo("550e8400-e29b-41d4-a716-446655440000")

    // Read should convert String back to UUID
    val read = converter.read("550e8400-e29b-41d4-a716-446655440000") as TypedValue<*, *>
    assertThat(read.value).isEqualTo(uuid)
    assertThat(read.type).isEqualTo(Order::class)
  }

  @Test
  fun `should fail when TypedValue type parameter is undefined`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(UndefinedTypeDocument::class.java))

    assertFailure { mappingContext.initialize() }
      .isInstanceOf<MappingException>()
      .cause()
      .isNotNull()
      .isInstanceOf<UnsupportedOperationException>()
      .messageContains("Actual type of TypedValue could not be resolved")
  }

  @Test
  fun `should fail when document contains Array of TypedValue`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(ArrayDocument::class.java))

    assertFailure { mappingContext.initialize() }
      .isInstanceOf<MappingException>()
      .cause()
      .isNotNull()
      .isInstanceOf<UnsupportedOperationException>()
      .messageContains("Arrays of TypedValue are not supported")
  }

  @Test
  fun `should fail when document contains Set of TypedValue`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(SetDocument::class.java))

    assertFailure { mappingContext.initialize() }
      .isInstanceOf<MappingException>()
      .cause()
      .isNotNull()
      .isInstanceOf<UnsupportedOperationException>()
      .messageContains("Sets of TypedValue are not supported")
  }

  // Convenience type tests

  @Test
  fun `should handle TypedString convenience type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(TypedStringDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(TypedStringDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val typedId = TypedString.of("user-123", User::class)
    val written = converter.write(typedId)
    assertThat(written).isEqualTo("user-123")

    val read = converter.read("user-456")
    assertThat(read).isInstanceOf<TypedString<*>>()
    val typedRead = read as TypedString<*>
    assertThat(typedRead.value).isEqualTo("user-456")
    assertThat(typedRead.type).isEqualTo(User::class)
  }

  @Test
  fun `should handle TypedLong convenience type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(TypedLongDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(TypedLongDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val typedId = TypedLong.of(42L, Product::class)
    val written = converter.write(typedId)
    assertThat(written).isEqualTo(42L)

    val read = converter.read(42L)
    assertThat(read).isInstanceOf<TypedLong<*>>()
    val typedRead = read as TypedLong<*>
    assertThat(typedRead.value).isEqualTo(42L)
    assertThat(typedRead.type).isEqualTo(Product::class)
  }

  @Test
  fun `should handle TypedInt convenience type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(TypedIntDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(TypedIntDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val typedId = TypedInt.of(42, Order::class)
    val written = converter.write(typedId)
    assertThat(written).isEqualTo(42)

    val read = converter.read(42)
    assertThat(read).isInstanceOf<TypedInt<*>>()
    val typedRead = read as TypedInt<*>
    assertThat(typedRead.value).isEqualTo(42)
    assertThat(typedRead.type).isEqualTo(Order::class)
  }

  @Test
  fun `should handle TypedUuid convenience type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(TypedUuidDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(TypedUuidDocument::class.java)
    val idProperty = entity.getRequiredPersistentProperty("id")
    val converter = idProperty.propertyValueConverter!!

    val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val typedId = TypedUuid.of(uuid, User::class)

    // Write should convert UUID to String for Elasticsearch
    val written = converter.write(typedId)
    assertThat(written).isEqualTo("550e8400-e29b-41d4-a716-446655440000")

    // Read should convert String back to UUID and return TypedUuid
    val read = converter.read("550e8400-e29b-41d4-a716-446655440000")
    assertThat(read).isInstanceOf<TypedUuid<*>>()
    val typedRead = read as TypedUuid<*>
    assertThat(typedRead.value).isEqualTo(uuid)
    assertThat(typedRead.type).isEqualTo(User::class)
  }

  @Test
  fun `should handle List of TypedUuid convenience type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(TypedUuidListDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(TypedUuidListDocument::class.java)
    val idsProperty = entity.getRequiredPersistentProperty("userIds")

    assertThat(idsProperty.hasPropertyValueConverter()).isEqualTo(true)
  }

  // Custom TypedValue registration tests

  @Test
  fun `should register and reconstruct custom TypedValue type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register custom type before initialization
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
      value,
      entityKClass ->
      TypedId(value, entityKClass)
    }

    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    // Write should convert to raw String
    val typedId = TypedId("user-123", User::class)
    val written = converter.write(typedId)
    assertThat(written).isEqualTo("user-123")

    // Read should return TypedId (not TypedString)
    val read = converter.read("user-456")
    assertThat(read).isInstanceOf<TypedId<*>>()
    val typedRead = read as TypedId<*>
    assertThat(typedRead.value).isEqualTo("user-456")
    assertThat(typedRead.type).isEqualTo(User::class)
  }

  @Test
  fun `should register and handle List of custom TypedValue type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register custom type
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
      value,
      entityKClass ->
      TypedId(value, entityKClass)
    }

    mappingContext.setInitialEntitySet(setOf(CustomTypeListDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeListDocument::class.java)
    val userIdsProperty = entity.getRequiredPersistentProperty("userIds")

    assertThat(userIdsProperty.hasPropertyValueConverter()).isEqualTo(true)

    val converter = userIdsProperty.propertyValueConverter!!
    val read = converter.read("user-789")
    assertThat(read).isInstanceOf<TypedId<*>>()
  }

  @Test
  fun `should fall back to TypedString for unregistered custom type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    // Don't register TypedId
    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    // Read should return TypedString (fallback behavior)
    val read = converter.read("user-456")
    assertThat(read).isInstanceOf<TypedString<*>>()
    val typedRead = read as TypedString<*>
    assertThat(typedRead.value).isEqualTo("user-456")
    assertThat(typedRead.type).isEqualTo(User::class)
  }

  @Test
  fun `should fail when registering duplicate type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register once
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
      value,
      entityKClass ->
      TypedId(value, entityKClass)
    }

    // Try to register again
    assertFailure {
        mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
          value,
          entityKClass ->
          TypedId(value, entityKClass)
        }
      }
      .isInstanceOf<IllegalArgumentException>()
      .messageContains("is already registered")
  }

  @Test
  fun `should fail when registering after initialization`() {
    val mappingContext = TypedValueElasticsearchMappingContext()
    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    // Try to register after initialization
    assertFailure {
        mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
          value,
          entityKClass ->
          TypedId(value, entityKClass)
        }
      }
      .isInstanceOf<IllegalStateException>()
      .messageContains("after mapping context initialization")
  }

  @Test
  fun `should wrap constructor exceptions with context`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register with constructor that throws
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) { _, _ ->
      @Suppress("TooGenericExceptionThrown") throw RuntimeException("Constructor failed")
    }

    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    // Read should wrap exception with context
    val exception = assertFailure { converter.read("user-456") }
    exception.isInstanceOf<IllegalStateException>()
    exception.messageContains("Failed to construct TypedValue")
    exception.messageContains("TypedId")
    exception.messageContains("User")
    exception.messageContains("user-456")
    exception.cause().isNotNull().isInstanceOf<RuntimeException>()
    exception.cause().isNotNull().messageContains("Constructor failed")
  }

  @Test
  fun `should handle multiple custom types`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register TypedId
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
      value,
      entityKClass ->
      TypedId(value, entityKClass)
    }

    // Register TypedCode
    mappingContext.registerCustomTypedValue(TypedCode::class.java, String::class) {
      value,
      entityKClass ->
      TypedCode(value, entityKClass)
    }

    mappingContext.setInitialEntitySet(setOf(MultipleCustomTypesDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(MultipleCustomTypesDocument::class.java)

    // Test TypedId field
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val userIdConverter = userIdProperty.propertyValueConverter!!
    val readUserId = userIdConverter.read("user-123")
    assertThat(readUserId).isInstanceOf<TypedId<*>>()
    assertThat((readUserId as TypedId<*>).value).isEqualTo("user-123")

    // Test TypedCode field
    val productCodeProperty = entity.getRequiredPersistentProperty("productCode")
    val productCodeConverter = productCodeProperty.propertyValueConverter!!
    val readProductCode = productCodeConverter.read("code-456")
    assertThat(readProductCode).isInstanceOf<TypedCode<*>>()
    assertThat((readProductCode as TypedCode<*>).value).isEqualTo("code-456")
  }

  @Test
  fun `should work with reified API`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Use reified version with type-safe API
    mappingContext.registerCustomTypedValue<TypedId<*>, String> { value, entityKClass ->
      TypedId(value, entityKClass)
    }

    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    val read = converter.read("user-789")
    assertThat(read).isInstanceOf<TypedId<*>>()
    assertThat((read as TypedId<*>).value).isEqualTo("user-789")
  }

  @Test
  fun `should work with Java-friendly API`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Use Java-friendly version with TypedValueConstructor interface
    mappingContext.registerCustomTypedValue(
      TypedId::class.java,
      String::class.java,
      TypedValueElasticsearchMappingContext.TypedValueConstructor { value, entityClass ->
        TypedId(value, entityClass.kotlin)
      },
    )

    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    val read = converter.read("user-999")
    assertThat(read).isInstanceOf<TypedId<*>>()
    assertThat((read as TypedId<*>).value).isEqualTo("user-999")
  }

  @Test
  fun `should validate value type matches registered VALUE type`() {
    val mappingContext = TypedValueElasticsearchMappingContext()

    // Register TypedId with String value type
    mappingContext.registerCustomTypedValue(TypedId::class.java, String::class) {
      value,
      entityKClass ->
      TypedId(value, entityKClass)
    }

    mappingContext.setInitialEntitySet(setOf(CustomTypeDocument::class.java))
    mappingContext.initialize()

    val entity = mappingContext.getRequiredPersistentEntity(CustomTypeDocument::class.java)
    val userIdProperty = entity.getRequiredPersistentProperty("userId")
    val converter = userIdProperty.propertyValueConverter!!

    // Try to read with wrong type (Long instead of String)
    val exception = assertFailure { converter.read(12345L) }
    exception.isInstanceOf<IllegalStateException>()
    exception.messageContains("Type mismatch")
    exception.messageContains("TypedId")
    exception.messageContains("expected raw value type String")
    exception.messageContains("from Elasticsearch but got Long")
  }

  // Custom TypedValue classes for testing
  open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

  open class TypedCode<T : Any>(code: String, type: KClass<T>) : TypedString<T>(code, type)

  // Test document classes
  @Document(indexName = "simple")
  private data class SimpleDocument(val id: TypedValue<String, User>)

  @Document(indexName = "with-list")
  private data class DocumentWithList(val userIds: List<TypedValue<String, User>>)

  @Document(indexName = "long-id")
  private data class LongIdDocument(val productId: TypedValue<Long, Product>)

  @Document(indexName = "uuid-id")
  private data class UuidIdDocument(val orderId: TypedValue<UUID, Order>)

  @Document(indexName = "undefined")
  private data class UndefinedTypeDocument(val id: TypedValue<String, *>)

  @Document(indexName = "array")
  private data class ArrayDocument(val ids: Array<TypedValue<String, User>>)

  @Document(indexName = "set")
  private data class SetDocument(val ids: Set<TypedValue<String, User>>)

  // Convenience type test document classes
  @Document(indexName = "typed-string")
  private data class TypedStringDocument(val id: TypedString<User>)

  @Document(indexName = "typed-long")
  private data class TypedLongDocument(val id: TypedLong<Product>)

  @Document(indexName = "typed-int") private data class TypedIntDocument(val id: TypedInt<Order>)

  @Document(indexName = "typed-uuid") private data class TypedUuidDocument(val id: TypedUuid<User>)

  @Document(indexName = "typed-uuid-list")
  private data class TypedUuidListDocument(val userIds: List<TypedUuid<User>>)

  // Custom type test document classes
  @Document(indexName = "custom-type")
  private data class CustomTypeDocument(val userId: TypedId<User>)

  @Document(indexName = "custom-type-list")
  private data class CustomTypeListDocument(val userIds: List<TypedId<User>>)

  @Document(indexName = "multiple-custom-types")
  private data class MultipleCustomTypesDocument(
    val userId: TypedId<User>,
    val productCode: TypedCode<Product>,
  )
}
