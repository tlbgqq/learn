import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  
  if (to.matched.some(record => record.meta.requiresAuth !== false)) {
    if (!isAuthenticated) {
      next({ name: 'Login' })
    } else {
      next()
    }
  } else {
    if (isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
      next({ name: 'StudentHome' })
    } else {
      next()
    }
  }
})

export default router
