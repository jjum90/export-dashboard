import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: {
        title: '대시보드'
      }
    },
    {
      path: '/countries',
      name: 'countries',
      component: () => import('@/views/CountriesView.vue'),
      meta: {
        title: '국가별 통계'
      }
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/ProductsView.vue'),
      meta: {
        title: '품목별 통계'
      }
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: () => import('@/views/AnalyticsView.vue'),
      meta: {
        title: '분석'
      }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  // Set page title
  if (to.meta.title) {
    document.title = `${to.meta.title} - 대한민국 수출 통계 대시보드`
  }
  next()
})

export default router