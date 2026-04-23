/**
 * Playwright Conftest
 *
 * This file provides shared fixtures and global setup for all E2E tests.
 * Playwright automatically loads conftest.js from the test directory.
 */

import {expect, test as base} from '@playwright/test'
import {generateTestUsername, registerTestAccount, setAuthState} from './helpers'

export { expect }

/**
 * Extended base test with shared authenticated page fixture.
 * Each test gets its own authenticated page context.
 */
export const test = base.extend({
  /**
   * Provide a pre-authenticated page using a dedicated test account.
   * Account is created fresh for each test to ensure isolation.
   */
  authenticatedPage: async ({ page }, use) => {
    const username = generateTestUsername()
    const password = 'test123456'

    try {
      const { token, student } = await registerTestAccount(username, password)

      // Inject auth state into localStorage before navigation
      await setAuthState(page, token, student)

      // Expose token on window for direct API calls in tests
      await page.addInitScript(
        (t) => {
          window.__TEST_TOKEN__ = t
        },
        token
      )
    } catch (error) {
      // If registration fails (e.g. backend not running), fail fast
      throw new Error(`Failed to create test account: ${error.message}`)
    }

    await use(page)

    // Cleanup happens automatically per-test via beforeEach
  }
})

/**
 * Global setup - runs once before all tests in the worker.
 * Use this for resource pre-warming or environment checks.
 */
export default async function globalSetup() {
  // Verify backend is reachable
  const backendUrl = 'http://localhost:8080/api/grade'
  try {
    const response = await fetch(backendUrl, { method: 'GET' })
    if (!response.ok) {
      console.warn(`[E2E Setup] Backend responded with HTTP ${response.status}`)
    } else {
      console.log('[E2E Setup] Backend is reachable and healthy.')
    }
  } catch (error) {
    console.error('[E2E Setup] Backend is not reachable at', backendUrl)
    console.error('[E2E Setup] Please ensure backend is running: cd backend && mvn spring-boot:run')
    // In CI, this should cause tests to fail. Locally, we warn and continue.
    if (process.env.CI) {
      throw error
    }
  }
}
