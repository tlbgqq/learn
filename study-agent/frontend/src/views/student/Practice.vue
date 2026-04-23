<template>
  <div class="practice">
    <div class="practice-header">
      <div class="mode-badge">{{ modeText }}</div>
      <div v-if="combo > 1" class="combo">
        <span class="combo-icon">🔥</span>
        <span class="combo-text">{{ combo }} 连击</span>
      </div>
    </div>

    <div v-if="currentQuestion" class="question-card">
      <div class="question-type">{{ questionTypeText }}</div>
      <div class="question-content">{{ currentQuestion.content }}</div>

      <div class="answer-section">
        <el-input
          v-if="currentQuestion.type === '填空' || currentQuestion.type === 'text'"
          v-model="answer"
          placeholder="请输入答案"
          size="large"
          @keyup.enter="submitAnswer"
        />

        <div v-else class="choice-list">
          <div
            v-for="(choice, index) in choices"
            :key="index"
            :class="{ selected: answer === choice }"
            class="choice-item"
            @click="selectChoice(choice)"
          >
            <span class="choice-index">{{ String.fromCharCode(65 + index) }}</span>
            <span class="choice-text">{{ choice }}</span>
          </div>
        </div>
      </div>

      <el-button
        :disabled="!answer"
        class="submit-btn"
        size="large"
        type="primary"
        @click="submitAnswer"
      >
        提交答案
      </el-button>
    </div>

    <div v-else-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载题目中...</span>
    </div>

    <div v-else class="empty-state">
      <span class="empty-icon">📝</span>
      <p>暂无题目</p>
      <el-button type="primary" @click="$router.push('/student/home')">返回首页</el-button>
    </div>

    <div v-if="showReward" class="reward-popup">
      <div class="reward-content">
        <div class="reward-icon">🎉</div>
        <div class="reward-text">回答正确！</div>
        <div class="reward-items">
          <span class="reward-item gold">+{{ rewardResult.gold }} 🪙</span>
          <span class="reward-item exp">+{{ rewardResult.exp }} 经验</span>
          <span v-if="rewardResult.diamond > 0" class="reward-item diamond">+{{ rewardResult.diamond }} 💎</span>
        </div>
        <el-button size="large" type="primary" @click="nextQuestion">下一题</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {questionApi} from '@/api'
import {useStudentStore} from '@/store/student'

const route = useRoute()
const router = useRouter()
const studentStore = useStudentStore()

const loading = ref(false)
const currentQuestion = ref(null)
const answer = ref('')
const choices = ref([])
const combo = ref(0)
const showReward = ref(false)
const rewardResult = ref({ gold: 0, exp: 0, diamond: 0 })

const modeText = computed(() => {
  const mode = route.query.mode || 'sprint'
  const kpId = route.query.kpId
  if (kpId) {
    return '🎯 错题复习'
  }
  const map = {
    sprint: '🏃 冲刺模式',
    targeted: '🎯 精准打击',
    pk: '🃏 卡牌对战',
    map: '🗺️ 知识地图'
  }
  return map[mode] || '练习'
})

const questionTypeText = computed(() => {
  const type = currentQuestion.value?.type
  const map = { '选择': '选择题', '填空': '填空题', 'text': '填空题', '解答': '解答题' }
  return map[type] || '题目'
})

const selectChoice = (choice) => {
  answer.value = choice
}

