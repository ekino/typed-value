<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# QueryDSL Integration

The `typed-value-querydsl` module provides type-safe QueryDSL expressions for TypedValue fields.

::: info Built with
- **QueryDSL** 5.1.x
:::

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-hibernate:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-querydsl:{{ v.typedValue }}")

    // QueryDSL dependencies
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}
```

The `typed-value-querydsl` module provides:
- `TypedValueExpression` for type-safe QueryDSL expressions
- Q-classes for the abstract entity base classes (`QAbstractIntEntity`, `QAbstractLongEntity`, `QAbstractUuidEntity`, `QAbstractStringEntity`), allowing QueryDSL's kapt to generate Q-classes for your entities

---

## TypedValueExpression Class

Wraps a QueryDSL path to create type-safe expressions for TypedValue fields.

```kotlin
class TypedValueExpression<ID : Comparable<ID>, T : Any> : FactoryExpression<TypedValue<ID, T>>
```

### Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `eq()` | `eq(right: TypedValue<ID, T>): BooleanExpression` | Equality predicate |
| `ne()` | `ne(right: TypedValue<ID, T>): BooleanExpression` | Inequality predicate |
| `isIn()` | `isIn(collection: Collection<S>): BooleanExpression` | IN clause |
| `notIn()` | `notIn(collection: Collection<S>): BooleanExpression` | NOT IN clause |
| `isNull()` | `isNull(): BooleanExpression` | NULL check |
| `isNotNull()` | `isNotNull(): BooleanExpression` | NOT NULL check |
| `path()` | `path(): Expression<*>` | Access underlying QueryDSL path |

---

## Creating TypedValueExpression

Use the extension function on EntityPathBase:

```kotlin
inline fun <ID : Comparable<ID>, T : Any, E : EntityPathBase<T>>
    E.typedValueExpressionOf(
        pathSelector: Function<E, Path<ID>>
    ): TypedValueExpression<ID, T>
```

### With Hibernate Abstract Entities

When your entity extends one of the abstract entity classes, use the `_id` field:

```kotlin
@Entity
class Product : AbstractLongEntity<Product>(Product::class) {
    var name: String? = null
}

// In your repository/service
val qProduct = QProduct.product
val productIdExpr = qProduct.typedValueExpressionOf { it._id }
```

### With Custom Entities

For custom entities with TypedValue IDs:

```kotlin
@Entity
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private var _id: UUID? = null

    @get:Transient
    var id: TypedUuid<User>?
        get() = _id?.toTypedUuid()
        set(value) { _id = value?.value }

    var name: String? = null
}

// In your repository/service
val qUser = QUser.user
val userIdExpr = qUser.typedValueExpressionOf { it._id }
```

---

## Path Type Mapping

The expression creates appropriate path types based on the ID type:

| ID Type | QueryDSL Path Type |
|---------|-------------------|
| String | `StringPath` |
| Long | `NumberPath<Long>` |
| Int | `NumberPath<Integer>` |
| UUID | `ComparablePath<UUID>` |
| Other Comparable | `ComparableExpressionBase<*>` |

---

## Predicates

### Equality (eq)

```kotlin
val productId = 42L.toTypedLong<Product>()

val product = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.eq(productId))
    .fetchOne()
```

Generated: `product._id = 42`

### Inequality (ne)

```kotlin
val products = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.ne(excludedId))
    .fetch()
```

Generated: `product._id != 123`

### IN Clause (isIn)

```kotlin
val productIds = listOf(
    1L.toTypedLong<Product>(),
    2L.toTypedLong<Product>(),
    3L.toTypedLong<Product>()
)

val products = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.isIn(productIds))
    .fetch()
```

Generated: `product._id IN (1, 2, 3)`

### NOT IN Clause (notIn)

```kotlin
val products = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.notIn(excludedIds))
    .fetch()
