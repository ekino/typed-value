/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

import kotlin.reflect.KClass

/** JS implementation: no Java serialization exists, so the [KClass] is held directly. */
internal actual class PlatformTypeRef<T : Any>
actual constructor(actual val kClass: KClass<out T>) : PlatformSerializable
