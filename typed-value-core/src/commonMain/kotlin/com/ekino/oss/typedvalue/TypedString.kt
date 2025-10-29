/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/**
 * Type-safe String identifier.
 *
 * This convenience class provides a cleaner API for the most common use case of String-based
 * identifiers while maintaining full compatibility with the generic TypedValue interface.
 *
 * ## Usage
 *
 * ```kotlin
 * val userId: TypedString<User> = "user-123".toTypedString()
 * val productId: TypedString<Product> = TypedString.of("prod-456", Product::class)
 *
 * // Compatible with TypedValue
 * val genericId: TypedValue<String, User> = userId
 * ```
 *
 * @param T The entity type this identifier represents
 */
open class TypedString<T : Any>(value: String, type: KClass<out T>) :
  TypedValue<String, T>(value, type) {

  override val value: String = super.value

  companion object {
    /**
     * Creates a TypedString from a String value and entity type.
     *
     * @param value The string identifier value
     * @param type The entity KClass
     * @return A new TypedString instance
     */
    fun <T : Any> of(value: String, type: KClass<T>): TypedString<T> = TypedString(value, type)
  }
}

/**
 * Creates a TypedString for a specific type using Kotlin reified generics.
 *
 * This is the idiomatic Kotlin way to create TypedString instances.
 *
 * Example:
 * ```kotlin
 * val userId = "user-123".toTypedString<User>()
 * val productId = "prod-456".toTypedString<Product>()
 * ```
 *
 * @return A new TypedString instance
 * @receiver The string identifier value
 */
inline fun <reified T : Any> String.toTypedString(): TypedString<T> = TypedString.of(this, T::class)
