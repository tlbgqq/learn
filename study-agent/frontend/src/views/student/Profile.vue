<template>
  <div class="profile">
    <div class="profile-header">
      <el-avatar :size="80" :src="studentStore.avatar">
        {{ studentStore.nickname?.charAt(0) }}
      </el-avatar>
      <h2>{{ studentStore.nickname }}</h2>
      <p class="grade">年级：{{ studentStore.gradeName || '未设置' }}</p>
    </div>

    <div class="stats-card">
      <div class="stat-item">
        <span class="stat-value">{{ studentStore.level }}</span>
        <span class="stat-label">等级</span>
      </div>
      <div class="stat-item">
        <span class="stat-value gold">{{ studentStore.gold }}</span>
        <span class="stat-label">金币</span>
      </div>
      <div class="stat-item">
        <span class="stat-value diamond">{{ studentStore.diamond }}</span>
        <span class="stat-label">钻石</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ studentStore.continuousStudyDays }}</span>
        <span class="stat-label">连续学习</span>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="goToKnowledge">
        <span class="menu-icon">📚</span>
        <span class="menu-text">知识地图</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="goToRank">
        <span class="menu-icon">📊</span>
        <span class="menu-text">排行榜</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="goToSettings">
        <span class="menu-icon">⚙️</span>
        <span class="menu-text">设置</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item logout" @click="handleLogout">
        <span class="menu-icon">🚪</span>
        <span class="menu-text">退出登录</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useRouter} from 'vue-router'
import {useStudentStore} from '@/store/student'
import {ElMessageBox} from 'element-plus'
import {onMounted} from 'vue'

const router = useRouter()
const studentStore = useStudentStore()

onMounted(async () => {
  if (studentStore.id) {
    await studentStore.fetchProfile(studentStore.id)
  }
})

const goToKnowledge = () => {
  router.push('/student/knowledge')
}

const goToRank = () => {
  router.push('/student/rank')
}

const goToSettings = () => {
  router.push('/student/settings')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.clear()
    studentStore.$reset()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.profile {
  padding-bottom: 20px;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: var(--primary-gradient);
  color: white;
  border-radius: 0 0 24px 24px;
  margin: -16px -16px 16px -16px;
}

.profile-header h2 {
  margin-top: 12px;
  font-size: 20px;
}

.grade {
  font-size: 14px;
  opacity: 0.9;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.stat-value.gold { color: #f5a623; }
.stat-value.diamond { color: #9b59b6; }

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.menu-list {
  background: white;
  border-radius: 16px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:active {
  background: #f8f9fa;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item.logout {
  color: #f44336;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-item.logout .menu-text {
  color: #f44336;
}

.menu-arrow {
  color: #ccc;
  font-size: 18px;
}
</style>
