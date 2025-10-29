<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Hibernate (JPA) Integration

The `typed-value-hibernate` module provides abstract entity classes and utilities for JPA/Hibernate entities with TypedValue IDs.

::: info Built with
- **Hibernate** 7.x
- **Jakarta Persistence API** 3.2.x
- **Spring Data JPA** 4.0.x (optional, for repository support)
:::

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-hibernate:{{ v.typedValue }}")

    // JPA implementation
    implementation("org.hibernate.orm:hibernate-core:7.x.x")
}
```

---

## Abstract Entity Classes

The module provides abstract base classes for common ID types with proper `equals()`/`hashCode()` implementations following JPA best practices.

### AbstractIntEntity

For entities with auto-generated `Int` IDs:

```kotlin
@Entity
class Category : AbstractIntEntity<Category>(Category::class) {
    var name: String? = null
}

// Usage
val category = Category().apply { name = "Electronics" }
categoryRepository.save(category)
println(category.id) // TypedInt<Category>
```

### AbstractLongEntity

For entities with auto-generated `Long` IDs:

```kotlin
@Entity
class Product : AbstractLongEntity<Product>(Product::class) {
    var name: String? = null
    var price: BigDecimal? = null
}

// Usage
val product = Product().apply {
    name = "Widget"
    price = BigDecimal("29.99")
}
productRepository.save(product)
println(product.id) // TypedLong<Product>
```

### AbstractUuidEntity

For entities with auto-generated `UUID` IDs:

```kotlin
@Entity
class Order : AbstractUuidEntity<Order>(Order::class) {
    var total: BigDecimal? = null
    var status: String? = null
}

// Usage
val order = Order().apply {
    total = BigDecimal("99.99")
    status = "PENDING"
}
orderRepository.save(order)
println(order.id) // TypedUuid<Order>
```

### AbstractStringEntity

For entities with auto-generated `String` (UUID) IDs:

```kotlin
@Entity
class Document : AbstractStringEntity<Document>(Document::class) {
    var title: String? = null
    var content: String? = null
}

// Usage
val doc = Document().apply {
    title = "Report"
    content = "..."
}
documentRepository.save(doc)
println(doc.id) // TypedString<Document> with UUID value
```

---

## HibernateEntityUtils

For entities that don't extend the abstract classes, use `HibernateEntityUtils` to implement proper `equals()`/`hashCode()`:

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
    var email: String? = null

    override fun equals(other: Any?) =
        HibernateEntityUtils.entityEquals(this, other) { it.id }

    override fun hashCode() =
        HibernateEntityUtils.entityHashCode(this)
}
```

### Why Use HibernateEntityUtils?

JPA entities require special handling for `equals()` and `hashCode()`:

::: tip Best Practices
1. **Never use `@Id` in `hashCode()`** - The ID is null for new entities, causing issues with collections
2. **Use a constant `hashCode()`** - Entities remain in the same hash bucket even after ID assignment
3. **Check ID equality when both entities are persisted** - Use ID for equality only after save
4. **Handle Hibernate proxies** - Use `Hibernate.getClass()` instead of `getClass()`
:::

### References

The implementation follows these authoritative sources:

