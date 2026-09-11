/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/**
 * Platform-specific, serializable holder for the entity [KClass] of a [TypedValue].
 *
 * On JVM, `KClass` implementations (`kotlin.reflect.jvm.internal.KClassImpl`) are not
 * `java.io.Serializable`, so the JVM actual stores the underlying `java.lang.Class` (which is
 * serializable) and rebuilds the `KClass` on access. On other platforms the `KClass` is held
 * directly.
 */
internal expect class PlatformTypeRef<T : Any>(kClass: KClass<out T>) : PlatformSerializable {
  val kClass: KClass<out T>
}
