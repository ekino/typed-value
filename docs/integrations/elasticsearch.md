<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Elasticsearch Integration

The `typed-value-spring-data-elasticsearch` module provides TypedValue support for Spring Data Elasticsearch documents.

::: info Built with
- **Spring Data Elasticsearch** 6.0.x
:::

## Installation

```kotlin-vue
dependencies {
    implementation("com.ekino.oss:typed-value-core:{{ v.typedValue }}")
    implementation("com.ekino.oss:typed-value-spring-data-elasticsearch:{{ v.typedValue }}")

    // Spring Data Elasticsearch
    implementation("org.springframework.data:spring-data-elasticsearch:6.x.x")
}
```

---

## TypedValueElasticsearchMappingContext

Custom Elasticsearch mapping context that handles TypedValue serialization automatically.

```kotlin
class TypedValueElasticsearchMappingContext : SimpleElasticsearchMappingContext()
```

### Features

- Automatically detects TypedValue fields
- Resolves generic type parameters at compile time
- Converts TypedValue to raw ID for storage
- Reconstructs TypedValue from raw ID on retrieval
- Supports `Collection<TypedValue>` (Lists only)

### Registered Simple Types

The mapping context registers TypedValue and all convenience types as simple types:

```kotlin
setSimpleTypeHolder(
    SimpleTypeHolder(
        setOf(
            TypedValue::class.java,
            TypedString::class.java,
            TypedInt::class.java,
            TypedLong::class.java,
            TypedUuid::class.java,
        ),
        true,
    )
)
```

This prevents Spring Data from trying to create PersistentEntity for them.

---

## TypedValueElasticPersistentPropertyWithConverter

Persistent property with custom PropertyValueConverter for TypedValue fields.

```kotlin
class TypedValueElasticPersistentPropertyWithConverter(
    private val property: ElasticsearchPersistentProperty,
    private val typedIdEntityType: Class<*>,
    private val typedIdType: Class<*>,
    private val typedValueClass: Class<*>?
) : ElasticsearchPersistentProperty by property, PropertyValueConverter
```

### Converter Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `write` | `(Any) -> Any` | Converts TypedValue to raw ID for Elasticsearch storage |
| `read` | `(Any) -> Any` | Converts raw ID from Elasticsearch to TypedValue |
| `hasPropertyValueConverter` | `() -> Boolean` | Always returns `true` |
| `getPropertyValueConverter` | `() -> PropertyValueConverter` | Returns `this` |

---

## Configuration

### With Spring Boot

Register the custom mapping context as a Bean:

```kotlin
@Configuration
class ElasticsearchConfig {

    @Bean
    fun elasticsearchMappingContext(): TypedValueElasticsearchMappingContext {
        return TypedValueElasticsearchMappingContext()
    }
}
```

### With ElasticsearchConfigurationSupport

```kotlin
@Configuration
class ElasticsearchConfig : ElasticsearchConfigurationSupport() {

    @Bean
    override fun elasticsearchMappingContext(
        elasticsearchConverter: ElasticsearchCustomConversions
    ): SimpleElasticsearchMappingContext {
        return TypedValueElasticsearchMappingContext()
    }
}
```

---

## ID Type Conversion

### Write Conversion (TypedValue → Elasticsearch)

| ID Type | Elasticsearch Storage |
|---------|----------------------|
| `String` | Direct pass-through |
| `Long` | Direct pass-through |
| `Int` | Direct pass-through |
| `Short` | Direct pass-through |
| `Byte` | Direct pass-through |
| `Double` | Direct pass-through |
| `Float` | Direct pass-through |
| `Boolean` | Direct pass-through |
| `UUID` | `toString()` (String) |
| Other types | `toString()` (String) |

### Read Conversion (Elasticsearch → TypedValue)

| Target ID Type | Conversion |
|----------------|------------|
| `String` | `value.toString()` |
| `Long` | `Number.toLong()` or `String.toLong()` |
| `Int` | `Number.toInt()` or `String.toInt()` |
| `Short` | `Number.toShort()` or `String.toShort()` |
| `Byte` | `Number.toByte()` or `String.toByte()` |
| `Double` | `Number.toDouble()` or `String.toDouble()` |
| `Float` | `Number.toFloat()` or `String.toFloat()` |
| `Boolean` | Direct `Boolean` or `String.toBoolean()` |
| `UUID` | Direct `UUID` or `UUID.fromString()` |

