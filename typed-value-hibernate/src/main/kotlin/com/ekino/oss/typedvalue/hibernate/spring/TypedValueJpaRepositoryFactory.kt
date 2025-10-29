/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.spring

import com.ekino.oss.typedvalue.TypedValue
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.data.repository.core.RepositoryInformation

/**
 * Custom JPA repository factory that creates [TypedValueJpaRepository] instances when the
 * repository ID type is a [TypedValue].
 *
 * This factory automatically detects repositories using TypedValue IDs (TypedUuid, TypedString,
 * TypedLong, TypedInt) and creates the appropriate repository implementation.
 */
class TypedValueJpaRepositoryFactory(em: EntityManager) : JpaRepositoryFactory(em) {

  @Suppress("UNCHECKED_CAST")
  override fun getTargetRepository(
    information: RepositoryInformation,
    entityManager: EntityManager,
  ): JpaRepositoryImplementation<*, *> {
    val entityInformation = getEntityInformation<Any, Any>(information.domainType as Class<Any>)

    return if (TypedValue::class.java.isAssignableFrom(information.idType)) {
      TypedValueJpaRepository<Any, TypedValue<Comparable<Any>, Any>>(
        entityInformation as JpaEntityInformation<Any, *>,
        entityManager,
      )
    } else {
      super.getTargetRepository(information, entityManager)
    }
  }
}
