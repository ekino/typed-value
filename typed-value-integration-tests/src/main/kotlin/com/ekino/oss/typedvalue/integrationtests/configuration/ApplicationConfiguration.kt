/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.configuration

import com.ekino.oss.typedvalue.elasticsearch.TypedValueElasticsearchMappingContext
import com.ekino.oss.typedvalue.hibernate.spring.TypedValueJpaRepositoryFactoryBean
import com.ekino.oss.typedvalue.integrationtests.model.TypedId
import com.ekino.oss.typedvalue.jackson.TypedValueModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
  basePackages = ["com.ekino.oss.typedvalue.integrationtests.repository"],
  repositoryFactoryBeanClass = TypedValueJpaRepositoryFactoryBean::class,
)
class ApplicationConfiguration {

  @Bean fun typedValueModule() = TypedValueModule()

  @Bean
  fun elasticsearchMappingContext(
    elasticsearchCustomConversions: ElasticsearchCustomConversions
  ): TypedValueElasticsearchMappingContext =
    TypedValueElasticsearchMappingContext().apply {
      setSimpleTypeHolder(elasticsearchCustomConversions.simpleTypeHolder)
      // Register custom TypedId for proper reconstruction from Elasticsearch
      registerCustomTypedValue(TypedId::class.java, String::class) { value, entityKClass ->
        TypedId(value, entityKClass)
      }
    }
}