---

## Supported Field Types

### Single Fields

All TypedValue types are supported:

```kotlin
@Document(indexName = "users")
data class UserDocument(
    @Id
    val id: TypedString<User>,

    val managerId: TypedUuid<User>?,

    val departmentId: TypedLong<Department>,

    val rankId: TypedInt<Rank>
)
```

### Collection Fields

**Only `List<TypedValue>` is supported:**

```kotlin
@Document(indexName = "teams")
data class TeamDocument(
    @Id
    val id: TypedString<Team>,

    val name: String,

    // Supported
    val memberIds: List<TypedString<User>>,

    val projectIds: List<TypedLong<Project>>
)
```

---

## Restrictions

::: danger Unsupported Collection Types
The following will throw `UnsupportedOperationException` at startup:
:::

### Arrays NOT Supported

```kotlin
// Will throw UnsupportedOperationException
@Document(indexName = "invalid")
data class InvalidDocument(
    @Id
    val id: TypedString<User>,

    // Arrays of TypedValue are not supported
    val memberIds: Array<TypedString<User>>  // Error!
)
```

**Error message:** `"Arrays of TypedValue are not supported. Caused by property: InvalidDocument#memberIds"`

### Sets NOT Supported

```kotlin
// Will throw UnsupportedOperationException
@Document(indexName = "invalid")
data class InvalidDocument(
    @Id
    val id: TypedString<User>,

    // Sets of TypedValue are not supported
    val memberIds: Set<TypedString<User>>  // Error!
)
```

**Error message:** `"Sets of TypedValue are not supported. Caused by property: InvalidDocument#memberIds"`

### Wildcard Types NOT Supported

```kotlin
// Will throw UnsupportedOperationException
@Document(indexName = "invalid")
data class InvalidDocument(
    @Id
    val id: TypedString<*>  // Error! Cannot resolve entity type
)
```

**Error message:** `"Actual type of TypedValue could not be resolved for property: InvalidDocument#id"`

---

## Document Mapping

### Basic Document

```kotlin
@Document(indexName = "users")
data class UserDocument(
    @Id
    val id: TypedString<User>,

    val name: String,
    val email: String,
    val createdAt: Instant
)
```

### With Field Type Hints

```kotlin
@Document(indexName = "users")
data class UserDocument(
    @Id
    @Field(type = FieldType.Keyword)
    val id: TypedString<User>,

    @Field(type = FieldType.Text)
    val name: String,

    @Field(type = FieldType.Keyword)
    val email: String
)
```

### Reference Fields

Store references to other documents:

```kotlin
@Document(indexName = "orders")
data class OrderDocument(
    @Id
    val id: TypedUuid<Order>,

    // Reference to user document
    val customerId: TypedString<User>,

    // References to product documents
    val productIds: List<TypedLong<Product>>,

    val total: BigDecimal,
    val status: OrderStatus
)
```

---

## Index Mapping

The mapping context handles field types automatically:

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "customerId": { "type": "keyword" },
      "productIds": { "type": "long" },
      "total": { "type": "double" },
      "status": { "type": "keyword" }
    }
  }
}
```

---

## Repository Usage

Use with Spring Data Elasticsearch repositories:

```kotlin
interface UserDocumentRepository : ElasticsearchRepository<UserDocument, TypedString<User>> {

    fun findByEmail(email: String): UserDocument?

    fun findByNameContaining(name: String): List<UserDocument>
}
```

```kotlin
@Service
class UserSearchService(private val userRepository: UserDocumentRepository) {

    fun findUser(id: TypedString<User>): UserDocument? {
        return userRepository.findById(id).orElse(null)
    }

    fun saveUser(user: UserDocument): UserDocument {
        return userRepository.save(user)
    }

    fun searchByName(query: String): List<UserDocument> {
        return userRepository.findByNameContaining(query)
    }
}
```

---

## Custom Queries

Use the Elasticsearch query builders:

```kotlin
@Service
class AdvancedSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    fun searchUsers(teamId: TypedString<Team>): List<UserDocument> {
        val query = NativeQuery.builder()
            .withQuery { q ->
                q.term { t ->
                    t.field("teamId")
                        .value(teamId.value)  // Extract raw value
                }
            }
            .build()

