/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import com.ekino.oss.typedvalue.TypedUuid
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Abstract JPA AttributeConverter for TypedUuid fields.
 *
 * **Important limitation**: JPA specification does not allow AttributeConverters on `@Id` fields.
 * This converter works for regular entity fields only, not primary keys.
 *
 * For primary keys, use a raw UUID with a virtual TypedUuid property:
 * ```kotlin
 * @Entity
 * class Person(
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.UUID)
 *     private var _id: UUID? = null,
 *
 *     var name: String
 * ) {
 *     @get:Transient
 *     var id: TypedUuid<Person>?
 *         get() = _id?.let { it.toTypedUuid() }
 *         set(value) { _id = value?.value }
 * }
 * ```
 *
 * For non-ID fields, extend this class to create a converter:
 * ```kotlin
 * @Converter(autoApply = true)
 * class UserIdConverter : TypedUuidConverter<User>(User::class)
 *
 * @Entity
 * class Order(
 *     @Id val id: UUID,
 *
 *     // Converter works for regular fields
 *     @Convert(converter = UserIdConverter::class)
 *     var createdBy: TypedUuid<User>? = null
 * )
 * ```
 *
 * @param T The entity type this converter handles
 * @param entityType The KClass of the entity type
 */
@Converter
abstract class TypedUuidConverter<T : Any>(private val entityType: KClass<T>) :
  AttributeConverter<TypedUuid<T>, UUID> {

  override fun convertToDatabaseColumn(attribute: TypedUuid<T>?): UUID? = attribute?.value

  override fun convertToEntityAttribute(dbData: UUID?): TypedUuid<T>? =
    dbData?.let { TypedUuid.of(it, entityType) }
}
