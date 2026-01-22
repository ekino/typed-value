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
class TypedValueExpression<ID : Comparable<ID>, T : Any, E : TypedValue<ID, T>> : FactoryExpression<E>
```

The third type parameter `E` allows using custom TypedValue subclasses (e.g., `TypedId<User>`) instead of just the base `TypedValue<ID, T>` type.

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

## Custom TypedValue Types

::: tip Constructor-Based Approach
QueryDSL uses an explicit constructor-based pattern for custom types, providing maximum flexibility at the query level.
:::

### Overview

When you create custom TypedValue subclasses (e.g., `TypedId` extending `TypedString`), you can specify how to construct them from query results using an explicit constructor parameter.

### Use Case

```kotlin
// Custom TypedValue type
open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

// Generic TypedValue (default):
val genericExpr = qUser.typedValueExpressionOf { it._id }
// Results: TypedValue<String, User>

// Custom TypedValue (with constructor):
val customExpr = qUser._id.typedValueExpressionOf { id -> TypedId(id, User::class) }
// Results: TypedId<User> ✓
```

### Factory Methods

**Method 1: Custom Types with Constructor** (New!)

```kotlin
inline fun <reified V : TypedValue<ID, T>, ID : Comparable<ID>, T : Any>
    Path<ID>.typedValueExpressionOf(
        noinline constructor: (value: ID) -> V
    ): TypedValueExpression<ID, T, V>
```

Use this when you want custom TypedValue instances in query results. The inline reified generic parameter ensures the correct runtime type is captured.

**Method 1b: Custom Types (Non-inline, Java interop)**

```kotlin
@JvmStatic
fun <ID : Comparable<ID>, T : Any, V : TypedValue<ID, T>> typedValueExpressionOf(
    typedValueClass: Class<out V>,
    path: Path<ID>,
    constructor: Function<ID, V>
): TypedValueExpression<ID, T, V>
```

Use this from Java or when you need to pass the class explicitly. See [Java Interop](#java-interop) section for usage.

**Method 2: Generic TypedValue** (Existing)

```kotlin
inline fun <ID : Comparable<ID>, T : Any, E : EntityPathBase<T>>
    E.typedValueExpressionOf(
        pathSelector: Function<E, Path<ID>>
    ): TypedValueExpression<ID, T, TypedValue<ID, T>>
```

Use this for automatic TypedValue construction (base type).

### Basic Usage

**Define Custom Type:**

```kotlin
open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

@Entity
class User : AbstractStringEntity<User>(User::class) {
    var name: String? = null
}
```

**Create Expression with Custom Constructor:**

```kotlin
val qUser = QUser.user

// Method 1: Custom type with explicit constructor
// Note: Must specify the type parameter explicitly for custom types
val customIdExpr = qUser._id.typedValueExpressionOf<TypedId<User>> { id ->
    TypedId(id, User::class)
}

// Method 2: Generic TypedValue (comparison)
val genericIdExpr = qUser.typedValueExpressionOf { it._id }
```

::: warning Type Parameter Required
When using custom TypedValue subclasses, you must explicitly specify the type parameter (e.g., `<TypedId<User>>`). This ensures QueryDSL's `Projections.constructor()` receives the correct runtime class.
:::

**Use in Queries:**

```kotlin
val userId = TypedId("user-123", User::class)

// Query with custom type
val user = queryFactory.selectFrom(qUser)
    .where(customIdExpr.eq(userId))
    .fetchOne()

// Select the ID itself
val ids: List<TypedId<User>> = queryFactory
    .select(customIdExpr)
    .from(qUser)
    .fetch()  // Returns List<TypedId<User>>, not List<TypedString<User>>
```

### Complete Example

```kotlin
// Custom types
open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
open class TypedCode<T : Any>(code: String, type: KClass<T>) : TypedString<T>(code, type)

@Entity
class Product : AbstractLongEntity<Product>(Product::class) {
    var name: String? = null
    var categoryCode: String? = null
}

@Repository
class ProductQueryRepository(private val queryFactory: JPAQueryFactory) {
    private val qProduct = QProduct.product

    // Create expressions with custom constructors
    // Note: Explicit type parameters required for custom types
    private val productIdExpr = qProduct._id.typedValueExpressionOf<TypedId<Product>> { id ->
        TypedId(id.toString(), Product::class)
    }

    private val categoryCodeExpr = qProduct.categoryCode.typedValueExpressionOf<TypedCode<Product>> { code ->
        TypedCode(code, Product::class)
    }

    fun findByCustomId(id: TypedId<Product>): Product? {
        return queryFactory.selectFrom(qProduct)
            .where(productIdExpr.eq(id))
            .fetchOne()
    }

    fun findByCustomIds(ids: List<TypedId<Product>>): List<Product> {
        return queryFactory.selectFrom(qProduct)
            .where(productIdExpr.isIn(ids))
            .fetch()
    }

    fun findByCategoryCode(code: TypedCode<Product>): List<Product> {
        return queryFactory.selectFrom(qProduct)
            .where(categoryCodeExpr.eq(code))
            .fetch()
    }

