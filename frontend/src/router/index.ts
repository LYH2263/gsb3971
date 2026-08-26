import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';

const routes = [
  {
    path: '/',
    redirect: '/customers'
  },
  {
    path: '/login',
    component: () => import('@/pages/LoginView.vue')
  },
  {
    path: '/customers',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/CustomersView.vue')
      }
    ]
  },
  {
    path: '/rooms',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/RoomsView.vue')
      }
    ]
  },
  {
    path: '/meals',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/MealsView.vue')
      }
    ]
  },
  {
    path: '/care',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/CareView.vue')
      }
    ]
  },
  {
    path: '/service-focus',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/ServiceFocusView.vue')
      }
    ]
  },
  {
    path: '/users',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        component: () => import('@/pages/UsersView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/customers'
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const token = localStorage.getItem('elderly_token');
  const userRaw = localStorage.getItem('elderly_user');
  const user = userRaw ? JSON.parse(userRaw) : null;
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录');
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    };
  }
  if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    ElMessage.error('仅管理员可访问该页面');
    return '/customers';
  }
  return true;
});
