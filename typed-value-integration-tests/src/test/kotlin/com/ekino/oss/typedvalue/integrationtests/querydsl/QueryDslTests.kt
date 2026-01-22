/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.querydsl

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.integrationtests.AbstractIntegrationTest
import com.ekino.oss.typedvalue.integrationtests.model.IntPerson
import com.ekino.oss.typedvalue.integrationtests.model.LongPerson
import com.ekino.oss.typedvalue.integrationtests.model.Person
import com.ekino.oss.typedvalue.integrationtests.model.QIntPerson
import com.ekino.oss.typedvalue.integrationtests.model.QLongPerson
import com.ekino.oss.typedvalue.integrationtests.model.QPerson
import com.ekino.oss.typedvalue.integrationtests.model.QStringPerson
import com.ekino.oss.typedvalue.integrationtests.model.QUuidPerson
import com.ekino.oss.typedvalue.integrationtests.model.StringPerson
import com.ekino.oss.typedvalue.integrationtests.model.UuidPerson
import com.ekino.oss.typedvalue.integrationtests.repository.IntPersonRepository
import com.ekino.oss.typedvalue.integrationtests.repository.LongPersonRepository
import com.ekino.oss.typedvalue.integrationtests.repository.PersonRepository
import com.ekino.oss.typedvalue.integrationtests.repository.StringPersonRepository
import com.ekino.oss.typedvalue.integrationtests.repository.UuidPersonRepository
import com.ekino.oss.typedvalue.querydsl.TypedValueExpression.Companion.typedValueExpressionOf
import com.ekino.oss.typedvalue.toTypedUuid
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.test.Test
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for QueryDSL support with TypedValue.
 *
 * Tests all entity variants:
 * - Person (uses HibernateEntityUtils directly)
 * - IntPerson (extends AbstractIntEntity)
 * - LongPerson (extends AbstractLongEntity)
 * - UuidPerson (extends AbstractUuidEntity)
 * - StringPerson (extends AbstractStringEntity)
 */
class QueryDslTests : AbstractIntegrationTest() {

  @Autowired lateinit var entityManager: EntityManager

  @Autowired private lateinit var personRepository: PersonRepository
  @Autowired private lateinit var intPersonRepository: IntPersonRepository
  @Autowired private lateinit var longPersonRepository: LongPersonRepository
  @Autowired private lateinit var uuidPersonRepository: UuidPersonRepository
  @Autowired private lateinit var stringPersonRepository: StringPersonRepository

  val queryFactory: JPAQueryFactory by lazy { JPAQueryFactory(entityManager) }

  private val faker: Faker by lazy { Faker() }

  private lateinit var personIds: Set<TypedUuid<Person>>
  private lateinit var intPersonIds: Set<TypedInt<IntPerson>>
  private lateinit var longPersonIds: Set<TypedLong<LongPerson>>
  private lateinit var uuidPersonIds: Set<TypedUuid<UuidPerson>>
  private lateinit var stringPersonIds: Set<TypedString<StringPerson>>

  @BeforeAll
  fun setup() {
    personIds =
      List(10) {
          Person().apply {
            name = faker.name().fullName()
            someUuidRef = UUID.randomUUID().toTypedUuid<Person>()
          }
        }
        .also { personRepository.saveAll(it) }
        .mapNotNull { it.id }
        .toSet()

    intPersonIds =
      List(10) { IntPerson().apply { name = faker.name().fullName() } }
        .also { intPersonRepository.saveAll(it) }
        .mapNotNull { it.id }
        .toSet()

    longPersonIds =
      List(10) { LongPerson().apply { name = faker.name().fullName() } }
        .also { longPersonRepository.saveAll(it) }
        .mapNotNull { it.id }
        .toSet()

    uuidPersonIds =
      List(10) { UuidPerson().apply { name = faker.name().fullName() } }
        .also { uuidPersonRepository.saveAll(it) }
        .mapNotNull { it.id }
        .toSet()

    stringPersonIds =
      List(10) { StringPerson().apply { name = faker.name().fullName() } }
        .also { stringPersonRepository.saveAll(it) }
        .mapNotNull { it.id }
        .toSet()
  }

