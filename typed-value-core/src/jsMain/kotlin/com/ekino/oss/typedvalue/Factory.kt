/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
@file:OptIn(ExperimentalJsExport::class)

package com.ekino.oss.typedvalue

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Creates a TypedStringJs from a string value.
 *
 * Type safety is provided at compile-time via TypeScript's phantom type parameter. The TypeScript
 * declaration adds a generic type parameter that enforces type safety without runtime overhead.
 *
 * @param value The string identifier
 * @return A TypedStringJs instance
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface User {}
 * const userId = createTypedString<User>('user-123')
 * ```
 */
@JsExport
@JsName("createTypedString")
fun createTypedString(value: String): TypedStringJs = TypedStringJs(value)

/**
 * Creates a TypedIntJs from an int value.
 *
 * Type safety is provided at compile-time via TypeScript's phantom type parameter.
 *
 * @param value The integer identifier
 * @return A TypedIntJs instance
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface Product {}
 * const productId = createTypedInt<Product>(42)
 * ```
 */
@JsExport @JsName("createTypedInt") fun createTypedInt(value: Int): TypedIntJs = TypedIntJs(value)

/**
 * Creates a TypedLongJs from a number value.
 *
 * Type safety is provided at compile-time via TypeScript's phantom type parameter.
 *
 * Note: JavaScript numbers are always doubles (64-bit floating point). For IDs larger than 2^53,
 * consider using TypedStringJs instead to avoid precision loss.
 *
 * @param value The numeric identifier
 * @return A TypedLongJs instance
 *
 * Example usage in TypeScript:
 * ```typescript
 * interface Order {}
 * const orderId = createTypedLong<Order>(123456789)
 * ```
 */
@JsExport
@JsName("createTypedLong")
fun createTypedLong(value: Double): TypedLongJs = TypedLongJs(value)
