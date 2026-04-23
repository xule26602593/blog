import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // 前台页面
  {
    path: '/',
    component: () => import('@/views/portal/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/portal/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/portal/ArticleDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'category/:id',
        name: 'CategoryArticles',
        component: () => import('@/views/portal/CategoryArticles.vue'),
        meta: { title: '分类文章' }
      },
      {
        path: 'tag/:id',
        name: 'TagArticles',
        component: () => import('@/views/portal/TagArticles.vue'),
        meta: { title: '标签文章' }
      },
      {
        path: 'archives',
        name: 'Archives',
        component: () => import('@/views/portal/Archives.vue'),
        meta: { title: '归档' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/portal/Search.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'series',
        name: 'SeriesList',
        component: () => import('@/views/portal/SeriesList.vue'),
        meta: { title: '系列' }
      },
      {
        path: 'series/:id',
        name: 'SeriesDetail',
        component: () => import('@/views/portal/SeriesDetail.vue'),
        meta: { title: '系列详情' }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/portal/UserCenter.vue'),
        meta: { title: '个人中心', requiresAuth: true },
        redirect: '/user/profile',
        children: [
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/portal/UserProfile.vue'),
            meta: { title: '个人资料' }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('@/views/portal/UserFavorites.vue'),
            meta: { title: '我的收藏' }
          },
          {
            path: 'history',
            name: 'UserHistory',
            component: () => import('@/views/portal/UserHistory.vue'),
            meta: { title: '阅读历史' }
          }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/portal/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/portal/Register.vue'),
    meta: { title: '注册' }
  },
  // 管理后台
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'articles',
        name: 'ArticleManage',
        component: () => import('@/views/admin/ArticleManage.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'articles/edit/:id?',
        name: 'ArticleEdit',
        component: () => import('@/views/admin/ArticleEdit.vue'),
        meta: { title: '文章编辑' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/admin/TagManage.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'comments',
        name: 'CommentManage',
        component: () => import('@/views/admin/CommentManage.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'series',
        name: 'SeriesManage',
        component: () => import('@/views/admin/SeriesManage.vue'),
        meta: { title: '系列管理' }
      },
      {
        path: 'series/edit/:id?',
        name: 'SeriesEdit',
        component: () => import('@/views/admin/SeriesEdit.vue'),
        meta: { title: '系列编辑' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 我的博客` : '我的博客'

  try {
    const userStore = useUserStore()

    // 需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 需要管理员权限
    if (to.meta.requiresAdmin && userStore.roleCode !== 'ADMIN') {
      next({ name: 'Home' })
      return
    }

    // 已登录用户访问登录/注册页面
    if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
      next({ name: 'Home' })
      return
    }

    next()
  } catch (error) {
    // Pinia 实例未初始化时，重定向到登录页
    console.error('路由守卫访问 Store 失败:', error)
    if (to.meta.requiresAuth || to.meta.requiresAdmin) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  }
})

export default router
