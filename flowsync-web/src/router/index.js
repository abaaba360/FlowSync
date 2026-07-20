import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isLeader } from '@/utils/auth'

const routes = [
  {
    path: '/',
    redirect: '/home/overview'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/home',
    component: () => import('@/views/HomeView.vue'),
    children: [
      {
        path: 'overview',
        name: 'Overview',
        component: () => import('@/views/panels/OverviewPanel.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/panels/ProjectPanel.vue')
      },
      {
        path: 'ai-plan',
        name: 'AiTaskPlan',
        component: () => import('@/views/panels/AiTaskPlanPanel.vue'),
        meta: {
          leaderOnly: true
        }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/panels/TaskPanel.vue')
      },
      {
        path: 'task-logs',
        name: 'TaskLogs',
        component: () => import('@/views/panels/TaskLogPanel.vue')
      },
      {
        path: 'summaries',
        name: 'Summaries',
        component: () => import('@/views/panels/SummaryPanel.vue')
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('@/views/panels/MemberPanel.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/panels/ProfilePanel.vue')
      }
    ]
  },
  // 未知路径重定向
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home/overview'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const loggedIn = isLoggedIn()

  // 未登录访问非公开页面 → 跳转登录页
  if (!loggedIn && !to.meta.public) {
    next('/login')
    return
  }

  // 已登录访问登录页 → 跳转总览
  if (loggedIn && to.path === '/login') {
    next('/home/overview')
    return
  }

  // 普通成员访问 AI 任务拆解 → 跳转总览
  if (loggedIn && to.meta.leaderOnly && !isLeader()) {
    next('/home/overview')
    return
  }

  next()
})

export default router
