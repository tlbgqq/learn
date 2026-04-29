import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/admin/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/student',
    component: () => import('@/views/student/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/student/home'
      },
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue')
      },
      {
        path: 'practice',
        name: 'Practice',
        component: () => import('@/views/student/Practice.vue')
      },
      {
        path: 'wrong-answers',
        name: 'WrongAnswers',
        component: () => import('@/views/student/WrongAnswers.vue')
      },
      {
        path: 'achievements',
        name: 'Achievements',
        component: () => import('@/views/student/Achievements.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/student/Profile.vue')
      },
      {
        path: 'sprint',
        name: 'Sprint',
        component: () => import('@/views/student/SprintIndex.vue')
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { requiresAuth: false, requiresAdminAuth: false }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAdminAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/admin/system/user/index.vue')
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/admin/system/role/index.vue')
      },
      {
        path: 'menu',
        name: 'MenuManage',
        component: () => import('@/views/admin/system/menu/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isStudentAuthenticated = localStorage.getItem('token')
  const isAdminAuthenticated = localStorage.getItem('admin_token')
  
  if (to.matched.some(record => record.meta.requiresAdminAuth === true)) {
    if (!isAdminAuthenticated) {
      next({ name: 'AdminLogin' })
    } else {
      next()
    }
  } else if (to.matched.some(record => record.meta.requiresAuth === true)) {
    if (!isStudentAuthenticated) {
      next({ name: 'Login' })
    } else {
      next()
    }
  } else {
    if (isStudentAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
      next({ name: 'StudentHome' })
    } else if (isAdminAuthenticated && to.name === 'AdminLogin') {
      next({ name: 'AdminDashboard' })
    } else {
      next()
    }
  }
})

export default router