const loadQuestion = async () => {
  loading.value = true
  try {
    const questionId = route.query.questionId
    const kpId = route.query.kpId

    if (questionId) {
      // 加载指定题目
      const question = await questionApi.getById(questionId)
      if (question) {
        currentQuestion.value = question
        if (question.options) {
          try {
            choices.value = JSON.parse(question.options)
          } catch {
            choices.value = ['A. 选项1', 'B. 选项2', 'C. 选项3', 'D. 选项4']
          }
        }
      } else {
        currentQuestion.value = null
      }
    } else if (kpId) {
      // 根据知识点加载题目
      const questions = await questionApi.getByKnowledgePoint(kpId)
      if (questions && questions.length > 0) {
        const excludeId = route.query.excludeQuestionId ? parseInt(route.query.excludeQuestionId) : null
        let availableQuestions = questions
        if (excludeId) {
          availableQuestions = questions.filter(q => q.id !== excludeId)
        }
        if (availableQuestions.length > 0) {
          currentQuestion.value = availableQuestions[Math.floor(Math.random() * availableQuestions.length)]
        } else {
          currentQuestion.value = questions[Math.floor(Math.random() * questions.length)]
        }
        if (currentQuestion.value.type === '选择' && currentQuestion.value.options) {
          try {
            choices.value = JSON.parse(currentQuestion.value.options)
          } catch {
            choices.value = ['A. 选项1', 'B. 选项2', 'C. 选项3', 'D. 选项4']
          }
        }
      } else {
        currentQuestion.value = null
      }
    } else {
      // 根据科目加载题目
      const subjectId = route.query.subjectId || 2
      const questions = await questionApi.getBySubject(subjectId)
      if (questions && questions.length > 0) {
        currentQuestion.value = questions[Math.floor(Math.random() * questions.length)]
        if (currentQuestion.value.type === '选择' && currentQuestion.value.options) {
          try {
            choices.value = JSON.parse(currentQuestion.value.options)
          } catch {
            choices.value = ['A. 选项1', 'B. 选项2', 'C. 选项3', 'D. 选项4']
          }
        }
      } else {
        currentQuestion.value = null
      }
    }
  } catch (e) {
    console.error('加载题目失败', e)
    currentQuestion.value = null
  } finally {
    loading.value = false
  }
}

const submitAnswer = async () => {
  if (!answer.value || !currentQuestion.value) return

  try {
    const result = await questionApi.submitAnswer({
      studentId: studentStore.id,
      questionId: currentQuestion.value.id,
      answer: answer.value
    })

    if (result.isCorrect) {
      combo.value++
      rewardResult.value = { gold: 10 + combo.value * 2, exp: 5 + combo.value, diamond: combo.value >= 5 ? 1 : 0 }
      showReward.value = true
      studentStore.addGold(rewardResult.value.gold)
      studentStore.addExp(rewardResult.value.exp)
      if (rewardResult.value.diamond > 0) {
        studentStore.addDiamond(rewardResult.value.diamond)
      }
    } else {
      combo.value = 0
      ElMessage.error({
        message: '回答错误，再接再厉！',
        duration: 1500
      })
      answer.value = ''
    }
  } catch (e) {
    console.error('提交答案失败', e)
    ElMessage.error('提交失败，请重试')
  }
}

const nextQuestion = () => {
  showReward.value = false
  answer.value = ''
  loadQuestion()
}

onMounted(() => {
  if (!studentStore.id) {
    router.push('/login')
    return
  }
  loadQuestion()
})

// 监听 query 变化，重新加载题目
watch(() => route.query, (newQuery) => {
  if (newQuery.questionId || newQuery.kpId) {
    loadQuestion()
  }
})
</script>

<style scoped>
.practice {
  padding-bottom: 20px;
}

.practice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.mode-badge {
  padding: 8px 16px;
  background: var(--primary-gradient);
  color: white;
  border-radius: 20px;
  font-weight: 600;
}

.combo {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: #fff3e0;
  border-radius: 20px;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.combo-icon {
  font-size: 18px;
}

.combo-text {
  color: #ff9800;
  font-weight: 600;
}

.question-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.question-type {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.question-content {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  line-height: 1.6;
  margin-bottom: 24px;
}

.answer-section {
  margin-bottom: 24px;
}

.choice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.choice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.choice-item:hover {
  background: #e9ecef;
}

.choice-item.selected {
  background: #667eea;
  color: #ffffff;
  border-color: #5a6fd6;
}

.choice-item.selected .choice-text {
  color: #ffffff;
}

.choice-index {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  font-weight: 600;
  flex-shrink: 0;
}

.choice-item.selected .choice-index {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.loading,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;
  background: white;
  border-radius: 16px;
}

.empty-icon {
  font-size: 64px;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  margin-bottom: 16px;
}

.reward-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.reward-content {
  background: white;
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  max-width: 320px;
  width: 90%;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.reward-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: bounce 0.6s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.reward-text {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.reward-items {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.reward-item {
  font-weight: 600;
  font-size: 14px;
}

.reward-item.gold { color: #f5a623; }
.reward-item.exp { color: var(--primary-color); }
.reward-item.diamond { color: #9b59b6; }
</style>
