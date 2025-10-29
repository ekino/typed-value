/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import org.hibernate.proxy.HibernateProxy

/**
 * Utility functions for JPA/Hibernate entity implementations.
 *
 * Use these when implementing equals/hashCode in your own entities without extending the
 * Abstract*Entity classes.
 *
 * **Why these utilities exist:**
 *
 * Hibernate uses proxy objects for lazy loading. When you load an entity with a lazy association,
 * you get a proxy instead of the actual entity. This causes problems with equals/hashCode:
 * - `proxy.javaClass` returns the proxy class, not the entity class
 * - `Hibernate.getClass(proxy)` triggers lazy loading (bad for performance)
 *
 * These utilities use `HibernateProxy.extractLazyInitializer()` to get the real entity class
 * without triggering lazy loading.
 *
 * **Usage example:**
 *
 * ```kotlin
 * @Entity
 * class MyEntity(
 *     @Id val id: String,
 *     val name: String
 * ) {
 *     override fun equals(other: Any?) = HibernateEntityUtils.entityEquals(this, other) { it.id }
 *     override fun hashCode() = HibernateEntityUtils.entityHashCode(this)
 * }
 * ```
 *
 * @see <a
 *   href="https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate">
 *   The best way to implement equals, hashCode, and toString with JPA and Hibernate -
 *   Database-generated identifiers</a>
 * @see <a
 *   href="https://github.com/JetBrains/jpa-buddy-kotlin-entities/blob/main/src/main/kotlin/com/jpabuddy/kotlinentities/domain.kt">
 *   JPA Buddy Kotlin entities - uses getPersistentClass() for proper proxy handling without
 *   triggering lazy loading</a>
 */
object HibernateEntityUtils {

  /**
   * Get the effective class of an entity, resolving Hibernate proxies without triggering lazy
   * loading.
   *
   * Unlike `Hibernate.getClass()`, this method uses `getPersistentClass()` instead of
   * `getImplementation().getClass()`, avoiding proxy initialization.
   *
   * @param obj The entity object (may be a Hibernate proxy)
   * @return The actual entity class, not the proxy class
   */
  @JvmStatic
  fun getEffectiveClass(obj: Any): Class<*> {
    val lazyInitializer = HibernateProxy.extractLazyInitializer(obj)
    return (lazyInitializer?.persistentClass ?: obj.javaClass)
  }

  /**
   * Compute hashCode for an entity using its effective class.
   *
   * This ensures consistent hashCode for both proxies and real instances of the same entity. Using
   * the class hashCode (rather than the ID) ensures the hashCode remains stable even when the ID
   * changes from null to a generated value.
   *
   * @param obj The entity object
   * @return A stable hashCode based on the entity class
   */
  @JvmStatic fun entityHashCode(obj: Any): Int = getEffectiveClass(obj).hashCode()

  /**
   * Check equality between two entities based on their effective class and ID.
   *
   * Returns true if:
   * - Both references point to the same instance, OR
   * - Both have the same effective class AND both have non-null equal IDs
   *
   * This follows the JPA best practice of using database-generated IDs for equality.
   *
   * @param obj The first entity (this)
   * @param other The object to compare with
   * @param getId A function to extract the ID from an entity
   * @return true if the entities are equal
   */
  @JvmStatic
  fun <E : Any, ID> entityEquals(obj: E, other: Any?, getId: (E) -> ID?): Boolean {
    if (obj === other) return true
    if (other == null || getEffectiveClass(obj) != getEffectiveClass(other)) return false

    val id = getId(obj)
    @Suppress("UNCHECKED_CAST")
    return id != null && id == getId(other as E)
  }
}
