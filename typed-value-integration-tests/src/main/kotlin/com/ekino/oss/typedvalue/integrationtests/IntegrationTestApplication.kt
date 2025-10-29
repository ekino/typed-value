/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class IntegrationTestApplication

fun main(args: Array<String>) {
  runApplication<IntegrationTestApplication>(*args)
}
