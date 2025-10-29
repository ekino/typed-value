/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
import { describe, test, expect } from 'vitest'
import { createTypedLong, TypedLong } from '@ekino/typed-value'

// Define entity marker types for type safety
interface Order {}
interface Invoice {}
interface Offset {}
interface LargeId {}

describe('TypedLong', () => {
  describe('Creation', () => {
    test('should create a TypedLong with value', () => {
      const orderId = createTypedLong<Order>(123456789)

      expect(orderId.value).toBe(123456789)
    })

    test('should handle zero value', () => {
      const zeroId = createTypedLong<Order>(0)

      expect(zeroId.value).toBe(0)
    })

    test('should handle negative value', () => {
      const negativeId = createTypedLong<Offset>(-123456789)

      expect(negativeId.value).toBe(-123456789)
    })

    test('should handle large values within safe integer range', () => {
      // JavaScript safe integer max: 2^53 - 1 = 9007199254740991
      const largeId = createTypedLong<LargeId>(9007199254740991)

      expect(largeId.value).toBe(9007199254740991)
    })
  })

  describe('toString', () => {
    test('should return the value as string', () => {
      const orderId = createTypedLong<Order>(123456789)

      expect(orderId.toString()).toBe('123456789')
    })

    test('should handle negative values in toString', () => {
      const negativeId = createTypedLong<Offset>(-5)

      expect(negativeId.toString()).toBe('-5')
    })
  })

  describe('Equality', () => {
    test('should be equal when values match', () => {
      const id1 = createTypedLong<Order>(123456789)
      const id2 = createTypedLong<Order>(123456789)

      expect(id1.equals(id2)).toBe(true)
    })

    test('should not be equal when values differ', () => {
      const id1 = createTypedLong<Order>(123456789)
      const id2 = createTypedLong<Order>(987654321)

      expect(id1.equals(id2)).toBe(false)
    })

    test('should be equal for same value regardless of type parameter (runtime)', () => {
      // Note: At runtime, type parameters are erased. Equality is based on value only.
      // Type safety is enforced at compile-time via TypeScript.
      const orderId = createTypedLong<Order>(123)
      const invoiceId = createTypedLong<Invoice>(123)

      // At runtime, these are equal because values match
      expect(orderId.equals(invoiceId)).toBe(true)
    })

    test('should not be equal to null', () => {
      const orderId = createTypedLong<Order>(123456789)

      expect(orderId.equals(null)).toBe(false)
    })
  })

  describe('Comparison', () => {
    test('should compare by value', () => {
      const a = createTypedLong<Order>(1)
      const b = createTypedLong<Order>(2)

      expect(a.compareTo(b)).toBeLessThan(0)
      expect(b.compareTo(a)).toBeGreaterThan(0)
    })

    test('should return 0 for equal values', () => {
      const id1 = createTypedLong<Order>(123456789)
      const id2 = createTypedLong<Order>(123456789)

      expect(id1.compareTo(id2)).toBe(0)
    })
  })

  describe('Type Safety', () => {
    test('should have proper TypeScript types', () => {
      const orderId: TypedLong<Order> = createTypedLong<Order>(123456789)

      const value: number = orderId.value

      expect(typeof value).toBe('number')
    })

    test('should allow type inference with explicit generic', () => {
      // TypeScript infers the return type from the generic parameter
      const orderId = createTypedLong<Order>(123456789)
      const invoiceId = createTypedLong<Invoice>(987654321)

      // Both are different TypeScript types (TypedLong<Order> vs TypedLong<Invoice>)
      // but this is only enforced at compile-time

      expect(orderId.value).toBe(123456789)
      expect(invoiceId.value).toBe(987654321)
    })
  })

  describe('Hash Code', () => {
    test('should return consistent hashCode for same values', () => {
      const id1 = createTypedLong<Order>(123456789)
      const id2 = createTypedLong<Order>(123456789)

      expect(id1.hashCode()).toBe(id2.hashCode())
    })

    test('should return different hashCode for different values', () => {
      const id1 = createTypedLong<Order>(123456789)
      const id2 = createTypedLong<Order>(987654321)

      expect(id1.hashCode()).not.toBe(id2.hashCode())
    })
  })
})
