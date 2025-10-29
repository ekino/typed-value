/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
import { describe, test, expect } from 'vitest'
import { createTypedInt, TypedInt } from '@ekino/typed-value'

// Define entity marker types for type safety
interface Product {}
interface Category {}
interface Temperature {}
interface MaxInt {}
interface MinInt {}
interface Offset {}

describe('TypedInt', () => {
  describe('Creation', () => {
    test('should create a TypedInt with value', () => {
      const productId = createTypedInt<Product>(42)

      expect(productId.value).toBe(42)
    })

    test('should handle zero value', () => {
      const zeroId = createTypedInt<Product>(0)

      expect(zeroId.value).toBe(0)
    })

    test('should handle negative value', () => {
      const negativeId = createTypedInt<Temperature>(-1)

      expect(negativeId.value).toBe(-1)
    })

    test('should handle max int value', () => {
      const maxId = createTypedInt<MaxInt>(2147483647)

      expect(maxId.value).toBe(2147483647)
    })

    test('should handle min int value', () => {
      const minId = createTypedInt<MinInt>(-2147483648)

      expect(minId.value).toBe(-2147483648)
    })
  })

  describe('toString', () => {
    test('should return the value as string', () => {
      const productId = createTypedInt<Product>(42)

      expect(productId.toString()).toBe('42')
    })

    test('should handle negative values in toString', () => {
      const negativeId = createTypedInt<Offset>(-5)

      expect(negativeId.toString()).toBe('-5')
    })
  })

  describe('Equality', () => {
    test('should be equal when values match', () => {
      const id1 = createTypedInt<Product>(42)
      const id2 = createTypedInt<Product>(42)

      expect(id1.equals(id2)).toBe(true)
    })

    test('should not be equal when values differ', () => {
      const id1 = createTypedInt<Product>(42)
      const id2 = createTypedInt<Product>(43)

      expect(id1.equals(id2)).toBe(false)
    })

    test('should be equal for same value regardless of type parameter (runtime)', () => {
      // Note: At runtime, type parameters are erased. Equality is based on value only.
      // Type safety is enforced at compile-time via TypeScript.
      const productId = createTypedInt<Product>(42)
      const categoryId = createTypedInt<Category>(42)

      // At runtime, these are equal because values match
      expect(productId.equals(categoryId)).toBe(true)
    })

    test('should not be equal to null', () => {
      const productId = createTypedInt<Product>(42)

      expect(productId.equals(null)).toBe(false)
    })
  })

  describe('Comparison', () => {
    test('should compare by value', () => {
      const a = createTypedInt<Product>(1)
      const b = createTypedInt<Product>(2)

      expect(a.compareTo(b)).toBeLessThan(0)
      expect(b.compareTo(a)).toBeGreaterThan(0)
    })

    test('should return 0 for equal values', () => {
      const id1 = createTypedInt<Product>(42)
      const id2 = createTypedInt<Product>(42)

      expect(id1.compareTo(id2)).toBe(0)
    })
  })

  describe('Type Safety', () => {
    test('should have proper TypeScript types', () => {
      const productId: TypedInt<Product> = createTypedInt<Product>(42)

      const value: number = productId.value

      expect(typeof value).toBe('number')
    })

    test('should allow type inference with explicit generic', () => {
      // TypeScript infers the return type from the generic parameter
      const productId = createTypedInt<Product>(42)
      const categoryId = createTypedInt<Category>(100)

      // Both are different TypeScript types (TypedInt<Product> vs TypedInt<Category>)
      // but this is only enforced at compile-time

      expect(productId.value).toBe(42)
      expect(categoryId.value).toBe(100)
    })
  })

  describe('Hash Code', () => {
    test('should return consistent hashCode for same values', () => {
      const id1 = createTypedInt<Product>(42)
      const id2 = createTypedInt<Product>(42)

      expect(id1.hashCode()).toBe(id2.hashCode())
    })

    test('should return different hashCode for different values', () => {
      const id1 = createTypedInt<Product>(42)
      const id2 = createTypedInt<Product>(43)

      expect(id1.hashCode()).not.toBe(id2.hashCode())
    })
  })
})
