import {defineStore} from 'pinia'
import {authApi, menuApi} from '@/api/admin'
import {computed, ref} from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_userInfo') || 'null'))
  const menuList = ref([])

  const isAuthenticated = computed(() => !!token.value)

  async function login(username, password) {
    const res = await authApi.login({ username, password })
    if (res.success) {
      token.value = res.data.token
      userInfo.value = res.data.userInfo
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_userInfo', JSON.stringify(res.data.userInfo))
      return res
    }
    throw new Error(res.message || '登录失败')
  }

  async function getUserInfo() {
    const res = await authApi.getUserInfo()
    if (res.success) {
      userInfo.value = res.data
      localStorage.setItem('admin_userInfo', JSON.stringify(res.data))
    }
    return res
  }

  async function getMenuList() {
    const res = await menuApi.getUserTree()
    if (res.success) {
      menuList.value = res.data
    }
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menuList.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_userInfo')
  }

  function hasPermission(permission) {
    if (!userInfo.value || !userInfo.value.permissions) {
      return false
    }
    return userInfo.value.permissions.includes(permission)
  }

  function hasRole(role) {
    if (!userInfo.value || !userInfo.value.roles) {
      return false
    }
    return userInfo.value.roles.includes(role)
  }

  return {
    token,
    userInfo,
    menuList,
    isAuthenticated,
    login,
    getUserInfo,
    getMenuList,
    logout,
    hasPermission,
    hasRole
  }
})
