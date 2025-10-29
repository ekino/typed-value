/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    globals: true,
    environment: 'node',
    include: ['tests/**/*.test.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['tests/**/*.ts']
    }
  }
})
