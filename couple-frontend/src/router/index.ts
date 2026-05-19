import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { auth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: 'bind',
          name: 'bind',
          component: () => import('@/views/BindView.vue'),
        },
        {
          path: 'memorial',
          name: 'memorial',
          component: () => import('@/views/MemorialView.vue'),
        },
        {
          path: 'post',
          name: 'post',
          component: () => import('@/views/PostView.vue'),
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/SettingsView.vue'),
        },
        {
          path: 'wish',
          name: 'wish',
          component: () => import('@/views/WishView.vue'),
        },
        {
          path: 'location',
          name: 'location',
          component: () => import('@/views/LocationView.vue'),
        },
      ]
    },
  ]
})

router.beforeEach((to, _from) => {
  const store = useUserStore()

  if (to.meta.auth && !store.token) {
    return '/login'
  }
  if (to.meta.guest && store.token) {
    return '/'
  }
})

export default router
