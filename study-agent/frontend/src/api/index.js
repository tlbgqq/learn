import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const studentApi = {
  register: (data) => api.post('/student/register', data),
  login: (data) => api.post('/student/login', data),
  getCurrentUser: () => api.get('/student/me'),
  getProfile: (id) => api.get(`/student/${id}`)
}

export const questionApi = {
  getById: (id) => api.get(`/question/${id}`),
  getBySubject: (subjectId) => api.get(`/question/subject/${subjectId}`),
  getByKnowledgePoint: (kpId) => api.get(`/question/knowledge/${kpId}`),
  submitAnswer: (data) => api.post('/question/submit', data),
  getWrongAnswers: (studentId) => api.get(`/question/wrong/${studentId}`)
}

export const knowledgeApi = {
  getSubjects: (gradeId) => api.get(`/knowledge/subjects/${gradeId}`),
  getTree: (subjectId) => api.get(`/knowledge/tree/${subjectId}`),
  getChildren: (parentId) => api.get(`/knowledge/children/${parentId}`),
  getBySubjectAndGrade: (subjectId, gradeId) => api.get(`/knowledge/subject/${subjectId}/grade/${gradeId}`)
}

export const rewardApi = {
  getAchievements: () => api.get('/reward/achievements'),
  getStudentAchievements: (studentId) => api.get(`/reward/achievements/${studentId}`),
  getDailyTasks: () => api.get('/reward/daily-tasks'),
  getStudentDailyTasks: (studentId) => api.get(`/reward/daily-tasks/${studentId}`)
}

export const gradeApi = {
  getAll: () => api.get('/grade')
}

export const rankApi = {
  getWeekRank: () => api.get('/rank/week')
}

export const sprintApi = {
  start: (subjectId) => api.post('/practice/sprint/start', { subjectId }),
  getQuestion: (sessionId) => api.get(`/practice/sprint/question/${sessionId}`),
  submitAnswer: (data) => api.post('/practice/sprint/answer', data),
  finish: (data) => api.post('/practice/sprint/finish', data),
  getHistory: () => api.get('/practice/sprint/history'),
  getBest: () => api.get('/practice/sprint/best'),
}

export default api
