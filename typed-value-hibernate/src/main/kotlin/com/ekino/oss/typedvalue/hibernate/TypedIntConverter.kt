/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import com.ekino.oss.typedvalue.TypedInt
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.reflect.KClass

/**
 * Abstract JPA AttributeConverter for TypedInt fields.
 *
 * **Important limitation**: JPA specification does not allow AttributeConverters on `@Id` fields.
 * This converter works for regular entity fields only, not primary keys.
 *
 * For primary keys, use a raw Int with a virtual TypedInt property:
 * ```kotlin
 * @Entity
 * class Counter(
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private var _id: Int? = null,
 *
 *     var value: Int
 * ) {
 *     @get:Transient
 *     var id: TypedInt<Counter>?
 *         get() = _id?.let { it.toTypedInt() }
 *         set(value) { _id = value?.value }
 * }
 * ```
 *
 * For non-ID fields, extend this class to create a converter:
 * ```kotlin
 * @Converter(autoApply = true)
 * class CounterIdConverter : TypedIntConverter<Counter>(Counter::class)
 *
 * @Entity
 * class Metric(
 *     @Id val id: Int,
 *
 *     // Converter works for regular fields
 *     @Convert(converter = CounterIdConverter::class)
 *     var counterId: TypedInt<Counter>? = null
 * )
 * ```
 *
 * @param T The entity type this converter handles
 * @param entityType The KClass of the entity type
 */
@Converter
abstract class TypedIntConverter<T : Any>(private val entityType: KClass<T>) :
  AttributeConverter<TypedInt<T>, Int> {

  override fun convertToDatabaseColumn(attribute: TypedInt<T>?): Int? = attribute?.value

  override fun convertToEntityAttribute(dbData: Int?): TypedInt<T>? =
    dbData?.let { TypedInt.of(it, entityType) }
}