```

Generated: `product._id NOT IN (1, 2)`

### NULL Checks

```kotlin
// Find products without ID (new/unsaved)
val newProducts = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.isNull())
    .fetch()

// Find products with ID (persisted)
val savedProducts = queryFactory.selectFrom(qProduct)
    .where(productIdExpr.isNotNull())
    .fetch()
```

---

## Complex Queries

Combine with other predicates:

```kotlin
val products = queryFactory.selectFrom(qProduct)
    .where(
        productIdExpr.isIn(productIds)
            .and(qProduct.status.eq(ProductStatus.ACTIVE))
            .and(qProduct.createdAt.after(startDate))
    )
    .orderBy(qProduct.name.asc())
    .fetch()
```

---

## Complete Example

```kotlin
@Repository
class ProductQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val qProduct = QProduct.product
    private val productIdExpr = qProduct.typedValueExpressionOf { it._id }

    fun findById(id: TypedLong<Product>): Product? {
        return queryFactory.selectFrom(qProduct)
            .where(productIdExpr.eq(id))
            .fetchOne()
    }

    fun findByIds(ids: List<TypedLong<Product>>): List<Product> {
        return queryFactory.selectFrom(qProduct)
            .where(productIdExpr.isIn(ids))
            .fetch()
    }

    fun findActiveProductsExcluding(excludedIds: List<TypedLong<Product>>): List<Product> {
        return queryFactory.selectFrom(qProduct)
            .where(
                productIdExpr.notIn(excludedIds)
                    .and(qProduct.status.eq(ProductStatus.ACTIVE))
            )
            .orderBy(qProduct.name.asc())
            .fetch()
    }

    fun findByCategory(categoryId: TypedInt<Category>): List<Product> {
        val categoryIdExpr = qProduct.typedValueExpressionOf { it.categoryId }
        return queryFactory.selectFrom(qProduct)
            .where(categoryIdExpr.eq(categoryId))
            .fetch()
    }
}
```

---

## Working with All Entity Types

### IntPerson (AbstractIntEntity)

```kotlin
val qIntPerson = QIntPerson.intPerson
val intPersonIdExpr = qIntPerson.typedValueExpressionOf { it._id }

val person = queryFactory.selectFrom(qIntPerson)
    .where(intPersonIdExpr.eq(personId))
    .fetchOne()
```

### LongPerson (AbstractLongEntity)

```kotlin
val qLongPerson = QLongPerson.longPerson
val longPersonIdExpr = qLongPerson.typedValueExpressionOf { it._id }

val persons = queryFactory.selectFrom(qLongPerson)
    .where(longPersonIdExpr.isIn(personIds))
    .fetch()
```

### UuidPerson (AbstractUuidEntity)

```kotlin
val qUuidPerson = QUuidPerson.uuidPerson
val uuidPersonIdExpr = qUuidPerson.typedValueExpressionOf { it._id }

val person = queryFactory.selectFrom(qUuidPerson)
    .where(uuidPersonIdExpr.eq(personId))
    .fetchOne()
```

### StringPerson (AbstractStringEntity)

```kotlin
val qStringPerson = QStringPerson.stringPerson
val stringPersonIdExpr = qStringPerson.typedValueExpressionOf { it._id }

val persons = queryFactory.selectFrom(qStringPerson)
    .where(stringPersonIdExpr.isNotNull())
    .fetch()
```

---

## API Summary

| Method | Description |
|--------|-------------|
| `typedValueExpressionOf()` | Create TypedValueExpression from EntityPath |
| `eq(value)` | Equality predicate |
| `ne(value)` | Inequality predicate |
| `isIn(collection)` | IN clause |
| `notIn(collection)` | NOT IN clause |
| `isNull()` | NULL check |
| `isNotNull()` | NOT NULL check |
| `path()` | Access underlying QueryDSL path |

## Next Steps

- [Hibernate Integration](/integrations/hibernate) - JPA persistence
- [Elasticsearch Integration](/integrations/elasticsearch) - Search support
