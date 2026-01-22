/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.TypedString
import kotlin.reflect.KClass

/**
 * Custom TypedValue type for testing custom registration in Elasticsearch.
 *
 * This class extends TypedString and represents a custom ID type that can be registered with the
 * Elasticsearch mapping context to be properly reconstructed from raw storage.
 */
open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
