/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import com.ekino.oss.typedvalue.TypedValue.Companion.rawIds
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * A type-safe identifier that associates a value with its entity type.
 *
 * This class provides compile-time type safety for entity identifiers, preventing accidental mixing
 * of IDs from different entity types. It wraps a comparable value (String, Long, UUID, etc.) along
 * with type information about the entity it identifies.
 *
 * ## Benefits
 * - **Compile-time safety**: Cannot accidentally mix User IDs with Product IDs
 * - **Type information**: Maintains entity type at runtime for reflection and serialization
 * - **Framework integration**: Seamlessly works with Jackson, Spring, QueryDSL, Elasticsearch
 * - **Flexible value types**: Supports any Comparable type (String, Long, UUID, custom types)
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Creating TypedValues
 * val userId: TypedValue<String, User> = "user-123".toTypedValueFor()
 * val productId: TypedValue<Long, Product> = TypedValue.typedValueFor(42L, Product::class)
 *
 * // Type safety prevents mixing
 * fun findUser(id: TypedValue<String, User>) { ... }
 * findUser(userId)      // ✓ Compiles
 * findUser(productId)   // ✗ Compilation error - type mismatch
 *
 * // Extract underlying value
 * val raw: String = userId.value  // "user-123"
 *
 * // Type checking
 * if (someId.isAboutType<User>()) { ... }
 * ```
 *
 * ## Equality
 * - Two TypedValues are equal if they have the same raw ID and the same type
 *
 * ## Comparison
 * - Primary: Compare by raw ID value
 * - Secondary: Compare by type hashCode (for stable ordering)
 *
 * @param VALUE The type of the identifier value, must be Comparable for ordering and range queries
 * @param T The entity type this identifier represents
 */
open class TypedValue<VALUE : Comparable<VALUE>, T : Any>(
  /** The underlying identifier value (e.g., "user-123", 42L, UUID) */
  open val value: VALUE,
  /** The runtime type of the entity this identifier represents */
  open val type: KClass<out T>,
) : Comparable<TypedValue<VALUE, T>> {

  companion object {
    /**
     * Creates a TypedValue from an identifier value and entity type.
     *
     * This is the primary factory method for creating TypedValues. For Kotlin code, consider using
     * the extension function [toTypedValueFor] for better type inference with reified generics.
     *
     * Example:
     * ```kotlin
     * TypedValue<String, User> userId = TypedValue.typedValueFor("user-123", User::class)
     * ```
     *
     * @param rawId The identifier value (String, Long, UUID, etc.)
     * @param type The entity KClass
     * @return A new TypedValue instance
     */
    fun <ID : Comparable<ID>, T : Any> typedValueFor(
      rawId: ID,
      type: KClass<T>,
    ): TypedValue<ID, T> = TypedValue(rawId, type)

    /**
     * Creates a TypedValue from a nullable identifier value, returning null if the value is null.
     *
     * Useful for mapping nullable database columns or optional API fields to TypedValues.
     *
     * Example:
     * ```kotlin
     * val userId: TypedValue<String, User>? = TypedValue.typedValueOrNullFor(
     *   nullableId,
     *   User::class
     * )
     * ```
     *
     * @param rawId The identifier value, nullable
     * @param type The entity KClass
     * @return A new TypedValue instance or null if rawId is null
     */
    fun <ID : Comparable<ID>, T : Any> typedValueOrNullFor(
      rawId: ID?,
      type: KClass<T>,
    ): TypedValue<ID, T>? = rawId?.let { TypedValue(it, type) }

    /**
     * Creates a builder function that constructs TypedValues for a specific entity type.
     *
     * Useful for mapping collections or streams of raw IDs to TypedValues.
     *
     * Example:
     * ```kotlin
     * val builder = TypedValue.typedValueBuilderFor<String, User>(User::class)
     * val userIds: List<TypedValue<String, User>> = rawIds.map(builder)
     * ```
     *
     * @param type The entity KClass
     * @return A function that takes an ID value and returns a TypedValue
     */
    fun <ID : Comparable<ID>, T : Any> typedValueBuilderFor(
      type: KClass<T>
    ): (ID) -> TypedValue<ID, T> = { TypedValue(it, type) }

    /**
     * Extracts the underlying identifier values from a collection of TypedValues.
     *
     * Useful when you need to pass raw IDs to APIs or queries that don't accept TypedValues.
     *
     * Example:
     * ```kotlin
     * List<TypedValue<String, User>> typedIds = ...
     * List<String> rawIds = TypedValue.rawIds(typedIds)
     * // Use rawIds with legacy API
     * ```
     *
     * @param typedValues Collection of TypedValues
     * @return List of underlying identifier values
     */
    @JvmStatic
    fun <ID : Comparable<ID>> rawIds(typedValues: Iterable<TypedValue<ID, *>>): List<ID> =
      typedValues.map { it.value }

    /**
     * Extension version of [rawIds] for Kotlin collection syntax.
     *
     * @see rawIds
     */
    @JvmStatic
    fun <ID : Comparable<ID>> Iterable<TypedValue<ID, *>>.toRawIds(): List<ID> = map { it.value }
  }

  override fun toString(): String = "${type.simpleName}($value)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TypedValue<*, *>) return false

    if (value != other.value) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int = value.hashCode()

  override fun compareTo(other: TypedValue<VALUE, T>): Int {
    // First compare by ID value
    val idComparison = value.compareTo(other.value)
    if (idComparison != 0) return idComparison

    // If IDs are equal, compare by type hashCode for stable ordering
    // (using hashCode instead of name to avoid JS reflection limitations)
    return type.hashCode().compareTo(other.type.hashCode())
  }
}

/**
 * Creates a TypedValue for a specific type using Kotlin reified generics.
 *
 * This is the idiomatic Kotlin way to create TypedValues, providing better type inference than the
 * factory methods.
 *
 * Example:
 * ```kotlin
 * val userId = "user-123".toTypedValueFor<String, User>()
 * val productId = 42L.toTypedValueFor<Long, Product>()
 * val orderId = UUID.randomUUID().toTypedValueFor<UUID, Order>()
 * ```
 *
 * @return A new TypedValue instance
 * @receiver The identifier value
 */
inline fun <ID : Comparable<ID>, reified T : Any> ID.toTypedValueFor(): TypedValue<ID, T> =
  TypedValue.typedValueFor(this, T::class)

/**
 * Checks if this TypedValue represents an identifier for the specified entity type.
 *
 * Example:
 * ```kotlin
 * val userId: TypedValue<String, User> = ...
 * if (userId.isAboutType<User>()) { ... }  // true
 * if (userId.isAboutType<Product>()) { ... }  // false
 * ```
 *
 * @return true if the TypedValue's type is exactly T
 */
inline fun <reified T : Any> TypedValue<*, *>.isAboutType(): Boolean = T::class == type

/**
 * Returns this TypedValue cast to the specified type if it represents that type, null otherwise.
 *
 * Useful for safely narrowing TypedValue types when working with heterogeneous collections or when
 * the exact type is determined at runtime.
 *
 * Example:
 * ```kotlin
 * val someId: TypedValue<String, *> = getIdFromSomewhere()
 *
 * val userId: TypedValue<String, User>? = someId.takeIfAboutType<String, User>()
 * if (userId != null) {
 *   // Now we know it's definitely a User ID
 *   processUser(userId)
 * }
 * ```
 *
 * @return This TypedValue cast to TypedValue<ID, T>, or null if type doesn't match
 */
@Suppress("UNCHECKED_CAST")
inline fun <ID : Comparable<ID>, reified T : Any> TypedValue<ID, *>.takeIfAboutType():
  TypedValue<ID, T>? {
  return if (T::class == type) {
    this as TypedValue<ID, T>
  } else {
    null
  }
}
