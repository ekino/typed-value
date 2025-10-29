/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.hibernate.TypedIntConverter
import com.ekino.oss.typedvalue.hibernate.TypedLongConverter
import com.ekino.oss.typedvalue.hibernate.TypedStringConverter
import com.ekino.oss.typedvalue.hibernate.TypedUuidConverter
import com.ekino.oss.typedvalue.hibernate.entity.HibernateEntityUtils
import com.ekino.oss.typedvalue.toTypedUuid
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Transient
import java.util.UUID

/**
 * Example JPA entity demonstrating proper TypedValue usage with Hibernate.
 *
 * This entity uses:
 * - [HibernateEntityUtils] for proper equals/hashCode implementation
 * - Virtual typed ID property wrapping the raw UUID
 * - Converters for all TypedValue types (String, Int, Long, UUID)
 */
@Entity
class Person {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false)
  private var _id: UUID? = null

  @get:Transient
  var id: TypedUuid<Person>?
    get() = _id?.toTypedUuid()
    set(value) {
      _id = value?.value
    }

  var name: String? = null

  @Convert(converter = PersonUuidRefConverter::class) var someUuidRef: TypedUuid<Person>? = null

  @Convert(converter = PersonStringRefConverter::class)
  var someStringRef: TypedString<Person>? = null

  @Convert(converter = PersonIntRefConverter::class) var someIntRef: TypedInt<Person>? = null

  @Convert(converter = PersonLongRefConverter::class) var someLongRef: TypedLong<Person>? = null

  override fun equals(other: Any?) = HibernateEntityUtils.entityEquals(this, other) { it.id }

  override fun hashCode() = HibernateEntityUtils.entityHashCode(this)
}

@Converter class PersonUuidRefConverter : TypedUuidConverter<Person>(Person::class)

@Converter class PersonStringRefConverter : TypedStringConverter<Person>(Person::class)

@Converter class PersonIntRefConverter : TypedIntConverter<Person>(Person::class)

@Converter class PersonLongRefConverter : TypedLongConverter<Person>(Person::class)
