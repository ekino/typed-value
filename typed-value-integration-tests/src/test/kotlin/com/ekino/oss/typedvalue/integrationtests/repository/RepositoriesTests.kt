/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.integrationtests.repository

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.ekino.oss.typedvalue.TypedInt
import com.ekino.oss.typedvalue.TypedLong
import com.ekino.oss.typedvalue.TypedString
import com.ekino.oss.typedvalue.TypedUuid
import com.ekino.oss.typedvalue.integrationtests.AbstractIntegrationTest
import com.ekino.oss.typedvalue.integrationtests.model.IntPerson
import com.ekino.oss.typedvalue.integrationtests.model.LongPerson
import com.ekino.oss.typedvalue.integrationtests.model.Person
import com.ekino.oss.typedvalue.integrationtests.model.StringPerson
import com.ekino.oss.typedvalue.integrationtests.model.UuidPerson
import com.ekino.oss.typedvalue.toTypedInt
import com.ekino.oss.typedvalue.toTypedLong
import com.ekino.oss.typedvalue.toTypedString
import com.ekino.oss.typedvalue.toTypedUuid
import java.util.UUID
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for repositories with TypedValue IDs.
 *
 * Tests all abstract entity variants:
 * - Person (uses HibernateEntityUtils directly)
 * - IntPerson (extends AbstractIntEntity)
 * - LongPerson (extends AbstractLongEntity)
 * - UuidPerson (extends AbstractUuidEntity)
 * - StringPerson (extends AbstractStringEntity)
 */
class RepositoriesTests : AbstractIntegrationTest() {

  @Autowired private lateinit var personRepository: PersonRepository
  @Autowired private lateinit var intPersonRepository: IntPersonRepository
  @Autowired private lateinit var longPersonRepository: LongPersonRepository
  @Autowired private lateinit var uuidPersonRepository: UuidPersonRepository
  @Autowired private lateinit var stringPersonRepository: StringPersonRepository

  @Nested
  inner class PersonWithHibernateEntityUtils {

    @Test
    fun `should create and retrieve a person with all TypedValue converter fields`() {
      val uuidRef = UUID.randomUUID().toTypedUuid<Person>()
      val stringRef = "ref-string-123".toTypedString<Person>()
      val intRef = 42.toTypedInt<Person>()
      val longRef = 123456789L.toTypedLong<Person>()

      val person =
        Person().apply {
          name = "John Doe"
          someUuidRef = uuidRef
          someStringRef = stringRef
          someIntRef = intRef
          someLongRef = longRef
        }

      val savedPerson = personRepository.save(person)

      assertThat(savedPerson.id).isNotNull().given { personId ->
        assertThat(personRepository.getReferenceById(personId)).all {
          transform { it.id }
            .isNotNull()
            .all {
              transform { it.value }.isInstanceOf<UUID>()
              transform { it.type }.isEqualTo(Person::class)
            }
          transform { it.name }.isEqualTo("John Doe")
          transform { it.someUuidRef }.isEqualTo(uuidRef)
          transform { it.someStringRef }.isEqualTo(stringRef)
          transform { it.someIntRef }.isEqualTo(intRef)
          transform { it.someLongRef }.isEqualTo(longRef)
        }
      }
    }

    @Test
    fun `should persist and retrieve TypedUuid converter field`() {
      val uuidRef = UUID.randomUUID().toTypedUuid<Person>()
      val person = Person().apply { someUuidRef = uuidRef }

      val saved = personRepository.save(person)
      val found = personRepository.findById(saved.id!!).get()

      assertThat(found.someUuidRef).isNotNull().all {
        transform { it.value }.isEqualTo(uuidRef.value)
        transform { it.type }.isEqualTo(Person::class)
      }
    }

    @Test
    fun `should persist and retrieve TypedString converter field`() {
      val stringRef = "my-string-ref".toTypedString<Person>()
      val person = Person().apply { someStringRef = stringRef }

      val saved = personRepository.save(person)
      val found = personRepository.findById(saved.id!!).get()

      assertThat(found.someStringRef).isNotNull().all {
        transform { it.value }.isEqualTo(stringRef.value)
        transform { it.type }.isEqualTo(Person::class)
      }
    }

    @Test
    fun `should persist and retrieve TypedInt converter field`() {
      val intRef = 999.toTypedInt<Person>()
      val person = Person().apply { someIntRef = intRef }

      val saved = personRepository.save(person)
      val found = personRepository.findById(saved.id!!).get()

      assertThat(found.someIntRef).isNotNull().all {
        transform { it.value }.isEqualTo(intRef.value)
        transform { it.type }.isEqualTo(Person::class)
      }
    }

    @Test
    fun `should persist and retrieve TypedLong converter field`() {
      val longRef = 9876543210L.toTypedLong<Person>()
      val person = Person().apply { someLongRef = longRef }

      val saved = personRepository.save(person)
      val found = personRepository.findById(saved.id!!).get()

      assertThat(found.someLongRef).isNotNull().all {
        transform { it.value }.isEqualTo(longRef.value)
        transform { it.type }.isEqualTo(Person::class)
      }
    }

    @Test
    fun `should handle null converter fields`() {
      val person = Person().apply { name = "Null Fields" }

      val saved = personRepository.save(person)
      val found = personRepository.findById(saved.id!!).get()

      assertThat(found.someUuidRef).isEqualTo(null)
      assertThat(found.someStringRef).isEqualTo(null)
      assertThat(found.someIntRef).isEqualTo(null)
      assertThat(found.someLongRef).isEqualTo(null)
    }
  }

