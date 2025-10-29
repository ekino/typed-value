/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/**
 * Type-safe Long identifier.
 *
 * The most common choice for database-generated IDs (auto-increment sequences).
 *
 * ## Usage
 *
 * ```kotlin
 * val productId: TypedLong<Product> = 42L.toTypedLong()
 * val explicitId: TypedLong<Product> = TypedLong.of(42L, Product::class)
 * ```
 *
 * @param T The entity type this identifier represents
 */
open class TypedLong<T : Any>(value: Long, type: KClass<out T>) : TypedValue<Long, T>(value, type) {
  companion object {
    /**
     * Creates a TypedLong from a Long value and entity type.
     *
     * @param value The long identifier value
     * @param type The entity KClass
     * @return A new TypedLong instance
     */
    fun <T : Any> of(value: Long, type: KClass<T>): TypedLong<T> = TypedLong(value, type)
  }
}

/**
 * Creates a TypedLong for a specific type using Kotlin reified generics.
 *
 * Example:
 * ```kotlin
 * val productId = 42L.toTypedLong<Product>()
 * val userId = 12345L.toTypedLong<User>()
 * ```
 *
 * @return A new TypedLong instance
 * @receiver The long identifier value
 */
inline fun <reified T : Any> Long.toTypedLong(): TypedLong<T> = TypedLong.of(this, T::class)
