/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/**
 * Type-safe Int identifier.
 *
 * Useful for integer-based identifiers, such as auto-increment database IDs.
 *
 * ## Usage
 *
 * ```kotlin
 * val counterId: TypedInt<Counter> = 42.toTypedInt()
 * val explicitId: TypedInt<Counter> = TypedInt.of(42, Counter::class)
 * ```
 *
 * @param T The entity type this identifier represents
 */
open class TypedInt<T : Any>(value: Int, type: KClass<out T>) : TypedValue<Int, T>(value, type) {
  companion object {
    /**
     * Creates a TypedInt from an Int value and entity type.
     *
     * @param value The integer identifier value
     * @param type The entity KClass
     * @return A new TypedInt instance
     */
    fun <T : Any> of(value: Int, type: KClass<T>): TypedInt<T> = TypedInt(value, type)
  }
}

/**
 * Creates a TypedInt for a specific type using Kotlin reified generics.
 *
 * Example:
 * ```kotlin
 * val counterId = 42.toTypedInt<Counter>()
 * val sequenceId = 100.toTypedInt<Sequence>()
 * ```
 *
 * @return A new TypedInt instance
 * @receiver The integer identifier value
 */
inline fun <reified T : Any> Int.toTypedInt(): TypedInt<T> = TypedInt.of(this, T::class)