  @Nested
  inner class PersonWithHibernateEntityUtils {
    private val personIdExpr = QPerson.person.typedValueExpressionOf { it._id }

    @Test
    fun `should find by TypedUuid`() {
      val personId = personIds.random()

      val result =
        queryFactory
          .select(QPerson.person)
          .from(QPerson.person)
          .where(personIdExpr.eq(personId))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.id }.isEqualTo(personId)
    }

    @Test
    fun `should find by TypedUuid in list`() {
      val ids = personIds.shuffled().take(3)

      val results =
        queryFactory
          .select(QPerson.person)
          .from(QPerson.person)
          .where(personIdExpr.isIn(ids))
          .fetch()

      assertThat(results.map { it.id }).isNotNull().containsOnly(*ids.toTypedArray())
    }

    @Test
    fun `should find all persons with isNotNull`() {
      val expectedPersons = personIds.size
      val results =
        queryFactory
          .select(QPerson.person)
          .from(QPerson.person)
          .where(personIdExpr.isNotNull())
          .fetch()

      assertThat(results).hasSize(expectedPersons)
    }

    @Test
    fun `should find person by someUuidRef`() {
      val person = personRepository.findById(personIds.random()).getOrNull()!!
      val result =
        queryFactory
          .select(QPerson.person)
          .from(QPerson.person)
          .where(QPerson.person.someUuidRef.eq(person.someUuidRef!!))
          .fetchOne()

      assertThat(result).isNotNull().isEqualTo(person)
    }

