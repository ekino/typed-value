/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
import { describe, test, expect } from 'vitest'
import {
  createTypedString,
  createTypedInt,
  createTypedLong,
  TypedString,
  TypedInt,
  TypedLong
} from '@ekino/typed-value'

// Define entity marker types for compile-time type safety
interface User {}
interface Product {}
interface Order {}

describe('Type Safety', () => {
  describe('Compile-time type safety', () => {
    test('TypedString instances with same value are equal at runtime (type safety is compile-time)', () => {
      // Note: Type safety is enforced at COMPILE-TIME via TypeScript phantom types.
      // At runtime, instances with the same value ARE equal because we only compare values.
      const userId = createTypedString<User>('123')
      const productId = createTypedString<Product>('123')

      // At compile-time: TypedStringJs<User> !== TypedStringJs<Product>
      // At runtime: they are equal because values match
      expect(userId.equals(productId)).toBe(true)
    })

    test('TypedInt instances with same value are equal at runtime', () => {
      const userId = createTypedInt<User>(123)
      const productId = createTypedInt<Product>(123)

      // Type safety is compile-time only
      expect(userId.equals(productId)).toBe(true)
    })

    test('TypedLong instances with same value are equal at runtime', () => {
      const userId = createTypedLong<User>(123)
      const productId = createTypedLong<Product>(123)

      // Type safety is compile-time only
      expect(userId.equals(productId)).toBe(true)
    })
  })

  describe('Collection usage patterns', () => {
    test('should be able to use TypedString in arrays with type parameter', () => {
      const userIds: TypedString<User>[] = [
        createTypedString<User>('user-1'),
        createTypedString<User>('user-2'),
        createTypedString<User>('user-3')
      ]

      expect(userIds).toHaveLength(3)
      expect(userIds[0].value).toBe('user-1')
    })

    test('should be able to use TypedInt in arrays with type parameter', () => {
      const productIds: TypedInt<Product>[] = [
        createTypedInt<Product>(1),
        createTypedInt<Product>(2),
        createTypedInt<Product>(3)
      ]

      expect(productIds).toHaveLength(3)
      expect(productIds[0].value).toBe(1)
    })

    test('should be able to use TypedLong in arrays with type parameter', () => {
      const orderIds: TypedLong<Order>[] = [
        createTypedLong<Order>(1000001),
        createTypedLong<Order>(1000002),
        createTypedLong<Order>(1000003)
      ]

      expect(orderIds).toHaveLength(3)
      expect(orderIds[0].value).toBe(1000001)
    })

    test('should be able to filter typed values', () => {
      const ids: TypedString<User>[] = [
        createTypedString<User>('active-1'),
        createTypedString<User>('inactive-2'),
        createTypedString<User>('active-3')
      ]

      const activeIds = ids.filter(id => id.value.startsWith('active'))

      expect(activeIds).toHaveLength(2)
    })

    test('should be able to map typed values to raw values', () => {
      const userIds: TypedString<User>[] = [
        createTypedString<User>('user-1'),
        createTypedString<User>('user-2')
      ]

      const rawValues: string[] = userIds.map(id => id.value)

      expect(rawValues).toEqual(['user-1', 'user-2'])
    })
  })

  describe('Domain modeling patterns', () => {
    test('should work in a domain entity context with typed IDs', () => {
      interface UserEntity {
        id: TypedString<User>
        name: string
        email: string
      }

      const user: UserEntity = {
        id: createTypedString<User>('user-123'),
        name: 'John Doe',
        email: 'john@example.com'
      }

      expect(user.id.value).toBe('user-123')
    })

    test('should work with nested entities using different ID types', () => {
      interface OrderEntity {
        id: TypedLong<Order>
        userId: TypedString<User>
        productIds: TypedInt<Product>[]
      }

      const order: OrderEntity = {
        id: createTypedLong<Order>(1001),
        userId: createTypedString<User>('user-123'),
        productIds: [createTypedInt<Product>(1), createTypedInt<Product>(2)]
      }

      expect(order.id.value).toBe(1001)
      expect(order.userId.value).toBe('user-123')
      expect(order.productIds[0].value).toBe(1)
    })
  })

  describe('toString for debugging', () => {
    test('all types should return value as string (no type info at runtime)', () => {
      const userId = createTypedString<User>('user-123')
      const productId = createTypedInt<Product>(42)
      const orderId = createTypedLong<Order>(1001)

      // toString now returns just the value (no type info since we removed typeName)
      expect(userId.toString()).toBe('user-123')
      expect(productId.toString()).toBe('42')
      expect(orderId.toString()).toBe('1001')
    })
  })

  describe('Immutability', () => {
    test('TypedStringJs should be immutable', () => {
      const userId = createTypedString<User>('user-123')
      const originalValue = userId.value

      // Attempt to modify (should not affect the object)
      expect(userId.value).toBe(originalValue)
    })
  })

  describe('Type-safe function parameters', () => {
    test('demonstrates how type-safe functions would work', () => {
      // This function only accepts User IDs (compile-time enforced)
      function getUserById(id: TypedString<User>): { id: string; name: string } {
        return { id: id.value, name: 'Test User' }
      }

      const userId = createTypedString<User>('user-123')
      const result = getUserById(userId)

      expect(result.id).toBe('user-123')

      // The following would NOT compile (but we can't test compile errors at runtime):
      // const productId = createTypedString<Product>('prod-456')
      // getUserById(productId) // TypeScript Error!
    })

    test('demonstrates mixed ID types in a service', () => {
      // A service that works with different ID types
      function processOrder(
        orderId: TypedLong<Order>,
        userId: TypedString<User>,
        productIds: TypedInt<Product>[]
      ): { orderValue: number; userValue: string; productCount: number } {
        return {
          orderValue: orderId.value,
          userValue: userId.value,
          productCount: productIds.length
        }
      }

      const result = processOrder(
        createTypedLong<Order>(1001),
        createTypedString<User>('user-123'),
        [createTypedInt<Product>(1), createTypedInt<Product>(2)]
      )

      expect(result.orderValue).toBe(1001)
      expect(result.userValue).toBe('user-123')
      expect(result.productCount).toBe(2)
    })
  })
})
