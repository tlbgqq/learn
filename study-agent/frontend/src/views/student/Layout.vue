<template>
  <div class="student-layout">
    <div class="header">
      <div class="user-info" @click="$router.push('/student/profile')">
        <el-avatar :size="36" :src="studentStore.avatar">
          {{ studentStore.nickname?.charAt(0) }}
        </el-avatar>
        <div class="user-stats">
          <span class="nickname">{{ studentStore.nickname }}</span>
          <div class="resources">
            <span class="gold">🪙 {{ studentStore.gold }}</span>
            <span class="diamond">💎 {{ studentStore.diamond }}</span>
          </div>
        </div>
      </div>

      <div class="level-badge">
        <span>Lv.{{ studentStore.level }}</span>
      </div>
    </div>

    <div class="content">
      <router-view />
    </div>

    <div class="bottom-nav">
      <div
        v-for="item in navItems"
        :key="item.path"
        :class="{ active: $route.path === item.path }"
        class="nav-item"
        @click="$router.push(item.path)"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useStudentStore} from '@/store/student'

const studentStore = useStudentStore()

const navItems = [
  { path: '/student/home', label: '首页', icon: '🏠' },
  { path: '/student/practice', label: '练习', icon: '📝' },
  { path: '/student/wrong-answers', label: '错题本', icon: '📋' },
  { path: '/student/achievements', label: '成就', icon: '🏅' },
  { path: '/student/profile', label: '我的', icon: '👤' }
]
</script>

<style scoped>
.student-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  max-width: 100%;
  padding-bottom: 70px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.user-stats {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 600;
  color: #333;
}

.resources {
  display: flex;
  gap: 8px;
  font-size: 12px;
}

.gold {
  color: #f5a623;
}

.diamond {
  color: #9b59b6;
}

.level-badge {
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

.content {
  flex: 1;
  padding: 16px;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid #eee;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 12px;
  transition: all 0.2s;
}

.nav-item:active {
  transform: scale(0.95);
}

.nav-icon {
  font-size: 20px;
}

.nav-label {
  font-size: 11px;
  color: #999;
}

.nav-item.active .nav-label {
  color: #667eea;
  font-weight: 600;
}

@media (min-width: 768px) {
  .student-layout {
    max-width: 480px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
}
</style>
