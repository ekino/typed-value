/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.elasticsearch

import com.ekino.oss.typedvalue.TypedValue
import kotlin.jvm.java
import kotlin.reflect.KClass
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQuery
import org.springframework.stereotype.Service

@Service
class ElasticsearchService(val elasticsearchOperations: ElasticsearchOperations) {

  private fun retrieveIndexCoordinates(clazz: Class<*>): IndexCoordinates {
    return elasticsearchOperations.getIndexCoordinatesFor(clazz)
  }

  fun <D : Any> index(document: D, id: String, clazz: KClass<out D>) {
    index(
      IndexQuery().apply {
        setId(id)
        setObject(document)
      },
      retrieveIndexCoordinates(clazz.java),
    )
  }

  private fun index(query: IndexQuery, indexCoordinates: IndexCoordinates) {
    elasticsearchOperations.index(query, indexCoordinates)
    elasticsearchOperations.indexOps(indexCoordinates).refresh()
  }
}

context(service: ElasticsearchService)
inline fun <reified D : Any> TypedValue<*, *>.find(): D? {
  return service.elasticsearchOperations.get(
    this.value.toString(),
    D::class.java,
    service.elasticsearchOperations.getIndexCoordinatesFor(D::class.java),
  )
}

context(service: ElasticsearchService)
fun ElasticsearchDocument<*, *>.index() {
  service.index(this, this.id.value.toString(), this::class)
}
