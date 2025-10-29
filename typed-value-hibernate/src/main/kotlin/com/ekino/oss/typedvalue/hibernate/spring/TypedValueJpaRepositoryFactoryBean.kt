/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.spring

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactorySupport

/**
 * Factory bean that creates [TypedValueJpaRepositoryFactory] instances.
 *
 * Use this factory bean with `@EnableJpaRepositories` to enable TypedValue ID support in all your
 * repositories:
 * ```kotlin
 * @Configuration
 * @EnableJpaRepositories(
 *     repositoryFactoryBeanClass = TypedValueJpaRepositoryFactoryBean::class
 * )
 * class JpaConfig
 * ```
 *
 * Then you can define repositories with TypedValue IDs:
 * ```kotlin
 * // Entity with virtual typed ID
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
 *
 * // Repository with typed ID
 * interface PersonRepository : JpaRepository<Person, TypedUuid<Person>>
 *
 * // Usage
 * val person = personRepository.findById(typedId)  // Works with TypedUuid<Person>
 * ```
 *
 * @param R The repository type
 * @param T The entity type
 * @param ID The ID type
 */
class TypedValueJpaRepositoryFactoryBean<R : Repository<T, ID>, T : Any, ID : Any>(
  repositoryInterface: Class<out R>
) : JpaRepositoryFactoryBean<R, T, ID>(repositoryInterface) {

  override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
    return TypedValueJpaRepositoryFactory(entityManager)
  }
}
