<template>
  <div class="sprint-container">
    <!-- 状态: idle - 开始页面 -->
    <div v-if="sprintStore.status === 'idle'" class="sprint-start">
      <div class="start-header">
        <span class="back-btn" @click="$router.push('/student/home')">← 返回</span>
      </div>

      <div class="mode-title">
        <span class="mode-icon">🏃</span>
        <h1>冲刺模式</h1>
        <p class="mode-desc">60 秒限时挑战，考验你的速度！</p>
      </div>

      <div class="best-records">
        <div class="best-item">
          <span class="best-icon">🏆</span>
          <span class="best-value">{{ sprintStore.historyBest.bestScore }}</span>
          <span class="best-label">最高分</span>
        </div>
        <div class="best-item">
          <span class="best-icon">🔥</span>
          <span class="best-value">{{ sprintStore.historyBest.bestCombo }}</span>
          <span class="best-label">最高连击</span>
        </div>
        <div class="best-item">
          <span class="best-icon">📊</span>
          <span class="best-value">{{ sprintStore.historyBest.bestAccuracy }}%</span>
          <span class="best-label">最高正确率</span>
        </div>
      </div>

      <div class="subject-select">
        <h3>选择科目</h3>
        <div class="subject-grid">
          <div
            v-for="subject in subjects"
            :key="subject.id"
            :class="{ selected: selectedSubject === subject.id }"
            class="subject-card"
            @click="selectedSubject = subject.id"
          >
            <span class="subject-icon">{{ subject.icon }}</span>
            <span class="subject-name">{{ subject.name }}</span>
          </div>
        </div>
      </div>

      <div class="start-btn-wrapper">
        <button class="start-btn" @click="handleStart">
          🚀 开始冲刺
        </button>
      </div>
    </div>

    <!-- 状态: playing - 答题页面 -->
    <div v-else-if="sprintStore.status === 'playing'" class="sprint-play">
      <div class="play-header">
        <div class="mode-badge">🏃 冲刺模式</div>
        <div v-if="sprintStore.combo > 0" class="combo-display">
          <span class="combo-icon">🔥</span>
          <span class="combo-text">{{ sprintStore.combo }} 连击</span>
        </div>
      </div>

      <div class="score-display">
        <span class="score-value">{{ sprintStore.score }}</span>
        <span class="score-label">分</span>
      </div>

      <div class="timer-section">
        <div :class="{ warning: sprintStore.timeLeft <= 10 }" class="timer-text">
          ⏱️ {{ formatTime(sprintStore.timeLeft) }}
        </div>
        <div class="timer-bar">
          <div
            :class="{ warning: sprintStore.timeLeft <= 10 }"
            :style="{ width: (sprintStore.timeLeft / 60) * 100 + '%' }"
            class="timer-progress"
          ></div>
        </div>
      </div>

      <div v-if="sprintStore.currentQuestion" class="question-card">
        <div class="question-type">
          {{ sprintStore.currentQuestion.type === '选择' ? '选择题' : '填空题' }}
        </div>
        <div class="question-content">
          {{ sprintStore.currentQuestion.content }}
        </div>

        <div class="answer-section">
          <el-input
            v-if="sprintStore.currentQuestion.type === '填空' || !sprintStore.currentQuestion.options?.length"
            v-model="textAnswer"
            placeholder="请输入答案"
            size="large"
            @keyup.enter="handleSubmitAnswer"
          />

          <div v-else class="choice-list">
            <div
              v-for="(choice, index) in sprintStore.currentQuestion.options"
              :key="index"
              :class="{ selected: selectedChoice === choice }"
              class="choice-item"
              @click="handleSelectChoice(choice)"
            >
              <span class="choice-index">{{ String.fromCharCode(65 + index) }}</span>
              <span class="choice-text">{{ choice }}</span>
            </div>
          </div>
        </div>

        <el-button
          v-if="sprintStore.currentQuestion.type === '填空' || !sprintStore.currentQuestion.options?.length"
          :disabled="!textAnswer"
          class="submit-btn"
          size="large"
          type="primary"
          @click="handleSubmitAnswer"
        >
          提交答案
        </el-button>
        <el-button
          v-else
          :disabled="!selectedChoice"
          class="submit-btn"
          size="large"
          type="primary"
          @click="handleSubmitAnswer"
        >
          提交答案
        </el-button>
      </div>

      <div class="answer-history">
        <span
          v-for="(result, index) in answerResults"
          :key="index"
          :class="result ? 'correct' : 'wrong'"
          class="result-dot"
        ></span>
      </div>

      <div class="quit-section">
        <button class="quit-btn" @click="handleQuit">提前结束</button>
      </div>

      <!-- 答对弹窗 -->
      <div v-if="showCorrectPopup" class="result-popup correct-popup">
        <div class="popup-icon">🎉</div>
        <div class="popup-text">回答正确！</div>
        <div class="popup-score">+{{ lastScore }} 分</div>
        <div class="popup-rewards">
          <span v-if="lastReward.gold > 0">+{{ lastReward.gold }} 🪙</span>
          <span v-if="lastReward.exp > 0">+{{ lastReward.exp }} 经验</span>
          <span v-if="lastReward.diamond > 0" class="diamond">+{{ lastReward.diamond }} 💎</span>
        </div>
      </div>

      <!-- 答错弹窗 -->
      <div v-if="showWrongPopup" class="result-popup wrong-popup">
        <div class="popup-icon">😢</div>
        <div class="popup-text">回答错误</div>
        <div class="popup-answer">正确答案：{{ lastCorrectAnswer }}</div>
      </div>
    </div>

    <!-- 状态: finished - 结算页面 -->
    <div v-else-if="sprintStore.status === 'finished'" class="sprint-result">
      <div class="result-header">
        <span class="back-btn" @click="$router.push('/student/home')">← 返回首页</span>
      </div>

      <div class="result-title">
        <span v-if="sprintStore.result?.isNewRecord" class="new-record">🏆 新纪录！</span>
        <h1>{{ sprintStore.result?.endReason === 'TIMEOUT' ? '⏱️ 时间到！' : '🏁 挑战结束' }}</h1>
      </div>

      <div class="result-card">
        <div class="main-score">
          <span class="score-number">{{ sprintStore.result?.totalScore || sprintStore.score }}</span>
          <span class="score-unit">分</span>
        </div>

        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ sprintStore.result?.totalQuestions || sprintStore.questionCount }}</span>
            <span class="stat-label">总题数</span>
          </div>
          <div class="stat-item correct">
            <span class="stat-value">{{ sprintStore.result?.correctCount || sprintStore.correctCount }}</span>
            <span class="stat-label">正确</span>
          </div>
          <div class="stat-item wrong">
            <span class="stat-value">{{ sprintStore.result?.wrongCount || sprintStore.wrongCount }}</span>
            <span class="stat-label">错误</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ sprintStore.result?.maxCombo || sprintStore.maxCombo }}</span>
            <span class="stat-label">最高连击</span>
          </div>
        </div>

        <div class="accuracy-section">
          <span class="accuracy-label">正确率</span>
          <span class="accuracy-value">{{ sprintStore.result?.accuracy || sprintStore.accuracy }}%</span>
        </div>
      </div>

      <div class="rewards-card">
        <h3>🎁 本轮奖励</h3>
        <div class="rewards-list">
          <div class="reward-item gold">
            <span class="reward-icon">🪙</span>
            <span class="reward-value">+{{ sprintStore.result?.rewards?.gold || sprintStore.rewards.gold }}</span>
            <span class="reward-label">金币</span>
          </div>
          <div class="reward-item exp">
            <span class="reward-icon">⭐</span>
            <span class="reward-value">+{{ sprintStore.result?.rewards?.exp || sprintStore.rewards.exp }}</span>
            <span class="reward-label">经验</span>
          </div>
          <div class="reward-item diamond">
            <span class="reward-icon">💎</span>
            <span class="reward-value">+{{ sprintStore.result?.rewards?.diamond || sprintStore.rewards.diamond }}</span>
            <span class="reward-label">钻石</span>
          </div>
        </div>
      </div>

      <div class="action-buttons">
        <button class="action-btn primary" @click="handleRestart">
          🚀 再来一局
        </button>
        <button class="action-btn secondary" @click="$router.push('/student/home')">
          🏠 返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useSprintStore} from '@/store/sprint'
