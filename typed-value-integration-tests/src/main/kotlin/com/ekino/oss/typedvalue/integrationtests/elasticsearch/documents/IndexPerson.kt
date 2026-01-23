/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.elasticsearch.documents

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.TypedValue
import com.ekino.oss.typedvalue.integrationtests.elasticsearch.ElasticsearchDocument
import com.ekino.oss.typedvalue.integrationtests.model.Person
import com.ekino.oss.typedvalue.integrationtests.model.TypedId
import java.math.BigDecimal
import java.util.UUID
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "person")
data class IndexPerson(
  @Field(type = FieldType.Keyword) override val id: TypedUuid<Person>,
  @Field(type = FieldType.Text) val name: String?,
  @Field(type = FieldType.Keyword) val stringId: TypedString<Person>? = null,
  @Field(type = FieldType.Integer) val intId: TypedInt<Person>? = null,
  @Field(type = FieldType.Long) val longId: TypedLong<Person>? = null,
  @Field(type = FieldType.Keyword) val genericStringId: TypedValue<String, Person>? = null,
  @Field(type = FieldType.Keyword) val genericUuidId: TypedValue<UUID, Person>? = null,
  @Field(type = FieldType.Keyword) val customId: TypedId<Person>? = null,
  @Field(type = FieldType.Double) val someBigDecimal: BigDecimal? = null,
) : ElasticsearchDocument<Person, UUID>