    @Test
    fun `should select into TypedValue`() {
      val person = personRepository.findById(personIds.random()).getOrNull()!!
      val personNameExpr = QPerson.person.typedValueExpressionOf { it.name }
      val result =
        queryFactory
          .select(personNameExpr)
          .from(QPerson.person)
          .where(QPerson.person.someUuidRef.eq(person.someUuidRef!!))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.value }.isEqualTo(person.name)
    }

    @Test
    fun `should select into TypedString`() {
      val person = personRepository.findById(personIds.random()).getOrNull()!!
      val personNameExpr =
        QPerson.person.name.typedValueExpressionOf { id -> TypedString.of(id, Person::class) }
      val result =
        queryFactory
          .select(personNameExpr)
          .from(QPerson.person)
          .where(QPerson.person.someUuidRef.eq(person.someUuidRef!!))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.value }.isEqualTo(person.name)
    }

    @Test
    fun `should select into custom TypedValueTyped`() {
      class PersonNameTyped(id: String) : TypedString<Person>(id, Person::class)

      val person = personRepository.findById(personIds.random()).getOrNull()!!
      val personNameExpr = QPerson.person.name.typedValueExpressionOf { id -> PersonNameTyped(id) }
      val result =
        queryFactory
          .select(personNameExpr)
          .from(QPerson.person)
          .where(QPerson.person.someUuidRef.eq(person.someUuidRef!!))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.value }.isEqualTo(person.name)
    }
  }

  @Nested
  inner class IntPersonWithAbstractIntEntity {
    private val intPersonIdExpr = QIntPerson.intPerson.typedValueExpressionOf { it._id }

    @Test
    fun `should find by TypedInt`() {
      val personId = intPersonIds.random()

      val result =
        queryFactory
          .select(QIntPerson.intPerson)
          .from(QIntPerson.intPerson)
          .where(intPersonIdExpr.eq(personId))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.id }.isEqualTo(personId)
    }

    @Test
    fun `should find by TypedInt in list`() {
      val ids = intPersonIds.shuffled().take(3)

      val results =
        queryFactory
          .select(QIntPerson.intPerson)
          .from(QIntPerson.intPerson)
          .where(intPersonIdExpr.isIn(ids))
          .fetch()

      assertThat(results.map { it.id }).isNotNull().containsOnly(*ids.toTypedArray())
    }

    @Test
    fun `should find all IntPersons with isNotNull`() {
      val expectedPersons = intPersonIds.size
      val results =
        queryFactory
          .select(QIntPerson.intPerson)
          .from(QIntPerson.intPerson)
          .where(intPersonIdExpr.isNotNull())
          .fetch()

      assertThat(results).hasSize(expectedPersons)
    }
  }

  @Nested
  inner class LongPersonWithAbstractLongEntity {
    private val longPersonIdExpr = QLongPerson.longPerson.typedValueExpressionOf { it._id }

    @Test
    fun `should find by TypedLong`() {
      val personId = longPersonIds.random()

      val result =
        queryFactory
          .select(QLongPerson.longPerson)
          .from(QLongPerson.longPerson)
          .where(longPersonIdExpr.eq(personId))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.id }.isEqualTo(personId)
    }

    @Test
    fun `should find by TypedLong in list`() {
      val ids = longPersonIds.shuffled().take(3)

      val results =
        queryFactory
          .select(QLongPerson.longPerson)
          .from(QLongPerson.longPerson)
          .where(longPersonIdExpr.isIn(ids))
          .fetch()

      assertThat(results.map { it.id }).isNotNull().containsOnly(*ids.toTypedArray())
    }

    @Test
    fun `should find all LongPersons with isNotNull`() {
      val expectedPersons = longPersonIds.size
      val results =
        queryFactory
          .select(QLongPerson.longPerson)
          .from(QLongPerson.longPerson)
          .where(longPersonIdExpr.isNotNull())
          .fetch()

      assertThat(results).hasSize(expectedPersons)
    }
  }

  @Nested
  inner class UuidPersonWithAbstractUuidEntity {
    private val uuidPersonIdExpr = QUuidPerson.uuidPerson.typedValueExpressionOf { it._id }

    @Test
    fun `should find by TypedUuid`() {
      val personId = uuidPersonIds.random()

      val result =
        queryFactory
          .select(QUuidPerson.uuidPerson)
          .from(QUuidPerson.uuidPerson)
          .where(uuidPersonIdExpr.eq(personId))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.id }.isEqualTo(personId)
    }

    @Test
    fun `should find by TypedUuid in list`() {
      val ids = uuidPersonIds.shuffled().take(3)

      val results =
        queryFactory
          .select(QUuidPerson.uuidPerson)
          .from(QUuidPerson.uuidPerson)
          .where(uuidPersonIdExpr.isIn(ids))
          .fetch()

      assertThat(results.map { it.id }).isNotNull().containsOnly(*ids.toTypedArray())
    }

    @Test
    fun `should find all UuidPersons with isNotNull`() {
      val expectedPersons = uuidPersonIds.size
      val results =
        queryFactory
          .select(QUuidPerson.uuidPerson)
          .from(QUuidPerson.uuidPerson)
          .where(uuidPersonIdExpr.isNotNull())
          .fetch()

      assertThat(results).hasSize(expectedPersons)
    }
  }

  @Nested
  inner class StringPersonWithAbstractStringEntity {
    private val stringPersonIdExpr = QStringPerson.stringPerson.typedValueExpressionOf { it._id }

    @Test
    fun `should find by TypedString`() {
      val personId = stringPersonIds.random()

      val result =
        queryFactory
          .select(QStringPerson.stringPerson)
          .from(QStringPerson.stringPerson)
          .where(stringPersonIdExpr.eq(personId))
          .fetchOne()

      assertThat(result).isNotNull().transform { it.id }.isEqualTo(personId)
    }

    @Test
    fun `should find by TypedString in list`() {
      val ids = stringPersonIds.shuffled().take(3)

      val results =
        queryFactory
          .select(QStringPerson.stringPerson)
          .from(QStringPerson.stringPerson)
          .where(stringPersonIdExpr.isIn(ids))
          .fetch()

      assertThat(results.map { it.id }).isNotNull().containsOnly(*ids.toTypedArray())
    }

    @Test
    fun `should find all StringPersons with isNotNull`() {
      val expectedPersons = stringPersonIds.size
      val results =
        queryFactory
          .select(QStringPerson.stringPerson)
          .from(QStringPerson.stringPerson)
          .where(stringPersonIdExpr.isNotNull())
          .fetch()

      assertThat(results).hasSize(expectedPersons)
    }
  }
}
