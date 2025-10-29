/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.hibernate.entity.AbstractLongEntity
import jakarta.persistence.Entity

/** Example JPA entity demonstrating usage of [AbstractLongEntity] with auto-increment Long IDs. */
@Entity
class LongPerson : AbstractLongEntity<LongPerson>(LongPerson::class) {
  var name: String? = null
}
