<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="admin-aside">
      <div class="logo">
        <span v-if="!isCollapse">后台管理系统</span>
        <el-icon v-else><Menu /></el-icon>
      </div>
      <el-menu
        :collapse="isCollapse"
        :collapse-transition="false"
        :default-active="activeMenu"
        active-text-color="#409EFF"
        background-color="#304156"
        router
        text-color="#bfcbd9"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-sub-menu index="question">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </template>
          <el-menu-item index="/admin/question">
            <el-icon><List /></el-icon>
            <template #title>题目管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/question/add">
            <el-icon><Plus /></el-icon>
            <template #title>题目录入</template>
          </el-menu-item>
          <el-menu-item index="/admin/knowledge-point">
            <el-icon><Connection /></el-icon>
            <template #title>知识点管理</template>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/user">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/role">
            <el-icon><Avatar /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/menu">
            <el-icon><Menu /></el-icon>
            <template #title>菜单管理</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.name !== 'AdminDashboard'">
              {{ currentRoute.meta?.title || currentRoute.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ adminStore.userInfo?.username || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" show-password type="password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" show-password type="password" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" show-password type="password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button :loading="passwordLoading" type="primary" @click="handleUpdatePassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  ArrowDown,
  Avatar,
  Connection,
  Document,
  HomeFilled,
  List,
  Menu,
  Plus,
  Setting,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import {useAdminStore} from '@/store/admin'
import {userApi} from '@/api/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

onMounted(async () => {
  await adminStore.getUserInfo()
  await adminStore.getMenuList()
})

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中')
      break
    case 'password':
      passwordDialogVisible.value = true
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          adminStore.logout()
          router.push('/admin/login')
          ElMessage.success('已退出登录')
        })
        .catch(() => {})
      break
  }
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        const res = await userApi.updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        if (res.success) {
          ElMessage.success('密码修改成功，请重新登录')
          passwordDialogVisible.value = false
          adminStore.logout()
          router.push('/admin/login')
        } else {
          ElMessage.error(res.message || '密码修改失败')
        }
      } catch (error) {
        ElMessage.error('密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5b;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}

.admin-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
}

.user-info .el-icon {
  margin-right: 5px;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
