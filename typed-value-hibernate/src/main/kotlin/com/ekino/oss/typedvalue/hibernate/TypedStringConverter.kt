/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate

import com.ekino.oss.typedvalue.TypedString
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.reflect.KClass

/**
 * Abstract JPA AttributeConverter for TypedString fields.
 *
 * **Important limitation**: JPA specification does not allow AttributeConverters on `@Id` fields.
 * This converter works for regular entity fields only, not primary keys.
 *
 * For primary keys, use a raw String with a virtual TypedString property:
 * ```kotlin
 * @Entity
 * class User(
 *     @Id
 *     private var _id: String? = null,
 *
 *     var name: String
 * ) {
 *     @get:Transient
 *     var id: TypedString<User>?
 *         get() = _id?.let { it.toTypedString() }
 *         set(value) { _id = value?.value }
 * }
 * ```
 *
 * For non-ID fields, extend this class to create a converter:
 * ```kotlin
 * @Converter(autoApply = true)
 * class UserIdConverter : TypedStringConverter<User>(User::class)
 *
 * @Entity
 * class Order(
 *     @Id val id: String,
 *
 *     // Converter works for regular fields
 *     @Convert(converter = UserIdConverter::class)
 *     var createdBy: TypedString<User>? = null
 * )
 * ```
 *
 * @param T The entity type this converter handles
 * @param entityType The KClass of the entity type
 */
@Converter
abstract class TypedStringConverter<T : Any>(private val entityType: KClass<T>) :
  AttributeConverter<TypedString<T>, String> {

  override fun convertToDatabaseColumn(attribute: TypedString<T>?): String? = attribute?.value

  override fun convertToEntityAttribute(dbData: String?): TypedString<T>? =
    dbData?.let { TypedString.of(it, entityType) }
}
