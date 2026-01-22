/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.elasticsearch

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.ekino.oss.typedvalue.TypedValue
import com.ekino.oss.typedvalue.integrationtests.AbstractIntegrationTest
import com.ekino.oss.typedvalue.integrationtests.elasticsearch.documents.IndexPerson
import com.ekino.oss.typedvalue.integrationtests.model.Person
import com.ekino.oss.typedvalue.integrationtests.model.TypedId
import com.ekino.oss.typedvalue.toTypedInt
import com.ekino.oss.typedvalue.toTypedLong
import com.ekino.oss.typedvalue.toTypedString
import com.ekino.oss.typedvalue.toTypedUuid
import java.util.UUID
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for the `typed-value-spring-data-elasticsearch` module.
 *
 * These tests verify that TypedValue instances are correctly mapped and persisted in Elasticsearch
 * documents using the custom mapping context provided by
 * [com.ekino.oss.typedvalue.elasticsearch.TypedValueElasticsearchMappingContext].
 *
 * Tests cover all supported typed value types:
 * - [com.ekino.oss.typedvalue.TypedUuid] (UUID-based IDs)
 * - [com.ekino.oss.typedvalue.TypedString] (String-based IDs)
 * - [com.ekino.oss.typedvalue.TypedInt] (Int-based IDs)
 * - [com.ekino.oss.typedvalue.TypedLong] (Long-based IDs)
 * - Generic [com.ekino.oss.typedvalue.TypedValue] with String and UUID ID types
 * - Custom TypedValue subclasses (TypedId) registered via registerCustomTypedValue()
 */
class ElasticSearchMappingTest : AbstractIntegrationTest() {

  @Autowired lateinit var elasticsearchService: ElasticsearchService

  @Test
  fun `should map TypedUuid field to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      IndexPerson(id = id, name = "John Doe").index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.name }.isEqualTo("John Doe")
      }
    }
  }

  @Test
  fun `should map TypedString field to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val stringId = "person-abc-123".toTypedString<Person>()

      IndexPerson(id = id, name = "Jane Doe", stringId = stringId).index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.stringId }.isEqualTo(stringId)
      }
    }
  }

  @Test
  fun `should map TypedInt field to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val intId = 42.toTypedInt<Person>()

      IndexPerson(id = id, name = "Int Person", intId = intId).index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.intId }.isEqualTo(intId)
      }
    }
  }

  @Test
  fun `should map TypedLong field to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val longId = 9876543210L.toTypedLong<Person>()

      IndexPerson(id = id, name = "Long Person", longId = longId).index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.longId }.isEqualTo(longId)
      }
    }
  }

  @Test
  fun `should map generic TypedValue with String ID to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val genericStringId = TypedValue.typedValueFor("generic-string-id", Person::class)

      IndexPerson(id = id, name = "Generic String Person", genericStringId = genericStringId)
        .index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.genericStringId }.isEqualTo(genericStringId)
      }
    }
  }

  @Test
  fun `should map generic TypedValue with UUID ID to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val genericUuidId = TypedValue.typedValueFor(UUID.randomUUID(), Person::class)

      IndexPerson(id = id, name = "Generic UUID Person", genericUuidId = genericUuidId).index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.genericUuidId }.isEqualTo(genericUuidId)
      }
    }
  }

  @Test
  fun `should map custom TypedId field to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val customId = TypedId("custom-id-123", Person::class)

      IndexPerson(id = id, name = "Custom ID Person", customId = customId).index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.customId }
          .isNotNull()
          .all {
            // Verify it's TypedId (not TypedString fallback)
            isInstanceOf<TypedId<Person>>()
            transform { it.value }.isEqualTo("custom-id-123")
            transform { it.type }.isEqualTo(Person::class)
          }
      }
    }
  }

  @Test
  fun `should map all TypedValue variants together to Elasticsearch`() {
    context(elasticsearchService) {
      val id = UUID.randomUUID().toTypedUuid<Person>()
      val stringId = "all-variants-test".toTypedString<Person>()
      val intId = 123.toTypedInt<Person>()
      val longId = 456789L.toTypedLong<Person>()
      val genericStringId = TypedValue.typedValueFor("generic-all", Person::class)
      val genericUuidId = TypedValue.typedValueFor(UUID.randomUUID(), Person::class)
      val customId = TypedId("all-variants-custom", Person::class)

      IndexPerson(
          id = id,
          name = "All Variants Person",
          stringId = stringId,
          intId = intId,
          longId = longId,
          genericStringId = genericStringId,
          genericUuidId = genericUuidId,
          customId = customId,
        )
        .index()
      val retrievedPerson = id.find<IndexPerson>()

      assertThat(retrievedPerson).isNotNull().all {
        transform { it.id }.isEqualTo(id)
        transform { it.name }.isEqualTo("All Variants Person")
        transform { it.stringId }.isEqualTo(stringId)
        transform { it.intId }.isEqualTo(intId)
        transform { it.longId }.isEqualTo(longId)
        transform { it.genericStringId }.isEqualTo(genericStringId)
        transform { it.genericUuidId }.isEqualTo(genericUuidId)
        transform { it.customId }.isNotNull().isInstanceOf<TypedId<Person>>()
      }
    }
  }
}
