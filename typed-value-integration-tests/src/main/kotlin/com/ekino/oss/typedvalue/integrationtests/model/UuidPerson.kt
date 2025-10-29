/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.model

import com.ekino.oss.typedvalue.hibernate.entity.AbstractUuidEntity
import jakarta.persistence.Entity

/** Example JPA entity demonstrating usage of [AbstractUuidEntity] with auto-generated UUID IDs. */
@Entity
class UuidPerson : AbstractUuidEntity<UuidPerson>(UuidPerson::class) {
  var name: String? = null
}
