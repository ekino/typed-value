/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
@file:JvmName("TypedValues")

package com.ekino.oss.typedvalue

import java.util.UUID

/**
 * Creates a TypedString from a String value and Java Class.
 *
 * This is the recommended way to create TypedString instances from Java code.
 *
 * Example (Java):
 * ```java
 * TypedString<User> userId = TypedValues.typedString("user-123", User.class);
 * ```
 *
 * @param value The string identifier value
 * @param type The entity Java Class
 * @return A new TypedString instance
 */
fun <T : Any> typedString(value: String, type: Class<T>): TypedString<T> =
  TypedString(value, type.kotlin)

/**
 * Creates a TypedLong from a Long value and Java Class.
 *
 * This is the recommended way to create TypedLong instances from Java code.
 *
 * Example (Java):
 * ```java
 * TypedLong<Product> productId = TypedValues.typedLong(42L, Product.class);
 * ```
 *
 * @param value The long identifier value
 * @param type The entity Java Class
 * @return A new TypedLong instance
 */
fun <T : Any> typedLong(value: Long, type: Class<T>): TypedLong<T> = TypedLong(value, type.kotlin)

/**
 * Creates a TypedInt from an Int value and Java Class.
 *
 * This is the recommended way to create TypedInt instances from Java code.
 *
 * Example (Java):
 * ```java
 * TypedInt<Counter> counterId = TypedValues.typedInt(100, Counter.class);
 * ```
 *
 * @param value The integer identifier value
 * @param type The entity Java Class
 * @return A new TypedInt instance
 */
fun <T : Any> typedInt(value: Int, type: Class<T>): TypedInt<T> = TypedInt(value, type.kotlin)

/**
 * Creates a TypedUuid from a UUID value and Java Class.
 *
 * This is the recommended way to create TypedUuid instances from Java code.
 *
 * Example (Java):
 * ```java
 * TypedUuid<Order> orderId = TypedValues.typedUuid(UUID.randomUUID(), Order.class);
 * ```
 *
 * @param value The UUID identifier value
 * @param type The entity Java Class
 * @return A new TypedUuid instance
 */
fun <T : Any> typedUuid(value: UUID, type: Class<T>): TypedUuid<T> = TypedUuid(value, type.kotlin)

/**
 * Creates a generic TypedValue from any Comparable value and Java Class.
 *
 * Use this for custom ID types that are not covered by the convenience methods.
 *
 * Example (Java):
 * ```java
 * TypedValue<BigDecimal, Transaction> txId = TypedValues.typedValue(new BigDecimal("123.45"), Transaction.class);
 * ```
 *
 * @param value The identifier value (must be Comparable)
 * @param type The entity Java Class
 * @return A new TypedValue instance
 */
fun <ID : Comparable<ID>, T : Any> typedValue(value: ID, type: Class<T>): TypedValue<ID, T> =
  TypedValue(value, type.kotlin)
