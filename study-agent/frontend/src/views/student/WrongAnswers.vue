<template>
  <div class="wrong-answers">
    <h2>错题本</h2>

    <div v-if="subjects.length > 0" class="filter-bar">
      <el-select v-model="filterSubject" placeholder="筛选学科" size="default">
        <el-option :value="null" label="全部学科" />
        <el-option v-for="sub in subjects" :key="sub.id" :label="sub.name" :value="sub.id" />
      </el-select>
    </div>

    <div v-if="filteredWrongAnswers.length > 0" class="wrong-list">
      <div
        v-for="item in filteredWrongAnswers"
        :key="item.id"
        class="wrong-item"
      >
        <div class="wrong-info">
          <span :class="item.errorType" class="wrong-type">{{ item.errorType || '错误' }}</span>
          <span v-if="item.knowledgePointId" class="wrong-kp">{{ item.knowledgePointName || '知识点 #' + item.knowledgePointId }}</span>
        </div>
        <div class="wrong-question">{{ item.questionContent || '题目内容' }}</div>
        <div class="wrong-detail">
          <span class="your-answer">你的答案: {{ item.yourAnswer }}</span>
          <span v-if="item.correctAnswer" class="correct-answer">正确答案: {{ item.correctAnswer }}</span>
        </div>
        <div class="wrong-actions">
          <el-button size="small" type="primary" @click="practiceAgain(item)">
            再练一练
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <span class="empty-icon">🎉</span>
      <p>太棒了！暂无错题</p>
      <el-button type="primary" @click="$router.push('/student/practice')">
        去练习
      </el-button>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {knowledgeApi, questionApi} from '@/api'
import {useStudentStore} from '@/store/student'

const router = useRouter()
const studentStore = useStudentStore()

const wrongAnswers = ref([])
const subjects = ref([])
const filterSubject = ref(null)

const filteredWrongAnswers = computed(() => {
  if (!filterSubject.value) return wrongAnswers.value
  return wrongAnswers.value.filter(item => item.subjectId === filterSubject.value)
})

const loadWrongAnswers = async () => {
  try {
    const answers = await questionApi.getWrongAnswers(studentStore.id)
    wrongAnswers.value = answers || []
  } catch (e) {
    console.error('加载错题失败', e)
    wrongAnswers.value = []
  }
}

const loadSubjects = async () => {
  try {
    subjects.value = await knowledgeApi.getSubjects(studentStore.gradeId)
  } catch (e) {
    console.error('加载学科失败', e)
  }
}

const practiceAgain = (item) => {
  if (item.knowledgePointId && item.knowledgePointId > 0) {
    router.push({
      path: '/student/practice',
      query: {
        kpId: item.knowledgePointId,
        excludeQuestionId: item.questionId,
        studentAnswerId: item.id
      }
    })
  } else if (item.questionId) {
    router.push({ path: '/student/practice', query: { questionId: item.questionId, studentAnswerId: item.id } })
  } else {
    router.push('/student/practice')
  }
}

onMounted(() => {
  if (studentStore.id) {
    loadWrongAnswers()
    loadSubjects()
  }
})
</script>

<style scoped>
.wrong-answers {
  padding-bottom: 20px;
}

h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.filter-bar {
  margin-bottom: 16px;
}

.wrong-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.wrong-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.wrong-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.wrong-type {
  padding: 4px 8px;
  background: #ffebee;
  color: #f44336;
  border-radius: 4px;
  font-size: 12px;
}

.wrong-type.知识点缺失 { background: #ffebee; color: #f44336; }
.wrong-type.理解偏差 { background: #fff3e0; color: #ff9800; }
.wrong-type.审题错误 { background: #e3f2fd; color: #2196f3; }
.wrong-type.计算失误 { background: #f3e5f5; color: #9c27b0; }

.wrong-kp {
  padding: 4px 8px;
  background: #e8f5e9;
  color: #4caf50;
  border-radius: 4px;
  font-size: 12px;
}

.wrong-question {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}

.wrong-detail {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.your-answer {
  color: #f44336;
}

.correct-answer {
  color: #4caf50;
  margin-left: 12px;
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
  margin-bottom: 20px;
}
</style>