import {useStudentStore} from '@/store/student'
import {ElMessage} from 'element-plus'

const router = useRouter()
const sprintStore = useSprintStore()
const studentStore = useStudentStore()

const selectedSubject = ref(3)
const textAnswer = ref('')
const selectedChoice = ref('')
const answerResults = ref([])
const showCorrectPopup = ref(false)
const showWrongPopup = ref(false)
const lastScore = ref(0)
const lastReward = ref({gold: 0, exp: 0, diamond: 0})
const lastCorrectAnswer = ref('')

let timerInterval = null

const subjects = [
  {id: 1, name: '语文', icon: '📚'},
  {id: 2, name: '数学', icon: '🔢'},
  {id: 3, name: '英语', icon: '🗣️'}
]

onMounted(async () => {
  // 尝试从 localStorage 恢复学生数据
  if (!studentStore.id) {
    const stored = localStorage.getItem('student')
    if (stored) {
      try {
        studentStore.setStudent(JSON.parse(stored))
      } catch (e) {
        console.error('恢复学生数据失败', e)
      }
    }
  }

  if (!studentStore.id) {
    router.push('/login')
    return
  }

  await sprintStore.loadBest(studentStore.id)
})

onUnmounted(() => {
  stopTimer()
  sprintStore.reset()
})

