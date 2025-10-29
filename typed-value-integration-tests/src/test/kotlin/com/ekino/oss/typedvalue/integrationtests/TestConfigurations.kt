/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestConfigurations {

  companion object {
    private val postgres: PostgreSQLContainer =
      PostgreSQLContainer("postgres:18.1")
        .withDatabaseName("integration-tests-db")
        .withUsername("sa")
        .withPassword("sa")
        .withReuse(true)

    private val elasticsearch: ElasticsearchContainer =
      ElasticsearchContainer("elasticsearch:9.2.2")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("discovery.type", "single-node")
        .withReuse(true)
  }

  @Bean @ServiceConnection fun postgresqlContainer(): PostgreSQLContainer = postgres

  @Bean @ServiceConnection fun elasticsearchContainer(): ElasticsearchContainer = elasticsearch
}
