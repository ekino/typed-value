/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue

/**
 * Multiplatform marker interface that maps to [java.io.Serializable] on JVM.
 *
 * On JVM, this is a typealias to `java.io.Serializable`, enabling framework compatibility (e.g.,
 * Hypersistence Utils deep copy). On JS and Native, it is an empty marker interface with no effect.
 */
expect interface PlatformSerializable
