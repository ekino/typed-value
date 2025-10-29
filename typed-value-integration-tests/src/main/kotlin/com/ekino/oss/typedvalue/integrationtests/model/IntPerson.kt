/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.hibernate.entity.AbstractIntEntity
import jakarta.persistence.Entity

/** Example JPA entity demonstrating usage of [AbstractIntEntity] with auto-increment Int IDs. */
@Entity
class IntPerson : AbstractIntEntity<IntPerson>(IntPerson::class) {
  var name: String? = null
}
