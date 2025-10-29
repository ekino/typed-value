/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.NumberPath

/**
 * QueryDSL Q-class for [AbstractLongEntity].
 *
 * This class is provided for consumers who extend [AbstractLongEntity] and use QueryDSL. It allows
 * generated Q-classes to properly reference the supertype.
 *
 * @param I The entity type parameter
 */
@Suppress("UNCHECKED_CAST", "VariableNaming")
class QAbstractLongEntity<I : Any>(path: EntityPathBase<out AbstractLongEntity<*>>) :
  EntityPathBase<AbstractLongEntity<I>>(path.type as Class<AbstractLongEntity<I>>, path.metadata) {

  @JvmField val _id: NumberPath<Long> = createNumber("_id", Long::class.javaObjectType)
}
