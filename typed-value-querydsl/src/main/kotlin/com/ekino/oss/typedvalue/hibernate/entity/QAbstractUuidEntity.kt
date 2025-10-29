/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import com.querydsl.core.types.dsl.ComparablePath
import com.querydsl.core.types.dsl.EntityPathBase
import java.util.UUID

/**
 * QueryDSL Q-class for [AbstractUuidEntity].
 *
 * This class is provided for consumers who extend [AbstractUuidEntity] and use QueryDSL. It allows
 * generated Q-classes to properly reference the supertype.
 *
 * @param I The entity type parameter
 */
@Suppress("UNCHECKED_CAST", "VariableNaming")
class QAbstractUuidEntity<I : Any>(path: EntityPathBase<out AbstractUuidEntity<*>>) :
  EntityPathBase<AbstractUuidEntity<I>>(path.type as Class<AbstractUuidEntity<I>>, path.metadata) {

  @JvmField val _id: ComparablePath<UUID> = createComparable("_id", UUID::class.java)
}