const handleStart = async () => {
  try {
    await sprintStore.startSprint(studentStore.id, selectedSubject.value)
    startTimer()
  } catch (e) {
    ElMessage.error('开始冲刺失败，请重试')
  }
}

const handleSelectChoice = (choice) => {
  selectedChoice.value = choice
}

const handleSubmitAnswer = async () => {
  let answer = sprintStore.currentQuestion?.type === '选择'
    ? selectedChoice.value
    : textAnswer.value

  if (!answer) return

  // 提取答案标识符（从 "C. 15" 提取 "C"）
  if (sprintStore.currentQuestion?.type === '选择') {
    answer = answer.charAt(0)
  }

  try {
    const result = await sprintStore.submitAnswer(answer)
    answerResults.value.push(result.isCorrect)

    if (result.isCorrect) {
      lastScore.value = result.score || 0
      lastReward.value = result.reward || {gold: 0, exp: 0, diamond: 0}
      showCorrectPopup.value = true
      setTimeout(() => {
        showCorrectPopup.value = false
      }, 800)
    } else {
      lastCorrectAnswer.value = result.correctAnswer || ''
      showWrongPopup.value = true
      setTimeout(() => {
        showWrongPopup.value = false
      }, 800)
    }

    selectedChoice.value = ''
    textAnswer.value = ''
  } catch (e) {
    selectedChoice.value = ''
    textAnswer.value = ''
  }
}

const handleQuit = async () => {
  await sprintStore.finishSprint('USER_END')
  await studentStore.fetchProfile(studentStore.id)
  stopTimer()
}

const handleRestart = async () => {
  await studentStore.fetchProfile(studentStore.id)
  sprintStore.reset()
  await sprintStore.loadBest(studentStore.id)
}

