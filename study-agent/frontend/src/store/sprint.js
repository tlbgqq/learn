import {defineStore} from 'pinia'
import {sprintApi} from '@/api'

export const useSprintStore = defineStore('sprint', {
  state: () => ({
    sessionId: null,
    status: 'idle', // idle | ready | playing | finished
    studentId: null,
    subjectId: null,
    startTime: null,
    currentQuestion: null,
    combo: 0,
    maxCombo: 0,
    score: 0,
    questionCount: 0,
    correctCount: 0,
    wrongCount: 0,
    timeLeft: 60,
    rewards: {gold: 0, exp: 0, diamond: 0},
    historyBest: {
      bestScore: 0,
      bestCombo: 0,
      bestAccuracy: 0
    },
    result: null,
    questionStartTime: null
  }),

  getters: {
    accuracy: (state) => {
      if (state.questionCount === 0) return 0
      return ((state.correctCount / state.questionCount) * 100).toFixed(1)
    },

    isPlaying: (state) => state.status === 'playing'
  },

  actions: {
    async loadBest(studentId) {
      try {
        const best = await sprintApi.getBest(studentId)
        if (best) {
          this.historyBest = {
            bestScore: best.bestScore || 0,
            bestCombo: best.bestCombo || 0,
            bestAccuracy: best.bestAccuracy || 0
          }
        }
      } catch (e) {
        console.error('获取历史最佳失败', e)
      }
    },

    async startSprint(studentId, subjectId) {
      try {
        this.studentId = studentId
        const data = await sprintApi.start(subjectId)
        this.sessionId = data.sessionId
        this.subjectId = data.subjectId
        this.startTime = Date.now()
        this.status = 'playing'
        this.combo = 0
        this.maxCombo = 0
        this.score = 0
        this.questionCount = 0
        this.correctCount = 0
        this.wrongCount = 0
        this.rewards = {gold: 0, exp: 0, diamond: 0}
        this.result = null
        this.currentQuestion = data.question
        this.questionStartTime = Date.now()
        this.timeLeft = 60

        localStorage.setItem('sprint_session', JSON.stringify({
          sessionId: data.sessionId,
          studentId,
          subjectId: data.subjectId
        }))

        return data
      } catch (e) {
        console.error('开始冲刺失败', e)
        throw e
      }
    },

    async submitAnswer(answer) {
      if (!this.currentQuestion || !this.sessionId) return null

      const answerTime = (Date.now() - this.questionStartTime) / 1000

      try {
        const result = await sprintApi.submitAnswer({
          sessionId: this.sessionId,
          questionId: this.currentQuestion.id,
          answer: answer,
          answerTime: answerTime
        })

        if (result.isCorrect) {
          this.combo++
          if (this.combo > this.maxCombo) {
            this.maxCombo = this.combo
          }
          this.score += result.score || 0
          this.correctCount++
          this.rewards.gold += result.reward?.gold || 0
          this.rewards.exp += result.reward?.exp || 0
          this.rewards.diamond += result.reward?.diamond || 0
        } else {
          this.combo = 0
          this.wrongCount++
        }

        this.questionCount++
        this.currentQuestion = result.nextQuestion
        this.questionStartTime = Date.now()

        return result
      } catch (e) {
        console.error('提交答案失败', e)
        this.combo = 0
        this.wrongCount++
        this.questionCount++
        throw e
      }
    },

    async finishSprint(reason = 'USER_END') {
      if (!this.sessionId) return null

      try {
        const result = await sprintApi.finish({
          sessionId: this.sessionId,
          reason: reason
        })

        this.result = result
        this.status = 'finished'
        this.currentQuestion = null

        localStorage.removeItem('sprint_session')

        return result
      } catch (e) {
        console.error('结束冲刺失败', e)
        throw e
      }
    },

    getStudentId() {
      // 备用：从 localStorage 获取
      const student = localStorage.getItem('student')
      if (student) {
        try {
          return JSON.parse(student).id
        } catch (e) {
          return null
        }
      }
      return null
    },

    reset() {
      this.sessionId = null
      this.status = 'idle'
      this.studentId = null
      this.subjectId = null
      this.startTime = null
      this.currentQuestion = null
      this.combo = 0
      this.maxCombo = 0
      this.score = 0
      this.questionCount = 0
      this.correctCount = 0
      this.wrongCount = 0
      this.rewards = {gold: 0, exp: 0, diamond: 0}
      this.result = null
      this.timeLeft = 60
    },

    setTimeLeft(time) {
      this.timeLeft = time
    },

    setStatus(status) {
      this.status = status
    }
  }
})
