/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.StringPath

/**
 * QueryDSL Q-class for [AbstractStringEntity].
 *
 * This class is provided for consumers who extend [AbstractStringEntity] and use QueryDSL. It
 * allows generated Q-classes to properly reference the supertype.
 *
 * @param I The entity type parameter
 */
@Suppress("UNCHECKED_CAST", "VariableNaming")
class QAbstractStringEntity<I : Any>(path: EntityPathBase<out AbstractStringEntity<*>>) :
  EntityPathBase<AbstractStringEntity<I>>(
    path.type as Class<AbstractStringEntity<I>>,
    path.metadata,
  ) {

  @JvmField val _id: StringPath = createString("_id")
}
