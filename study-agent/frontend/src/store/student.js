import {defineStore} from 'pinia'
import {studentApi} from '@/api'

// 从 localStorage 恢复状态
const savedStudent = localStorage.getItem('student')
const savedState = savedStudent ? JSON.parse(savedStudent) : null

export const useStudentStore = defineStore('student', {
  state: () => ({
    id: savedState?.id || null,
    username: savedState?.username || '',
    nickname: savedState?.nickname || '',
    avatar: savedState?.avatar || '',
    level: savedState?.level || 1,
    exp: savedState?.exp || 0,
    gold: savedState?.gold || 0,
    diamond: savedState?.diamond || 0,
    continuousStudyDays: savedState?.continuousStudyDays || 0,
    gradeId: savedState?.gradeId || null,
    gradeName: savedState?.gradeName || ''
  }),

  getters: {
    expForNextLevel: (state) => state.level * 100,
    expProgress: (state) => {
      const max = state.level * 100
      return Math.min((state.exp / max) * 100, 100)
    }
  },

  actions: {
    setStudent(student) {
      this.id = student.id
      this.username = student.username || ''
      this.nickname = student.nickname || student.username || '学生'
      this.avatar = student.avatar || ''
      this.level = student.level || 1
      this.exp = student.exp || 0
      this.gold = student.gold || 0
      this.diamond = student.diamond || 0
      this.continuousStudyDays = student.continuousStudyDays || 0
      this.gradeId = student.gradeId
      this.gradeName = student.gradeName || ''
      // 同步到 localStorage
      this.saveToStorage()
    },

    saveToStorage() {
      const data = {
        id: this.id,
        username: this.username,
        nickname: this.nickname,
        avatar: this.avatar,
        level: this.level,
        exp: this.exp,
        gold: this.gold,
        diamond: this.diamond,
        continuousStudyDays: this.continuousStudyDays,
        gradeId: this.gradeId,
        gradeName: this.gradeName
      }
      localStorage.setItem('student', JSON.stringify(data))
    },

    async fetchProfile(id) {
      try {
        const student = await studentApi.getProfile(id)
        this.setStudent(student)
        return student
      } catch (e) {
        console.error('获取学生信息失败', e)
        return null
      }
    },

    addGold(amount) {
      this.gold += amount
      this.saveToStorage()
    },

    addDiamond(amount) {
      this.diamond += amount
      this.saveToStorage()
    },

    addExp(amount) {
      this.exp += amount
      while (this.exp >= this.expForNextLevel) {
        this.exp -= this.expForNextLevel
        this.level++
      }
      this.saveToStorage()
    }
  }
})
