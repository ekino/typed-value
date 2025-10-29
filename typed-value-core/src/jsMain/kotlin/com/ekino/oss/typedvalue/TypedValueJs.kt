/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
@file:OptIn(ExperimentalJsExport::class)

package com.ekino.oss.typedvalue

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * TypeScript-friendly TypedString implementation with compile-time type safety via phantom types.
 *
 * Type safety is enforced at compile-time through TypeScript's type system using phantom type
 * parameters in the generated .d.ts declarations. At runtime, this class holds only the value.
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface User {}
 * interface Product {}
 *
 * const userId = createTypedString<User>('user-123')
 * const productId = createTypedString<Product>('prod-456')
 *
 * function getUser(id: TypedString<User>) { ... }
 * getUser(userId)      // OK
 * getUser(productId)   // Compile ERROR
 * ```
 */
@JsExport
@JsName("TypedString")
class TypedStringJs(val value: String) {

  override fun toString(): String = value

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is TypedStringJs) return false
    return value == other.value
  }

  override fun hashCode(): Int = value.hashCode()

  /** Compares this TypedStringJs to another by value. */
  fun compareTo(other: TypedStringJs): Int = value.compareTo(other.value)
}

/**
 * TypeScript-friendly TypedInt implementation with compile-time type safety via phantom types.
 *
 * Type safety is enforced at compile-time through TypeScript's type system using phantom type
 * parameters in the generated .d.ts declarations. At runtime, this class holds only the value.
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface Product {}
 * const productId = createTypedInt<Product>(42)
 * ```
 */
@JsExport
@JsName("TypedInt")
class TypedIntJs(val value: Int) {

  override fun toString(): String = value.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is TypedIntJs) return false
    return value == other.value
  }

  override fun hashCode(): Int = value.hashCode()

  /** Compares this TypedIntJs to another by value. */
  fun compareTo(other: TypedIntJs): Int = value.compareTo(other.value)
}

/**
 * TypeScript-friendly TypedLong implementation with compile-time type safety via phantom types.
 *
 * Type safety is enforced at compile-time through TypeScript's type system using phantom type
 * parameters in the generated .d.ts declarations. At runtime, this class holds only the value.
 *
 * Note: JavaScript numbers have limited precision for integers > 2^53. For very large IDs, consider
 * using TypedString instead.
 *
 * The value is stored as a Double (JavaScript number) to ensure JS compatibility.
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface Order {}
 * const orderId = createTypedLong<Order>(123456789)
 * ```
 */
@JsExport
@JsName("TypedLong")
class TypedLongJs(val value: Double) {

  override fun toString(): String = value.toLong().toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is TypedLongJs) return false
    return value == other.value
  }

  override fun hashCode(): Int = value.hashCode()

  /** Compares this TypedLongJs to another by value. */
  fun compareTo(other: TypedLongJs): Int = value.compareTo(other.value)
}
