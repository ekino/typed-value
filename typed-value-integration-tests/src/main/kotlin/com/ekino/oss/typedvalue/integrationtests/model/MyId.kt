/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.TypedString
import kotlin.reflect.KClass

/**
 * Custom TypedValue type for testing custom registration in Jackson.
 *
 * This class extends TypedString and represents a custom ID type that should be registered with the
 * Jackson module to be properly reconstructed with the correct runtime type.
 *
 * Without registration, instances deserialized from JSON will be TypedString at runtime (type
 * erasure), not MyId. With registration, instances will be actual MyId instances.
 */
open class MyId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
