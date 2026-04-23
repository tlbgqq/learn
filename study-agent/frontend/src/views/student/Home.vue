<template>
  <div class="home">
    <div class="welcome-card">
      <h2>你好，{{ studentStore.nickname }}！</h2>
      <p>继续学习，保持连胜！</p>

      <div class="exp-bar">
        <div :style="{ width: expProgress + '%' }" class="exp-progress"></div>
      </div>
      <span class="exp-text">经验值 {{ studentStore.exp }}/{{ expForNextLevel }}</span>
    </div>

    <div v-if="dailyTasks.length > 0" class="daily-tasks">
      <h3>今日任务</h3>
      <div class="task-list">
        <div
          v-for="task in dailyTasks"
          :key="task.id"
          :class="{ completed: task.completed }"
          class="task-item"
        >
          <span class="task-icon">{{ task.icon || '📝' }}</span>
          <div class="task-info">
            <span class="task-name">{{ task.name }}</span>
            <span class="task-progress">{{ task.progress || 0 }}/{{ task.targetCount }}</span>
          </div>
          <span class="task-reward">+{{ task.rewardGold || 0 }}🪙</span>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>开始练习</h3>
      <div class="action-grid">
        <div class="action-card" @click="startPractice('sprint')">
          <span class="action-icon">🏃</span>
          <span class="action-name">冲刺模式</span>
          <span class="action-desc">限时挑战</span>
        </div>
        <div class="action-card" @click="startPractice('targeted')">
          <span class="action-icon">🎯</span>
          <span class="action-name">精准打击</span>
          <span class="action-desc">针对薄弱点</span>
        </div>
        <div class="action-card" @click="startPractice('pk')">
          <span class="action-icon">🃏</span>
          <span class="action-name">卡牌对战</span>
          <span class="action-desc">在线PK</span>
        </div>
        <div class="action-card" @click="startPractice('map')">
          <span class="action-icon">🗺️</span>
          <span class="action-name">知识地图</span>
          <span class="action-desc">通关式学习</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useStudentStore} from '@/store/student'
import {rewardApi} from '@/api'

const router = useRouter()
const studentStore = useStudentStore()

const dailyTasks = ref([])

const expForNextLevel = computed(() => studentStore.level * 100)
const expProgress = computed(() => Math.min((studentStore.exp / expForNextLevel.value) * 100, 100))

const startPractice = (mode) => {
  if (mode === 'sprint') {
    router.push('/student/sprint')
  } else {
    router.push({ path: '/student/practice', query: { mode } })
  }
}

const loadDailyTasks = async () => {
  try {
    const tasks = await rewardApi.getStudentDailyTasks(studentStore.id)
    dailyTasks.value = tasks || []
  } catch (e) {
    console.error('获取每日任务失败', e)
  }
}

onMounted(() => {
  if (studentStore.id) {
    loadDailyTasks()
  }
})
</script>

<style scoped>
.home {
  padding-bottom: 20px;
}

.welcome-card {
  background: var(--primary-gradient);
  color: white;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 20px;
}

.welcome-card h2 {
  font-size: 20px;
  margin-bottom: 4px;
}

.welcome-card p {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 16px;
}

.exp-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.exp-progress {
  height: 100%;
  background: white;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.exp-text {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.9;
}

.daily-tasks,
.quick-actions {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
}

h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: opacity 0.2s;
}

.task-item.completed {
  opacity: 0.6;
}

.task-item.completed .task-name {
  text-decoration: line-through;
}

.task-icon {
  font-size: 24px;
}

.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.task-name {
  font-weight: 500;
  color: #333;
}

.task-progress {
  font-size: 12px;
  color: #999;
}

.task-reward {
  font-size: 14px;
  color: #f5a623;
  font-weight: 500;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-card:active {
  transform: translateY(0);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.action-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: #999;
}
</style>