        return elasticsearchOperations
            .search(query, UserDocument::class.java)
            .map { it.content }
            .toList()
    }
}
```

---

## Nested Documents

TypedValue works in nested documents:

```kotlin
data class OrderItem(
    val productId: TypedLong<Product>,
    val quantity: Int,
    val price: BigDecimal
)

@Document(indexName = "orders")
data class OrderDocument(
    @Id
    val id: TypedUuid<Order>,

    val customerId: TypedString<User>,

    @Field(type = FieldType.Nested)
    val items: List<OrderItem>
)
```

---

## Bulk Operations

TypedValue works with bulk operations:

```kotlin
@Service
class BulkIndexService(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    fun indexUsers(users: List<UserDocument>) {
        val queries = users.map { user ->
            IndexQuery().apply {
                id = user.id.value  // Extract raw value for ID
                `object` = user
            }
        }

        elasticsearchOperations.bulkIndex(
            queries,
            IndexCoordinates.of("users")
        )
    }
}
```

---

## Type Resolution

The mapping context resolves TypedValue generic types from:

1. **Field declarations** - Direct TypedValue fields
2. **Collection element types** - `List<TypedValue<ID, T>>`
3. **Nested class fields** - TypedValue in nested data classes

### Convenience Type Resolution

For convenience types (TypedString, TypedLong, TypedInt, TypedUuid), the ID type is known from the class:

| Convenience Type | ID Type |
|-----------------|---------|
| `TypedString<T>` | `String` |
| `TypedLong<T>` | `Long` |
| `TypedInt<T>` | `Int` |
| `TypedUuid<T>` | `UUID` |

### Generic TypedValue Resolution

For generic `TypedValue<ID, T>`, both ID and entity type are resolved from generic parameters.

::: tip
Always use concrete types in your documents. Avoid `TypedValue<*, *>` which loses type information.
:::

---

## Error Handling

Invalid configurations are caught at startup:

```kotlin
// This will throw at startup
@Document(indexName = "invalid")
data class InvalidDocument(
    @Id
    val id: TypedString<User>,

    // Error: Sets not supported
    val memberIds: Set<TypedString<User>>  // Will fail!
)
```

---

## Performance Considerations

- TypedValue fields add minimal overhead
- Conversion happens only during serialization/deserialization
- No runtime reflection for basic operations
- Index operations use raw values directly
- PropertyValueConverter is resolved once per property

---

## Complete Example

```kotlin
@Configuration
class ElasticsearchConfig {
    @Bean
    fun elasticsearchMappingContext() = TypedValueElasticsearchMappingContext()
}

// Domain classes
class User
class Product
class Order

// Document
@Document(indexName = "orders")
data class OrderDocument(
    @Id
    val id: TypedUuid<Order>,

    @Field(type = FieldType.Keyword)
    val customerId: TypedString<User>,

    @Field(type = FieldType.Long)
    val productIds: List<TypedLong<Product>>,

    @Field(type = FieldType.Double)
    val total: BigDecimal,

    @Field(type = FieldType.Date)
    val createdAt: Instant
)

// Repository
interface OrderDocumentRepository : ElasticsearchRepository<OrderDocument, TypedUuid<Order>> {
    fun findByCustomerId(customerId: TypedString<User>): List<OrderDocument>
}

// Service
@Service
class OrderSearchService(private val orderRepository: OrderDocumentRepository) {

    fun findById(id: TypedUuid<Order>): OrderDocument? =
        orderRepository.findById(id).orElse(null)

    fun findByCustomer(customerId: TypedString<User>): List<OrderDocument> =
        orderRepository.findByCustomerId(customerId)

    fun save(order: OrderDocument): OrderDocument =
        orderRepository.save(order)
}
```

---

## API Summary

| Class | Purpose |
|-------|---------|
| `TypedValueElasticsearchMappingContext` | Custom mapping context for TypedValue support |
| `TypedValueElasticPersistentPropertyWithConverter` | Property converter for TypedValue fields |

### Supported Types

| Collection Type | Supported |
|-----------------|-----------|
| `TypedValue<ID, T>` (single) | Yes |
| `List<TypedValue>` | Yes |
| `Set<TypedValue>` | No |
| `Array<TypedValue>` | No |

## Next Steps

- [Jackson Integration](/integrations/jackson) - JSON serialization
- [Spring Integration](/integrations/spring) - Web framework support