  @Nested
  inner class IntPersonWithAbstractIntEntity {

    @Test
    fun `should create and retrieve an IntPerson with auto-generated Int ID`() {
      val person = IntPerson().apply { name = "Int Person" }

      val savedPerson = intPersonRepository.save(person)

      assertThat(savedPerson.id).isNotNull().given { personId ->
        assertThat(personId).isInstanceOf<TypedInt<IntPerson>>()
        assertThat(personId.type).isEqualTo(IntPerson::class)

        assertThat(intPersonRepository.getReferenceById(personId)).all {
          transform { it.id }.isEqualTo(personId)
          transform { it.name }.isEqualTo("Int Person")
        }
      }
    }

    @Test
    fun `should find IntPerson by TypedInt ID`() {
      val person = IntPerson().apply { name = "Find By Int ID" }
      val savedPerson = intPersonRepository.save(person)

      val found = intPersonRepository.findById(savedPerson.id!!)

      assertThat(found.isPresent).isEqualTo(true)
      assertThat(found.get().name).isEqualTo("Find By Int ID")
    }
  }

  @Nested
  inner class LongPersonWithAbstractLongEntity {

    @Test
    fun `should create and retrieve a LongPerson with auto-generated Long ID`() {
      val person = LongPerson().apply { name = "Long Person" }

      val savedPerson = longPersonRepository.save(person)

      assertThat(savedPerson.id).isNotNull().given { personId ->
        assertThat(personId).isInstanceOf<TypedLong<LongPerson>>()
        assertThat(personId.type).isEqualTo(LongPerson::class)

        assertThat(longPersonRepository.getReferenceById(personId)).all {
          transform { it.id }.isEqualTo(personId)
          transform { it.name }.isEqualTo("Long Person")
        }
      }
    }

    @Test
    fun `should find LongPerson by TypedLong ID`() {
      val person = LongPerson().apply { name = "Find By Long ID" }
      val savedPerson = longPersonRepository.save(person)

      val found = longPersonRepository.findById(savedPerson.id!!)

      assertThat(found.isPresent).isEqualTo(true)
      assertThat(found.get().name).isEqualTo("Find By Long ID")
    }
  }

  @Nested
  inner class UuidPersonWithAbstractUuidEntity {

    @Test
    fun `should create and retrieve a UuidPerson with auto-generated UUID ID`() {
      val person = UuidPerson().apply { name = "Uuid Person" }

      val savedPerson = uuidPersonRepository.save(person)

      assertThat(savedPerson.id).isNotNull().given { personId ->
        assertThat(personId).isInstanceOf<TypedUuid<UuidPerson>>()
        assertThat(personId.type).isEqualTo(UuidPerson::class)

        assertThat(uuidPersonRepository.getReferenceById(personId)).all {
          transform { it.id }.isEqualTo(personId)
          transform { it.name }.isEqualTo("Uuid Person")
        }
      }
    }

    @Test
    fun `should find UuidPerson by TypedUuid ID`() {
      val person = UuidPerson().apply { name = "Find By Uuid ID" }
      val savedPerson = uuidPersonRepository.save(person)

      val found = uuidPersonRepository.findById(savedPerson.id!!)

      assertThat(found.isPresent).isEqualTo(true)
      assertThat(found.get().name).isEqualTo("Find By Uuid ID")
    }
  }

  @Nested
  inner class StringPersonWithAbstractStringEntity {

    @Test
    fun `should create and retrieve a StringPerson with auto-generated String ID`() {
      val person = StringPerson().apply { name = "String Person" }

      val savedPerson = stringPersonRepository.save(person)

      assertThat(savedPerson.id).isNotNull().given { personId ->
        assertThat(personId).isInstanceOf<TypedString<StringPerson>>()
        assertThat(personId.type).isEqualTo(StringPerson::class)

        assertThat(stringPersonRepository.getReferenceById(personId)).all {
          transform { it.id }.isEqualTo(personId)
          transform { it.name }.isEqualTo("String Person")
        }
      }
    }

    @Test
    fun `should find StringPerson by TypedString ID`() {
      StringPerson().apply {
        name = "Find By String ID"
        id = "pipo".toTypedString()
      }
      val person = StringPerson().apply { name = "Find By String ID" }
      val savedPerson = stringPersonRepository.save(person)

      val found = stringPersonRepository.findById(savedPerson.id!!)

      assertThat(found.isPresent).isEqualTo(true)
      assertThat(found.get().name).isEqualTo("Find By String ID")
    }
  }
}