    // Select IDs directly (returns custom types)
    fun getAllProductIds(): List<TypedId<Product>> {
        return queryFactory
            .select(productIdExpr)
            .from(qProduct)
            .fetch()  // Returns List<TypedId<Product>>
    }
}
```

### Multiple Custom Types

Use different constructors for different purposes:

```kotlin
// Different custom types for different use cases
open class TypedId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)
open class ExternalId<T : Any>(id: String, type: KClass<T>) : TypedString<T>(id, type)

val qUser = QUser.user

// Different expressions for different fields
val internalIdExpr = qUser._id.typedValueExpressionOf<TypedId<User>> { id ->
    TypedId(id, User::class)
}

val externalIdExpr = qUser.externalId.typedValueExpressionOf<ExternalId<User>> { id ->
    ExternalId(id, User::class)
}

// Use in same query
val users = queryFactory.selectFrom(qUser)
    .where(
        internalIdExpr.isIn(internalIds)
            .and(externalIdExpr.isIn(externalIds))
    )
    .fetch()
```

### Java Interop

When using custom TypedValue types from Java, you'll encounter unchecked cast warnings due to Java's type erasure. This is expected and safe.

**Java Example:**

```java
import static com.ekino.oss.typedvalue.querydsl.TypedValueExpression.typedValueExpressionOf;

@Repository
public class ProductRepository {
    private final JPAQueryFactory queryFactory;
    private final QProduct qProduct = QProduct.product;

    @SuppressWarnings("unchecked")  // Safe: types are verified at compile time
    private TypedValueExpression<String, Product, TypedId<Product>> createExpression() {
        return typedValueExpressionOf(
            TypedId.class,
            qProduct._id,
            value -> new TypedId<>(value, ProductKt.getProductClass())
        );
    }

    public Product findById(TypedId<Product> id) {
        TypedValueExpression<String, Product, TypedId<Product>> expr = createExpression();
        return queryFactory.selectFrom(qProduct)
            .where(expr.eq(id))
            .fetchOne();
    }
}
```

::: tip Java Limitation
The `@SuppressWarnings("unchecked")` annotation is necessary because:
- Java's `TypedId.class` is `Class<TypedId>` (raw type), not `Class<TypedId<Product>>`
- Generic type parameters don't exist at runtime in Java (type erasure)
- This is standard Java practice when working with generics and reflection

The suppression is **safe** because the compiler verifies the constructor returns the correct type.
:::

### Comparison: Constructor vs Generic

| Aspect | Custom Constructor | Generic TypedValue |
|--------|-------------------|-------------------|
| **Syntax** | `path.typedValueExpressionOf<Custom<T>> { id -> Custom(...) }` | `entity.typedValueExpressionOf { it._id }` |
| **Result Type** | Custom type (e.g., `TypedId<User>`) | `TypedValue<ID, T>` |
| **Use Case** | Domain-specific types | General purpose |
| **Flexibility** | Different per expression | Consistent across queries |
| **Configuration** | Explicit type parameter required | None needed |
| **Type Safety** | Compile-time verified with reified generics | Compile-time verified |
| **Java Interop** | Requires `@SuppressWarnings("unchecked")` | Works without warnings |

### When to Use Each Approach

**Use Custom Constructor When:**
- You have domain-specific ID types (`TypedId`, `ExternalId`, etc.)
- You want type information in query results
- You need different behaviors for different ID types
- You're building a rich domain model

**Use Generic TypedValue When:**
- Simple use cases without custom types
- Prototyping or exploration
- No need for specialized ID behavior
- Consistent handling across all queries

### Design Rationale

QueryDSL uses a **constructor-based pattern** rather than registration (like Elasticsearch) because:

1. **Query Flexibility**: Different queries can use different constructors
2. **No Configuration**: No Spring beans or initialization required
3. **Explicit Dependencies**: Constructor is visible at call site
4. **Type Safety**: Full compile-time verification
5. **Zero Overhead**: No lookups or reflection

This pattern fits QueryDSL's fluent API philosophy perfectly.

### Projection with Custom Types

```kotlin
// Custom type for projections
data class UserSummary(
    val id: TypedId<User>,
    val name: String
)

val customIdExpr = qUser._id.typedValueExpressionOf<TypedId<User>> { id ->
    TypedId(id, User::class)
}

val summaries = queryFactory
    .select(
        Projections.constructor(
            UserSummary::class.java,
            customIdExpr,
            qUser.name
        )
    )
    .from(qUser)
    .fetch()  // Returns List<UserSummary> with TypedId fields
```

### Testing Custom Types

```kotlin
@Test
fun `should work with custom TypedValue types`() {
    val customExpr = qUser._id.typedValueExpressionOf<TypedId<User>> { id ->
        TypedId(id, User::class)
    }

    val userId = TypedId("test-123", User::class)

    val user = queryFactory.selectFrom(qUser)
        .where(customExpr.eq(userId))
        .fetchOne()

    // Result has custom type
    val retrievedId: TypedId<User> = queryFactory
        .select(customExpr)
        .from(qUser)
        .where(customExpr.eq(userId))
        .fetchOne()!!

    assertThat(retrievedId).isInstanceOf<TypedId<User>>()
}
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
