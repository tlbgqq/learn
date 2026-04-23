<template>
  <div class="achievements">
    <h2>成就徽章</h2>

    <div class="achievement-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部成就" name="all"></el-tab-pane>
        <el-tab-pane label="已获得" name="earned"></el-tab-pane>
        <el-tab-pane label="进行中" name="progress"></el-tab-pane>
      </el-tabs>
    </div>

    <div v-if="filteredAchievements.length > 0" class="achievement-grid">
      <div
        v-for="ach in filteredAchievements"
        :key="ach.id"
        :class="{ earned: isEarned(ach.id) }"
        class="achievement-card"
      >
        <div class="ach-icon">{{ ach.icon || '🏅' }}</div>
        <div class="ach-info">
          <span class="ach-name">{{ ach.name }}</span>
          <span class="ach-desc">{{ ach.description }}</span>
        </div>
        <div class="ach-reward">
          <span v-if="ach.rewardGold">+{{ ach.rewardGold }}🪙</span>
          <span v-if="ach.rewardExp">+{{ ach.rewardExp }}exp</span>
        </div>
        <div v-if="isEarned(ach.id)" class="ach-status">
          <span class="earned-tag">已获得</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <span class="empty-icon">🏅</span>
      <p>暂无成就数据</p>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {rewardApi} from '@/api'
import {useStudentStore} from '@/store/student'

const studentStore = useStudentStore()

const allAchievements = ref([])
const earnedAchievementIds = ref(new Set())
const activeTab = ref('all')

const filteredAchievements = computed(() => {
  if (activeTab.value === 'earned') {
    return allAchievements.value.filter(a => earnedAchievementIds.value.has(a.id))
  }
  if (activeTab.value === 'progress') {
    return allAchievements.value.filter(a => !earnedAchievementIds.value.has(a.id))
  }
  return allAchievements.value
})

const isEarned = (id) => earnedAchievementIds.value.has(id)

const loadAchievements = async () => {
  try {
    allAchievements.value = await rewardApi.getAchievements()
    const earned = await rewardApi.getStudentAchievements(studentStore.id)
    earnedAchievementIds.value = new Set(earned.map(e => e.achievementId))
  } catch (e) {
    console.error('加载成就失败', e)
  }
}

onMounted(() => {
  if (studentStore.id) {
    loadAchievements()
  }
})
</script>

<style scoped>
.achievements {
  padding-bottom: 20px;
}

h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.achievement-tabs {
  margin-bottom: 16px;
}

.achievement-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.achievement-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.achievement-card.earned {
  opacity: 1;
}

.ach-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.ach-info {
  margin-bottom: 8px;
}

.ach-name {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.ach-desc {
  font-size: 12px;
  color: #999;
}

.ach-reward {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #f5a623;
}

.ach-status {
  position: absolute;
  top: 8px;
  right: 8px;
}

.earned-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: #4caf50;
  color: white;
  border-radius: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #666;
}
</style>
