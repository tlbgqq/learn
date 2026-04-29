<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon role-icon">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ roleCount }}</div>
            <div class="stat-label">角色总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon menu-icon">
            <el-icon><Menu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ menuCount }}</div>
            <div class="stat-label">菜单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon online-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ onlineCount }}</div>
            <div class="stat-label">在线用户</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="quick-action" @click="goToPage('/admin/user')">
                <el-icon class="action-icon user-action"><UserFilled /></el-icon>
                <div class="action-text">用户管理</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="quick-action" @click="goToPage('/admin/role')">
                <el-icon class="action-icon role-action"><Avatar /></el-icon>
                <div class="action-text">角色管理</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="quick-action" @click="goToPage('/admin/menu')">
                <el-icon class="action-icon menu-action"><Menu /></el-icon>
                <div class="action-text">菜单管理</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="info-label">系统名称:</span>
              <span class="info-value">后台管理系统</span>
            </div>
            <div class="info-item">
              <span class="info-label">系统版本:</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">技术栈:</span>
              <span class="info-value">Vue3 + Spring Boot</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前用户:</span>
              <span class="info-value">{{ currentUser }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {Avatar, Connection, Menu, User, UserFilled} from '@element-plus/icons-vue'
import {useAdminStore} from '@/store/admin'
import {menuApi, roleApi, userApi} from '@/api/admin'

const router = useRouter()
const adminStore = useAdminStore()

const userCount = ref(0)
const roleCount = ref(0)
const menuCount = ref(0)
const onlineCount = ref(1)

const currentUser = computed(() => adminStore.userInfo?.username || '管理员')

const goToPage = (path) => {
  router.push(path)
}

onMounted(async () => {
  try {
    const [userRes, roleRes, menuRes] = await Promise.all([
      userApi.list({ current: 1, size: 1 }),
      roleApi.list({ current: 1, size: 1 }),
      menuApi.getTree()
    ])
    
    if (userRes.success) {
      userCount.value = userRes.data.total
    }
    if (roleRes.success) {
      roleCount.value = roleRes.data.total
    }
    if (menuRes.success) {
      menuCount.value = countMenus(menuRes.data)
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
})

const countMenus = (menus) => {
  let count = menus.length
  menus.forEach(menu => {
    if (menu.children && menu.children.length > 0) {
      count += countMenus(menu.children)
    }
  })
  return count
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
  margin-right: 20px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.role-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.menu-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.online-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.quick-action {
  text-align: center;
  padding: 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.quick-action:hover {
  background-color: #f5f7fa;
}

.action-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.user-action {
  color: #409eff;
}

.role-action {
  color: #67c23a;
}

.menu-action {
  color: #e6a23c;
}

.action-text {
  font-size: 14px;
  color: #666;
}

.system-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #999;
}

.info-value {
  color: #333;
  font-weight: 500;
}
</style>
