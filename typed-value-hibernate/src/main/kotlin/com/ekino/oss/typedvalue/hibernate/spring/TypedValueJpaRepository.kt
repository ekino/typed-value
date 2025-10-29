/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.spring

import com.ekino.oss.typedvalue.TypedValue
import jakarta.persistence.EntityManager
import java.util.Optional
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository

/**
 * Custom JPA repository implementation that supports TypedValue as ID type.
 *
 * This repository automatically extracts the raw value from TypedValue IDs when performing database
 * operations, allowing you to use typed IDs in your repository interfaces.
 *
 * @param T The entity type
 * @param ID The TypedValue ID type (e.g., TypedUuid<Person>, TypedString<User>)
 */
@Suppress("UNCHECKED_CAST")
open class TypedValueJpaRepository<T : Any, ID : TypedValue<*, *>>(
  entityInformation: JpaEntityInformation<T, *>,
  entityManager: EntityManager,
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

  private val delegate =
    SimpleJpaRepository<T, Any>(entityInformation as JpaEntityInformation<T, Any>, entityManager)

  // Single ID operations
  override fun findById(id: ID): Optional<T> = delegate.findById(id.value)

  override fun existsById(id: ID): Boolean = delegate.existsById(id.value)

  override fun deleteById(id: ID) = delegate.deleteById(id.value)

  override fun getReferenceById(id: ID): T = delegate.getReferenceById(id.value)

  // Multiple IDs operations
  override fun findAllById(ids: Iterable<ID>): List<T> = delegate.findAllById(ids.map { it.value })

  override fun deleteAllById(ids: Iterable<ID>) = delegate.deleteAllById(ids.map { it.value })

  override fun deleteAllByIdInBatch(ids: Iterable<ID>) =
    delegate.deleteAllByIdInBatch(ids.map { it.value })
}
