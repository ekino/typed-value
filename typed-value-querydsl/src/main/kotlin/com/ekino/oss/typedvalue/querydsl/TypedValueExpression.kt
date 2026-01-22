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
class TypedValueExpression<ID : Comparable<ID>, T : Any, E : TypedValue<ID, T>>(
  private val typedIdClass: Class<E>,
  private val idPath: Expression<ID>,
  private val constructor: (value: ID) -> E,
) : FactoryExpression<E> {

  private val args = listOf(idPath)

  override fun newInstance(vararg args: Any): E? {
    val rawId = args.firstOrNull() ?: return null
    @Suppress("UNCHECKED_CAST")
    return constructor(rawId as ID)
  }

  override fun getType(): Class<E> {
    return typedIdClass
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

    /**
     * Factory method for creating TypedValueExpression with explicit class parameter. This method
     * is primarily for Java interop or when the type cannot be inferred.
     *
     * **Java Usage Note**: Due to Java's type erasure, when using custom TypedValue subclasses,
     * you'll get unchecked cast warnings. This is expected and safe - use
     * `@SuppressWarnings("unchecked")` on the calling method.
     *
     * Example Java usage:
     * ```java
     * @SuppressWarnings("unchecked")
     * TypedValueExpression<String, User, TypedId<User>> expr =
     *     typedValueExpressionOf(TypedId.class, path, value -> new TypedId<>(value, userKClass));
     * ```
     *
     * **Kotlin Usage**: Prefer the inline reified extension function `Path.typedValueExpressionOf`
     * which provides better type safety and doesn't require passing the class explicitly.
     *
     * @param typedValueClass The Class object for the TypedValue type V
     * @param path The QueryDSL path expression
     * @param constructor Function to construct V from ID value
     * @return TypedValueExpression instance
     */
    @JvmStatic
    fun <ID : Comparable<ID>, T : Any, V : TypedValue<ID, T>> typedValueExpressionOf(
      typedValueClass: Class<out V>,
      path: Path<ID>,
      constructor: Function<ID, V>,
    ): TypedValueExpression<ID, T, V> {
      @Suppress("UNCHECKED_CAST")
      return TypedValueExpression(typedValueClass as Class<V>, path, constructor::apply)
    }

    /**
     * Kotlin extension function for creating TypedValueExpression with automatic type inference.
     * This is the preferred method for Kotlin code as it provides compile-time type safety without
     * needing to pass the class explicitly.
     *
     * Example usage with custom TypedValue:
     * ```kotlin
     * val expression = QUser.user.id.typedValueExpressionOf<TypedId<User>> { id ->
     *   TypedId(id, User::class)
     * }
     * ```
     *
     * Example usage with standard TypedValue (type can often be inferred):
     * ```kotlin
     * val expression = QUser.user.id.typedValueExpressionOf { id ->
     *   TypedString.of(id, User::class)
     * }
     * ```
     *
     * @param V The TypedValue type (can be TypedValue, TypedString, or custom subclass)
     * @param ID The type of the identifier (String, Long, UUID, etc.)
     * @param T The entity type
     * @param constructor Function to construct V from ID value
     * @return TypedValueExpression instance
     */
    inline fun <reified V : TypedValue<ID, T>, ID : Comparable<ID>, T : Any> Path<ID>
      .typedValueExpressionOf(
      noinline constructor: (value: ID) -> V
    ): TypedValueExpression<ID, T, V> {
      return TypedValueExpression(V::class.java, this, constructor)
    }

    @JvmStatic
    fun <ID : Comparable<ID>, T : Any, E : EntityPathBase<T>> E.typedValueExpressionOf(
      pathSelector: Function<E, Path<ID>>
    ): TypedValueExpression<ID, T, TypedValue<ID, T>> {
      val entityPath = pathSelector.apply(this)
      require(this.root == entityPath.root) { "EntityPath and id Path must share the same root" }
      @Suppress("UNCHECKED_CAST")
      return TypedValueExpression(TypedValue::class.java as Class<TypedValue<ID, T>>, entityPath) {
        value: ID ->
        TypedValue.typedValueFor(value, entityPath.root.type.kotlin) as TypedValue<ID, T>
      }
    }
  }
}
