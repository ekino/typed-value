/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/**
 * JVM implementation backed by [java.lang.Class], which is [java.io.Serializable] (unlike
 * `KClassImpl`). [Class.kotlin] returns an equal `KClass` after deserialization, so
 * [TypedValue.equals] keeps working across a Java serialization round trip.
 */
internal actual class PlatformTypeRef<T : Any> actual constructor(kClass: KClass<out T>) :
  PlatformSerializable {

  private val javaClass: Class<out T> = kClass.java

  actual val kClass: KClass<out T>
    get() = javaClass.kotlin

  private companion object {
    private const val serialVersionUID: Long = 1L
  }
}
