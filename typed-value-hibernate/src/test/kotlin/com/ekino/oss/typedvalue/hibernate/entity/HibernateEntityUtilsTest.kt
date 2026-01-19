/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue.hibernate.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hibernate.proxy.HibernateProxy
import org.hibernate.proxy.LazyInitializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HibernateEntityUtilsTest {

  @BeforeEach
  fun setUp() {
    mockkStatic(HibernateProxy::class)
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic(HibernateProxy::class)
  }

  @Nested
  inner class GetEffectiveClass {

    @Test
    fun `should return actual class for non-proxy object`() {
      val entity = TestEntity("123")
      every { HibernateProxy.extractLazyInitializer(entity) } returns null

      val result = HibernateEntityUtils.getEffectiveClass(entity)

      assertThat(result).isEqualTo(TestEntity::class.java)
    }

    @Test
    fun `should return persistent class for proxy object`() {
      val proxy = mockk<HibernateProxy>()
      val lazyInitializer = mockk<LazyInitializer>()
      every { HibernateProxy.extractLazyInitializer(proxy) } returns lazyInitializer
      every { lazyInitializer.persistentClass } returns TestEntity::class.java

      val result = HibernateEntityUtils.getEffectiveClass(proxy)

      assertThat(result).isEqualTo(TestEntity::class.java)
    }
  }

  @Nested
  inner class EntityHashCode {

    @Test
    fun `should return class hashCode for non-proxy object`() {
      val entity = TestEntity("123")
      every { HibernateProxy.extractLazyInitializer(entity) } returns null

      val result = HibernateEntityUtils.entityHashCode(entity)

      assertThat(result).isEqualTo(TestEntity::class.java.hashCode())
    }

    @Test
    fun `should return persistent class hashCode for proxy object`() {
      val proxy = mockk<HibernateProxy>()
      val lazyInitializer = mockk<LazyInitializer>()
      every { HibernateProxy.extractLazyInitializer(proxy) } returns lazyInitializer
      every { lazyInitializer.persistentClass } returns TestEntity::class.java

      val result = HibernateEntityUtils.entityHashCode(proxy)

      assertThat(result).isEqualTo(TestEntity::class.java.hashCode())
    }
  }

  @Nested
  inner class EntityEquals {

    @Test
    fun `should return true for same instance`() {
      val entity = TestEntity("123")

      val result = HibernateEntityUtils.entityEquals(entity, entity) { it.id }

      assertThat(result).isTrue()
    }

    @Test
    fun `should return false when other is null`() {
      val entity = TestEntity("123")
      every { HibernateProxy.extractLazyInitializer(entity) } returns null

      val result = HibernateEntityUtils.entityEquals(entity, null) { it.id }

      assertThat(result).isFalse()
    }

    @Test
    fun `should return false when classes are different`() {
      val entity1 = TestEntity("123")
      val entity2 = OtherEntity("123")
      every { HibernateProxy.extractLazyInitializer(entity1) } returns null
      every { HibernateProxy.extractLazyInitializer(entity2) } returns null

      val result = HibernateEntityUtils.entityEquals(entity1, entity2) { it.id }

      assertThat(result).isFalse()
    }

    @Test
    fun `should return false when id is null`() {
      val entity1 = TestEntity(null)
      val entity2 = TestEntity(null)
      every { HibernateProxy.extractLazyInitializer(entity1) } returns null
      every { HibernateProxy.extractLazyInitializer(entity2) } returns null

      val result = HibernateEntityUtils.entityEquals(entity1, entity2) { it.id }

      assertThat(result).isFalse()
    }

    @Test
    fun `should return true when same class and same non-null id`() {
      val entity1 = TestEntity("123")
      val entity2 = TestEntity("123")
      every { HibernateProxy.extractLazyInitializer(entity1) } returns null
      every { HibernateProxy.extractLazyInitializer(entity2) } returns null

      val result = HibernateEntityUtils.entityEquals(entity1, entity2) { it.id }

      assertThat(result).isTrue()
    }

    @Test
    fun `should return false when same class but different ids`() {
      val entity1 = TestEntity("123")
      val entity2 = TestEntity("456")
      every { HibernateProxy.extractLazyInitializer(entity1) } returns null
      every { HibernateProxy.extractLazyInitializer(entity2) } returns null

      val result = HibernateEntityUtils.entityEquals(entity1, entity2) { it.id }

      assertThat(result).isFalse()
    }
  }

  private data class TestEntity(val id: String?)

  private data class OtherEntity(val id: String?)
}
