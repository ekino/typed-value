/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import java.util.UUID
import kotlin.reflect.KClass

/**
 * Type-safe UUID identifier (JVM only).
 *
 * Ideal for distributed systems where globally unique identifiers are needed. Uses java.util.UUID
 * which is Comparable and well-established on JVM.
 *
 * ## Usage
 *
 * ```kotlin
 * val orderId: TypedUuid<Order> = UUID.randomUUID().toTypedUuid()
 * val explicitId: TypedUuid<Order> = TypedUuid.of(UUID.randomUUID(), Order::class)
 * val parsed: TypedUuid<Session> = UUID.fromString("...").toTypedUuid<Session>()
 * ```
 *
 * @param T The entity type this identifier represents
 */
open class TypedUuid<T : Any>(value: UUID, type: KClass<out T>) : TypedValue<UUID, T>(value, type) {
  companion object {
    /**
     * Creates a TypedUuid from a UUID value and entity type.
     *
     * @param value The UUID identifier value
     * @param type The entity KClass
     * @return A new TypedUuid instance
     */
    fun <T : Any> of(value: UUID, type: KClass<T>): TypedUuid<T> = TypedUuid(value, type)
  }
}

/**
 * Creates a TypedUuid for a specific type using Kotlin reified generics.
 *
 * Example:
 * ```kotlin
 * val orderId = UUID.randomUUID().toTypedUuid<Order>()
 * val sessionId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000").toTypedUuid<Session>()
 * ```
 *
 * @return A new TypedUuid instance
 * @receiver The UUID identifier value
 */
inline fun <reified T : Any> UUID.toTypedUuid(): TypedUuid<T> = TypedUuid.of(this, T::class)
