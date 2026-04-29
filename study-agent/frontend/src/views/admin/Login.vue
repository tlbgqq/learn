<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">后台管理系统</h2>
      <el-form ref="loginForm" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            type="password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" class="login-btn" size="large" type="primary" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <p>默认账号: admin / Admin@123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {useAdminStore} from '@/store/admin'

const router = useRouter()
const adminStore = useAdminStore()

const loading = ref(false)
const loginForm = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  
  await loginForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await adminStore.login(form.username, form.password)
        ElMessage.success('登录成功')
        router.push('/admin/dashboard')
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
}

.login-tip {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>
