/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.hibernate.entity.AbstractStringEntity
import jakarta.persistence.Entity

/**
 * Example JPA entity demonstrating usage of [AbstractStringEntity] with auto-generated String
 * (UUID) IDs.
 */
@Entity
class StringPerson : AbstractStringEntity<StringPerson>(StringPerson::class) {
  var name: String? = null
}
