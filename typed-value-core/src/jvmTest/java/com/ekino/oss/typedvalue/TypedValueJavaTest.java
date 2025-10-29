/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.typedvalue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import kotlin.jvm.JvmClassMappingKt;
import org.junit.jupiter.api.Test;

/**
 * Java interoperability tests for TypedValue.
 *
 * <p>Note: IntelliJ may show false positive type errors due to Kotlin multiplatform type inference
 * issues. These are IDE inspection warnings only - the code compiles and runs correctly.
 */
public class TypedValueJavaTest {

  private static class User {}

  private static class Product {}

  private static class Counter {}

  private static class Order {}

  @Test
  public void shouldCreateTypedString() {
    TypedString<User> userId = TypedValues.typedString("user-123", User.class);
    String id = userId.getValue();
    assertThat(userId.getValue()).isEqualTo("user-123");
    assertThat(userId.getType()).isEqualTo(JvmClassMappingKt.getKotlinClass(User.class));
  }

  @Test
  public void shouldCreateTypedLong() {
    TypedLong<Product> productId = TypedValues.typedLong(42L, Product.class);

    assertThat(productId.getValue()).isEqualTo(42L);
    assertThat(productId.getType()).isEqualTo(JvmClassMappingKt.getKotlinClass(Product.class));
  }

  @Test
  public void shouldCreateTypedInt() {
    TypedInt<Counter> counterId = TypedValues.typedInt(100, Counter.class);

    assertThat(counterId.getValue()).isEqualTo(100);
    assertThat(counterId.getType()).isEqualTo(JvmClassMappingKt.getKotlinClass(Counter.class));
  }

  @Test
  public void shouldCreateTypedUuid() {
    UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    TypedUuid<Order> orderId = TypedValues.typedUuid(uuid, Order.class);

    assertThat(orderId.getValue()).isEqualTo(uuid);
    assertThat(orderId.getType()).isEqualTo(JvmClassMappingKt.getKotlinClass(Order.class));
  }

  @Test
  public void shouldCreateGenericTypedValue() {
    TypedValue<String, User> userId = TypedValues.typedValue("user-456", User.class);

    assertThat(userId.getValue()).isEqualTo("user-456");
    assertThat(userId.getType()).isEqualTo(JvmClassMappingKt.getKotlinClass(User.class));
  }

  @Test
  public void shouldBeAssignableToTypedValueInterface() {
    // Convenience types should be assignable to TypedValue
    TypedValue<String, User> stringId = TypedValues.typedString("user-123", User.class);
    TypedValue<Long, Product> longId = TypedValues.typedLong(42L, Product.class);
    TypedValue<Integer, Counter> intId = TypedValues.typedInt(100, Counter.class);
    TypedValue<UUID, Order> uuidId = TypedValues.typedUuid(UUID.randomUUID(), Order.class);

    assertThat(stringId).isInstanceOf(TypedString.class);
    assertThat(longId).isInstanceOf(TypedLong.class);
    assertThat(intId).isInstanceOf(TypedInt.class);
    assertThat(uuidId).isInstanceOf(TypedUuid.class);
  }
}
