<template>
  <div class="register-container">
    <div class="register-box">
      <h1 class="title">注册账号</h1>
      <p class="subtitle">开启你的学习之旅</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            clearable
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            clearable
            placeholder="请输入昵称"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            size="large"
            type="password"
          />
        </el-form-item>

        <el-form-item prop="gradeId">
          <el-select
            v-model="form.gradeId"
            placeholder="请选择年级"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="grade in grades"
              :key="grade.id"
              :label="grade.name"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            :loading="loading"
            class="register-button"
            size="large"
            type="primary"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span class="link" @click="$router.push('/login')">已有账号？立即登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {gradeApi, studentApi} from '@/api'
import {useStudentStore} from '@/store/student'

const router = useRouter()
const studentStore = useStudentStore()

const formRef = ref()
const loading = ref(false)
const grades = ref([])

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  gradeId: null
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const loadGrades = async () => {
  try {
    grades.value = await gradeApi.getAll()
  } catch (e) {
    console.error('加载年级列表失败', e)
  }
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await studentApi.register(form)
    studentStore.setStudent(data.student)
    localStorage.setItem('token', data.token)
    ElMessage.success('注册成功')
    router.push('/student/home')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadGrades()
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: var(--primary-gradient);
}

.register-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
}

.title {
  text-align: center;
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  font-size: 14px;
  color: #999;
  margin-bottom: 32px;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.footer-links {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: var(--primary-color);
  cursor: pointer;
  font-size: 14px;
}

.link:hover {
  text-decoration: underline;
}
</style>
