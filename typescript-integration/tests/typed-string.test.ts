/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
import { describe, test, expect } from 'vitest'
import { createTypedString, TypedString } from '@ekino/typed-value'

// Define entity marker types for type safety
interface User {}
interface Product {}
interface Email {}

describe('TypedString', () => {
  describe('Creation', () => {
    test('should create a TypedString with value', () => {
      const userId = createTypedString<User>('user-123')

      expect(userId.value).toBe('user-123')
    })

    test('should handle empty string value', () => {
      const emptyId = createTypedString<User>('')

      expect(emptyId.value).toBe('')
    })

    test('should handle special characters in value', () => {
      const specialId = createTypedString<Email>('user@domain.com')

      expect(specialId.value).toBe('user@domain.com')
    })
  })

  describe('toString', () => {
    test('should return the value as string', () => {
      const userId = createTypedString<User>('user-123')

      expect(userId.toString()).toBe('user-123')
    })
  })

  describe('Equality', () => {
    test('should be equal when values match', () => {
      const id1 = createTypedString<User>('user-123')
      const id2 = createTypedString<User>('user-123')

      expect(id1.equals(id2)).toBe(true)
    })

    test('should not be equal when values differ', () => {
      const id1 = createTypedString<User>('user-123')
      const id2 = createTypedString<User>('user-456')

      expect(id1.equals(id2)).toBe(false)
    })

    test('should be equal for same value regardless of type parameter (runtime)', () => {
      // Note: At runtime, type parameters are erased. Equality is based on value only.
      // Type safety is enforced at compile-time via TypeScript.
      const userId = createTypedString<User>('123')
      const productId = createTypedString<Product>('123')

      // At runtime, these are equal because values match
      expect(userId.equals(productId)).toBe(true)
    })

    test('should not be equal to null', () => {
      const userId = createTypedString<User>('user-123')

      expect(userId.equals(null)).toBe(false)
    })
  })

  describe('Comparison', () => {
    test('should compare by value', () => {
      const a = createTypedString<User>('a')
      const b = createTypedString<User>('b')

      expect(a.compareTo(b)).toBeLessThan(0)
      expect(b.compareTo(a)).toBeGreaterThan(0)
    })

    test('should return 0 for equal values', () => {
      const id1 = createTypedString<User>('user-123')
      const id2 = createTypedString<User>('user-123')

      expect(id1.compareTo(id2)).toBe(0)
    })
  })

  describe('Type Safety', () => {
    test('should have proper TypeScript types', () => {
      const userId: TypedString<User> = createTypedString<User>('user-123')

      const value: string = userId.value

      expect(typeof value).toBe('string')
    })

    test('should allow type inference with explicit generic', () => {
      // TypeScript infers the return type from the generic parameter
      const userId = createTypedString<User>('user-123')
      const productId = createTypedString<Product>('prod-456')

      // Both are different TypeScript types (TypedString<User> vs TypedString<Product>)
      // but this is only enforced at compile-time

      expect(userId.value).toBe('user-123')
      expect(productId.value).toBe('prod-456')
    })
  })

  describe('Hash Code', () => {
    test('should return consistent hashCode for same values', () => {
      const id1 = createTypedString<User>('user-123')
      const id2 = createTypedString<User>('user-123')

      expect(id1.hashCode()).toBe(id2.hashCode())
    })

    test('should return different hashCode for different values', () => {
      const id1 = createTypedString<User>('user-123')
      const id2 = createTypedString<User>('user-456')

      expect(id1.hashCode()).not.toBe(id2.hashCode())
    })
  })
})
