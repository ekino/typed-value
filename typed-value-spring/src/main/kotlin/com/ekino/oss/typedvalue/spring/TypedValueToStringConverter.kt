/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.spring

import com.ekino.oss.typedvalue.TypedValue
import org.springframework.core.convert.converter.Converter

/**
 * Renders a [TypedValue] as its raw id when used as an HTTP interface path variable or request
 * param.
 */
class TypedValueToStringConverter : Converter<TypedValue<*, *>, String> {
  override fun convert(source: TypedValue<*, *>): String = source.value.toString()
}
