/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.querydsl

import com.ekino.oss.typedvalue.TypedValue
import com.querydsl.core.types.Expression
import com.querydsl.core.types.FactoryExpression
import com.querydsl.core.types.Path
import com.querydsl.core.types.Visitor
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpression
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.StringPath
import java.util.function.Function

/**
 * QueryDSL FactoryExpression for TypedValue that enables type-safe query construction.
 *
 * This class bridges TypedValue with QueryDSL's expression system, allowing you to write type-safe
 * queries with compile-time verification.
 *
 * Example usage:
 * ```kotlin
 * val qUser = QUser.user
 * val userIdExpr = TypedValueExpression.typedValueExpressionOf(qUser)
 *
 * // Use in queries
 * query.where(userIdExpr.eq(userId))
 * query.where(userIdExpr.isIn(userIds))
 * ```
 *
 * Supports different ID types:
 * - String IDs → StringPath
 * - Long IDs → NumberPath<Long>
 * - Int IDs → NumberPath<Integer>
 * - UUID IDs → ComparableExpression<UUID>
 *
 * @param ID The type of the identifier (String, Long, UUID, etc.)
 * @param T The entity type
 */
class TypedValueExpression<ID : Comparable<ID>, T : Any>
private constructor(private val actualType: Class<out T>, private val idPath: Expression<ID>) :
  FactoryExpression<TypedValue<ID, T>> {

  private val args = listOf(idPath)

  override fun newInstance(vararg args: Any): TypedValue<ID, T>? {
    val rawId = args.firstOrNull() ?: return null
    @Suppress("UNCHECKED_CAST")
    return TypedValue(rawId as ID, actualType.kotlin)
  }

  override fun getType(): Class<out TypedValue<ID, T>> {
    @Suppress("UNCHECKED_CAST")
    return TypedValue::class.java as Class<TypedValue<ID, T>>
  }

  override fun getArgs(): List<Expression<*>> = args

  override fun <R, C> accept(v: Visitor<R, C>, context: C?): R? = v.visit(this, context)

  /** Get the underlying QueryDSL path */
  fun path(): Expression<*> = idPath

  /**
   * Create equality predicate: `this == right`
   *
   * @param right The TypedValue to compare with
   * @return Boolean expression for the equality check
   */
  fun eq(right: TypedValue<ID, T>): BooleanExpression {
    @Suppress("UNCHECKED_CAST")
    return when (idPath) {
      is StringPath -> idPath.eq(right.value as String)
      is ComparableExpressionBase<*> -> (idPath as ComparableExpressionBase<ID>).eq(right.value)
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  /**
   * Create inequality predicate: `this != right`
   *
   * @param right The TypedValue to compare with
   * @return Boolean expression for the inequality check
   */
  fun ne(right: TypedValue<ID, T>): BooleanExpression {
    @Suppress("UNCHECKED_CAST")
    return when (idPath) {
      is StringPath -> idPath.ne(right.value as String)
      is ComparableExpressionBase<*> -> (idPath as ComparableExpression<ID>).ne(right.value)
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  /**
   * Create IN predicate: `this IN collection`
   *
   * @param collection Collection of TypedValues
   * @return Boolean expression for the IN check
   */
  fun <S : TypedValue<ID, T>> isIn(collection: Collection<S>): BooleanExpression {
    val rawIds = collection.map { it.value }
    @Suppress("UNCHECKED_CAST")
    return when (idPath) {
      is StringPath -> idPath.`in`(rawIds as Collection<String>)
      is ComparableExpressionBase<*> -> (idPath as ComparableExpressionBase<ID>).`in`(rawIds)
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  /**
   * Create NOT IN predicate: `this NOT IN collection`
   *
   * @param collection Collection of TypedValues
   * @return Boolean expression for the NOT IN check
   */
  fun <S : TypedValue<ID, T>> notIn(collection: Collection<S>): BooleanExpression {
    val rawIds = collection.map { it.value }
    @Suppress("UNCHECKED_CAST")
    return when (idPath) {
      is StringPath -> idPath.notIn(rawIds as Collection<String>)
      is ComparableExpressionBase<*> -> (idPath as ComparableExpressionBase<ID>).notIn(rawIds)
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  /**
   * Create IS NULL predicate
   *
   * @return Boolean expression for NULL check
   */
  fun isNull(): BooleanExpression {
    return when (idPath) {
      is StringPath -> idPath.isNull
      is ComparableExpressionBase<*> -> idPath.isNull
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  /**
   * Create IS NOT NULL predicate
   *
   * @return Boolean expression for NOT NULL check
   */
  fun isNotNull(): BooleanExpression {
    return when (idPath) {
      is StringPath -> idPath.isNotNull
      is ComparableExpressionBase<*> -> idPath.isNotNull
      else -> throw UnsupportedOperationException("Unsupported path type: ${idPath.javaClass}")
    }
  }

  companion object {

    @JvmStatic
    fun <ID : Comparable<ID>, T : Any, E : EntityPathBase<T>> E.typedValueExpressionOf(
      pathSelector: Function<E, Path<ID>>
    ): TypedValueExpression<ID, T> {
      val entityPath = pathSelector.apply(this)
      require(this.root == entityPath.root) { "EntityPath and id Path must share the same root" }
      @Suppress("UNCHECKED_CAST")
      return TypedValueExpression(this.type as Class<out T>, entityPath)
    }
  }
}
