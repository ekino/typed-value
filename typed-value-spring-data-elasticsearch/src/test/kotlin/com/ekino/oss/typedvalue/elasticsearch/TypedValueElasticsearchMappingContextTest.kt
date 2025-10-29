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
}