- [Hibernate User Guide - Equals and HashCode](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#mapping-model-pojo-equalshashcode)
- [Vlad Mihalcea - Entity Identifier](https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/)
- [Vlad Mihalcea - Equality and HashCode](https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/)
- [Thorben Janssen - Equals/HashCode Pitfalls](https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/)

---

## Spring Data JPA Repository Support

### TypedValueJpaRepositoryFactoryBean

Enable Spring Data JPA repositories with TypedValue IDs:

```kotlin
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = TypedValueJpaRepositoryFactoryBean::class
)
class JpaConfig
```

### Repository Definition

Define repositories with TypedValue ID types:

```kotlin
interface ProductRepository : JpaRepository<Product, TypedLong<Product>>
interface OrderRepository : JpaRepository<Order, TypedUuid<Order>>
interface CategoryRepository : JpaRepository<Category, TypedInt<Category>>
interface DocumentRepository : JpaRepository<Document, TypedString<Document>>
```

### Usage

```kotlin
@Service
class ProductService(private val productRepository: ProductRepository) {

    fun findProduct(id: TypedLong<Product>): Product? {
        return productRepository.findById(id).orElse(null)
    }

    fun productExists(id: TypedLong<Product>): Boolean {
        return productRepository.existsById(id)
    }

    fun deleteProduct(id: TypedLong<Product>) {
        productRepository.deleteById(id)
    }

    fun findProducts(ids: List<TypedLong<Product>>): List<Product> {
        return productRepository.findAllById(ids)
    }
}
```

---

## Attribute Converters

For TypedValue fields (not `@Id` fields), use attribute converters:

### Defining Converters

```kotlin
@Converter
class UserIdConverter : TypedUuidConverter<User>(User::class)

@Converter
class ProductIdConverter : TypedLongConverter<Product>(Product::class)
```

### Using Converters

```kotlin
@Entity
class OrderItem : AbstractLongEntity<OrderItem>(OrderItem::class) {

    @Convert(converter = ProductIdConverter::class)
    var productId: TypedLong<Product>? = null

    var quantity: Int = 0
}
```

### Available Converters

| Converter | TypedValue Type | Database Type |
|-----------|-----------------|---------------|
| `TypedStringConverter<T>` | `TypedString<T>` | `String` |
| `TypedIntConverter<T>` | `TypedInt<T>` | `Int` |
| `TypedLongConverter<T>` | `TypedLong<T>` | `Long` |
| `TypedUuidConverter<T>` | `TypedUuid<T>` | `UUID` |

---

## QueryDSL Support

For QueryDSL integration with abstract entities, add the `typed-value-querydsl` module:

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-hibernate:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-querydsl:{{ v.typedValue }}")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}
```

The `typed-value-querydsl` module provides Q-classes (`QAbstractIntEntity`, `QAbstractLongEntity`, `QAbstractUuidEntity`, `QAbstractStringEntity`) that allow QueryDSL's kapt to generate Q-classes for your entities.

See [QueryDSL Integration](/integrations/querydsl) for usage details.

---

## Complete Example

```kotlin
// Configuration
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = TypedValueJpaRepositoryFactoryBean::class
)
class JpaConfig

// Entity using abstract class
@Entity
class Product : AbstractLongEntity<Product>(Product::class) {
    var name: String? = null
    var price: BigDecimal? = null
}

// Repository
interface ProductRepository : JpaRepository<Product, TypedLong<Product>>

// Service
@Service
class ProductService(private val productRepository: ProductRepository) {

    fun createProduct(name: String, price: BigDecimal): Product {
        val product = Product().apply {
            this.name = name
            this.price = price
        }
        return productRepository.save(product)
    }

    fun findById(id: TypedLong<Product>): Product? =
        productRepository.findById(id).orElse(null)
}
```

---

## API Summary

### Abstract Entity Classes

| Class | ID Type | Generation Strategy |
|-------|---------|-------------------|
| `AbstractIntEntity<T>` | `TypedInt<T>` | `IDENTITY` |
| `AbstractLongEntity<T>` | `TypedLong<T>` | `IDENTITY` |
| `AbstractUuidEntity<T>` | `TypedUuid<T>` | `UUID` |
| `AbstractStringEntity<T>` | `TypedString<T>` | `UUID` (as String) |

### Utility Classes

| Class | Purpose |
|-------|---------|
| `HibernateEntityUtils` | Helper for custom entity equals/hashCode |
| `TypedValueJpaRepository` | Repository implementation for TypedValue IDs |
| `TypedValueJpaRepositoryFactoryBean` | Spring factory bean for configuration |

## Next Steps

- [QueryDSL Integration](/integrations/querydsl) - Type-safe queries
- [Elasticsearch Integration](/integrations/elasticsearch) - Document mapping
