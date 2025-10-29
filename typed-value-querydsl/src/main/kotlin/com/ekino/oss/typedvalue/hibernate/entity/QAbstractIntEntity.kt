/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.NumberPath

/**
 * QueryDSL Q-class for [AbstractIntEntity].
 *
 * This class is provided for consumers who extend [AbstractIntEntity] and use QueryDSL. It allows
 * generated Q-classes to properly reference the supertype.
 *
 * @param I The entity type parameter
 */
@Suppress("UNCHECKED_CAST", "VariableNaming", "PropertyName")
class QAbstractIntEntity<I : Any>(path: EntityPathBase<out AbstractIntEntity<*>>) :
  EntityPathBase<AbstractIntEntity<I>>(path.type as Class<AbstractIntEntity<I>>, path.metadata) {

  @JvmField val _id: NumberPath<Int> = createNumber("_id", Int::class.javaObjectType)
}
