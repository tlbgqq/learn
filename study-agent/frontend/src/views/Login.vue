<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">学习辅助智能体</h1>
      <p class="subtitle">让学习像玩游戏一样上瘾</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            clearable
            placeholder="请输入用户名"
            prefix-icon="User"
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
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            :loading="loading"
            class="login-button"
            size="large"
            type="primary"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span class="link" @click="$router.push('/register')">还没有账号？立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {studentApi} from '@/api'
import {useStudentStore} from '@/store/student'

const router = useRouter()
const studentStore = useStudentStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await studentApi.login(form)
    studentStore.setStudent(data.student)
    localStorage.setItem('token', data.token)
    ElMessage.success('登录成功')
    router.push('/student/home')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: var(--primary-gradient);
}

.login-box {
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

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.footer-links {
  text-align: center;
  margin-top: 20px;
}
</style>