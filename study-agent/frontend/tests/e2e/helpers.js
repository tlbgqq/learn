/**
 * E2E Test Helpers
 *
 * Provides utility functions for:
 * - Test account registration and cleanup
 * - API calls directly to backend
 * - Token management
 */

const API_BASE = 'http://localhost:8080/api'

/**
 * Generate a unique test account username
 */
export function generateTestUsername() {
  return `e2e_test_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
}

/**
 * Register a new test account via backend API
 * @param {string} username
 * @param {string} password
 * @param {string} nickname
 * @param {number} gradeId
 * @returns {{ student: object, token: string }}
 */
export async function registerTestAccount(username, password, nickname = 'E2E测试用户', gradeId = 1) {
  const response = await fetch(`${API_BASE}/student/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, nickname, gradeId })
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Registration failed' }))
    throw new Error(error.message || `Registration failed: ${response.status}`)
  }

  return response.json()
}

/**
 * Login a test account via backend API
 * @param {string} username
 * @param {string} password
 * @returns {{ student: object, token: string }}
 */
export async function loginTestAccount(username, password) {
  const response = await fetch(`${API_BASE}/student/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Login failed' }))
    throw new Error(error.message || `Login failed: ${response.status}`)
  }

  return response.json()
}

/**
 * Get current user profile via backend API
 * @param {string} token
 * @returns {object}
 */
export async function getCurrentUser(token) {
  const response = await fetch(`${API_BASE}/student/me`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  if (!response.ok) {
    throw new Error(`Failed to get current user: ${response.status}`)
  }

  return response.json()
}

/**
 * Start a sprint session via backend API
 * @param {string} token
 * @param {number} subjectId
 * @returns {{ sessionId: string, question: object }}
 */
export async function startSprintSession(token, subjectId = 3) {
  const response = await fetch(`${API_BASE}/practice/sprint/start`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ subjectId })
  })

  if (!response.ok) {
    throw new Error(`Failed to start sprint: ${response.status}`)
  }

  return response.json()
}

/**
 * Submit an answer to a sprint question via backend API
 * @param {string} token
 * @param {string} sessionId
 * @param {number} questionId
 * @param {string} answer
 * @param {number} answerTime
 * @returns {{ isCorrect: boolean, nextQuestion: object }}
 */
export async function submitSprintAnswer(token, sessionId, questionId, answer, answerTime = 5) {
  const response = await fetch(`${API_BASE}/practice/sprint/answer`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ sessionId, questionId, answer, answerTime })
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`Failed to submit answer: ${response.status} - ${errorText}`)
  }

  return response.json()
}

/**
 * Finish a sprint session via backend API
 * @param {string} token
 * @param {string} sessionId
 * @param {string} reason - 'USER_END' or 'TIMEOUT'
 * @returns {{ totalScore: number, rewards: object }}
 */
export async function finishSprintSession(token, sessionId, reason = 'USER_END') {
  const response = await fetch(`${API_BASE}/practice/sprint/finish`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ sessionId, reason })
  })

  if (!response.ok) {
    throw new Error(`Failed to finish sprint: ${response.status}`)
  }

  return response.json()
}

/**
 * Get wrong answers for a student via backend API
 * @param {string} token
 * @param {number} studentId
 * @returns {Array}
 */
export async function getWrongAnswers(token, studentId) {
  const response = await fetch(`${API_BASE}/question/wrong/${studentId}`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  if (!response.ok) {
    throw new Error(`Failed to get wrong answers: ${response.status}`)
  }

  return response.json()
}

/**
 * Get student profile (gold, exp, level) from backend
 * @param {string} token
 * @param {number} studentId
 * @returns {object}
 */
export async function getStudentProfile(token, studentId) {
  const response = await fetch(`${API_BASE}/student/${studentId}`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  if (!response.ok) {
    throw new Error(`Failed to get student profile: ${response.status}`)
  }

  return response.json()
}

/**
 * Get sprint history via backend API
 * @param {string} token
 * @returns {Array}
 */
export async function getSprintHistory(token) {
  const response = await fetch(`${API_BASE}/practice/sprint/history`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  if (!response.ok) {
    throw new Error(`Failed to get sprint history: ${response.status}`)
  }

  return response.json()
}

/**
 * Get sprint best record via backend API
 * @param {string} token
 * @returns {object}
 */
export async function getSprintBest(token) {
  const response = await fetch(`${API_BASE}/practice/sprint/best`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  if (!response.ok) {
    throw new Error(`Failed to get sprint best: ${response.status}`)
  }

  return response.json()
}

/**
 * Set localStorage auth state for a logged-in user
 * @param {import('@playwright/test').Page} page
 * @param {string} token
 * @param {object} student
 */
export async function setAuthState(page, token, student) {
  await page.evaluate(
    ({ token: t, student: s }) => {
      localStorage.setItem('token', t)
      localStorage.setItem('student', JSON.stringify(s))
      // Also set the pinia persist key directly to ensure store restoration works
      localStorage.setItem('student', JSON.stringify({
        id: s.id,
        username: s.username,
        nickname: s.nickname || s.username || '学生',
        avatar: s.avatar || '',
        level: s.level || 1,
        exp: s.exp || 0,
        gold: s.gold || 0,
        diamond: s.diamond || 0,
        continuousStudyDays: s.continuousStudyDays || 0,
        gradeId: s.gradeId,
        gradeName: s.gradeName || ''
      }))
    },
    { token, student }
  )
}

/**
 * Clear all auth state from localStorage
 * @param {import('@playwright/test').Page} page
 */
export async function clearAuthState(page) {
  await page.evaluate(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('student')
    localStorage.removeItem('sprint_session')
  })
}
