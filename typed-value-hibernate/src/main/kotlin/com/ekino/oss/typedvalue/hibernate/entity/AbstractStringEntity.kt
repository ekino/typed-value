/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import com.ekino.oss.typedvalue.TypedString
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Transient
import kotlin.reflect.KClass

/**
 * Abstract base class for JPA entities with auto-generated String (UUID) primary keys and proper
 * equals/hashCode implementation.
 *
 * The String ID is auto-generated using UUID strategy (RFC 4122 UUID as String).
 *
 * **Usage:**
 *
 * ```kotlin
 * @Entity
 * class Product : AbstractStringEntity<Product>(Product::class) {
 *     var name: String? = null
 * }
 *
 * // Create without ID - it will be auto-generated on save
 * val product = Product().apply { name = "Widget" }
 * productRepository.save(product)
 * ```
 *
 * @param I The entity type (used for type-safe ID)
 * @param entityClass The KClass of the entity type
 * @see HibernateEntityUtils for implementing equals/hashCode in custom entities
 */
@MappedSuperclass
abstract class AbstractStringEntity<I : Any>(@Transient val entityClass: KClass<I>) {

  @Id @GeneratedValue(strategy = GenerationType.UUID) private var _id: String? = null

  @get:Transient
  open var id: TypedString<I>?
    get() = _id?.let { TypedString.of(it, entityClass) }
    set(value) {
      _id = value?.value
    }

  override fun equals(other: Any?) = HibernateEntityUtils.entityEquals(this, other) { it.id }

  override fun hashCode() = HibernateEntityUtils.entityHashCode(this)
}
