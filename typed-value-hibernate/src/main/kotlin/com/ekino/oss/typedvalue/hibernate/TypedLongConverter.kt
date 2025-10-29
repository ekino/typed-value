/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import com.ekino.oss.typedvalue.TypedLong
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.reflect.KClass

/**
 * Abstract JPA AttributeConverter for TypedLong fields.
 *
 * **Important limitation**: JPA specification does not allow AttributeConverters on `@Id` fields.
 * This converter works for regular entity fields only, not primary keys.
 *
 * For primary keys, use a raw Long with a virtual TypedLong property:
 * ```kotlin
 * @Entity
 * class Product(
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private var _id: Long? = null,
 *
 *     var name: String
 * ) {
 *     @get:Transient
 *     var id: TypedLong<Product>?
 *         get() = _id?.let { it.toTypedLong() }
 *         set(value) { _id = value?.value }
 * }
 * ```
 *
 * For non-ID fields, extend this class to create a converter:
 * ```kotlin
 * @Converter(autoApply = true)
 * class ProductIdConverter : TypedLongConverter<Product>(Product::class)
 *
 * @Entity
 * class OrderLine(
 *     @Id val id: Long,
 *
 *     // Converter works for regular fields
 *     @Convert(converter = ProductIdConverter::class)
 *     var productId: TypedLong<Product>? = null
 * )
 * ```
 *
 * @param T The entity type this converter handles
 * @param entityType The KClass of the entity type
 */
@Converter
abstract class TypedLongConverter<T : Any>(private val entityType: KClass<T>) :
  AttributeConverter<TypedLong<T>, Long> {

  override fun convertToDatabaseColumn(attribute: TypedLong<T>?): Long? = attribute?.value

  override fun convertToEntityAttribute(dbData: Long?): TypedLong<T>? =
    dbData?.let { TypedLong.of(it, entityType) }
}