const startTimer = () => {
  timerInterval = setInterval(() => {
    const elapsed = Math.floor((Date.now() - sprintStore.startTime) / 1000)
    const timeLeft = Math.max(0, 60 - elapsed)
    sprintStore.setTimeLeft(timeLeft)

    if (timeLeft <= 0) {
      handleTimeUp()
    }
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const handleTimeUp = async () => {
  stopTimer()
  try {
    await sprintStore.finishSprint('TIMEOUT')
    await studentStore.fetchProfile(studentStore.id)
  } catch (e) {
    console.error('结束冲刺失败', e)
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}
</script>

<style scoped>
.sprint-container {
  min-height: 100vh;
  padding: 16px;
  background: linear-gradient(180deg, #f0f2f5 0%, #ffffff 100%);
}

/* 开始页面 */
.sprint-start {
  max-width: 480px;
  margin: 0 auto;
}

.start-header {
  margin-bottom: 20px;
}

.back-btn {
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.mode-title {
  text-align: center;
  margin-bottom: 30px;
}

.mode-icon {
  font-size: 64px;
}

.mode-title h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 8px 0;
}

.mode-desc {
  color: #999;
  font-size: 14px;
}

.best-records {
  display: flex;
  justify-content: space-around;
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.best-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.best-icon {
  font-size: 24px;
}

.best-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.best-label {
  font-size: 12px;
  color: #999;
}

.subject-select {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.subject-select h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.subject-grid {
  display: flex;
  gap: 12px;
}

.subject-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.subject-card.selected {
  border-color: var(--primary-color, #4f46e5);
  background: rgba(79, 70, 229, 0.05);
}

.subject-icon {
  font-size: 32px;
}

.subject-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.start-btn-wrapper {
  text-align: center;
}

.start-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 18px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.start-btn:hover {
  transform: translateY(-2px);
}

/* 答题页面 */
.sprint-play {
  max-width: 480px;
  margin: 0 auto;
}

.play-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mode-badge {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

.combo-display {
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

.score-display {
  text-align: center;
  margin-bottom: 16px;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  color: #ff5722;
}

.score-label {
  font-size: 18px;
  color: #666;
  margin-left: 4px;
}

.timer-section {
  margin-bottom: 20px;
}

.timer-text {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.timer-text.warning {
  color: #ff5722;
  animation: blink 0.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.timer-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.timer-progress {
  height: 100%;
  background: linear-gradient(90deg, #4caf50 0%, #8bc34a 100%);
  transition: width 1s linear;
}

.timer-progress.warning {
  background: linear-gradient(90deg, #ff5722 0%, #ff9800 100%);
}

.question-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
}

.question-type {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.question-content {
  font-size: 20px;
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
}

.choice-item:hover {
  background: #e9ecef;
}

.choice-item.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
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
}

.choice-item.selected .choice-index {
  background: rgba(255, 255, 255, 0.3);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.answer-history {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.result-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.result-dot.correct {
  background: #4caf50;
}

.result-dot.wrong {
  background: #ff5722;
}

.quit-section {
  text-align: center;
}

.quit-btn {
  padding: 8px 24px;
  background: transparent;
  color: #999;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

/* 结果弹窗 */
.result-popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 32px;
  border-radius: 24px;
  text-align: center;
  z-index: 1000;
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  from { transform: translate(-50%, -50%) scale(0.8); opacity: 0; }
  to { transform: translate(-50%, -50%) scale(1); opacity: 1; }
}

.correct-popup {
  background: white;
}

.wrong-popup {
  background: white;
}

.popup-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.popup-text {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.popup-score {
  font-size: 32px;
  font-weight: 700;
  color: #ff5722;
  margin-bottom: 8px;
}

.popup-answer {
  font-size: 16px;
  color: #666;
}

.popup-rewards {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
}

.popup-rewards span {
  font-weight: 600;
  color: #f5a623;
}

.popup-rewards .diamond {
  color: #9b59b6;
}

/* 结算页面 */
.sprint-result {
  max-width: 480px;
  margin: 0 auto;
}

.result-header {
  margin-bottom: 20px;
}

.result-title {
  text-align: center;
  margin-bottom: 24px;
}

.result-title h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.new-record {
  display: block;
  font-size: 18px;
  color: #ff5722;
  margin-bottom: 8px;
  animation: bounce 0.6s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.result-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  text-align: center;
}

.main-score {
  margin-bottom: 24px;
}

.score-number {
  font-size: 72px;
  font-weight: 700;
  color: #ff5722;
}

.score-unit {
  font-size: 24px;
  color: #666;
  margin-left: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-item.correct .stat-value {
  color: #4caf50;
}

.stat-item.wrong .stat-value {
  color: #ff5722;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.accuracy-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.accuracy-label {
  font-size: 14px;
  color: #999;
}

.accuracy-value {
  font-size: 20px;
  font-weight: 600;
  color: #4caf50;
}

.rewards-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.rewards-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  text-align: center;
}

.rewards-list {
  display: flex;
  justify-content: space-around;
}

.reward-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.reward-icon {
  font-size: 24px;
}

.reward-value {
  font-size: 20px;
  font-weight: 700;
}

.reward-item.gold .reward-value {
  color: #f5a623;
}

.reward-item.exp .reward-value {
  color: #4caf50;
}

.reward-item.diamond .reward-value {
  color: #9b59b6;
}

.reward-label {
  font-size: 12px;
  color: #999;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.secondary {
  background: #f8f9fa;
  color: #333;
}
</style>
