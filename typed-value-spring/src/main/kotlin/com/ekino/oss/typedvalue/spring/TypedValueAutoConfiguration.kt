/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.spring

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring Boot Auto-configuration for TypedValue support.
 *
 * This configuration automatically registers the StringToTypedValueConverter when the application
 * starts, enabling automatic conversion of path variables and request parameters to TypedValue
 * instances.
 *
 * To use, simply add this module to your classpath. Spring Boot will automatically detect and apply
 * this configuration.
 *
 * Manual configuration (if auto-configuration is disabled):
 * ```kotlin
 * @Configuration
 * class MyConfig : WebMvcConfigurer {
 *   override fun addFormatters(registry: FormatterRegistry) {
 *     registry.addConverter(StringToTypedValueConverter())
 *   }
 * }
 * ```
 */
@AutoConfiguration
open class TypedValueAutoConfiguration : WebMvcConfigurer {

  @Bean
  open fun stringToTypedValueConverter(): StringToTypedValueConverter =
    StringToTypedValueConverter()

  override fun addFormatters(registry: FormatterRegistry) {
    registry.addConverter(stringToTypedValueConverter())
  }
}
