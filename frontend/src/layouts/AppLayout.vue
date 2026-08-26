<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const menus = computed(() => {
  const baseMenus = [
    { path: '/customers', label: '学员档案' },
    { path: '/rooms', label: '营房铺位' },
    { path: '/meals', label: '营期配餐' },
    { path: '/care', label: '带教辅导' }
  ];
  if (authStore.isAdmin) {
    baseMenus.push({ path: '/service-focus', label: '研学增值包' });
    baseMenus.push({ path: '/users', label: '营务账号' });
  }
  return baseMenus;
});

const activeMenu = computed(() => route.path);

function navigate(path: string) {
  router.push(path);
}

function logout() {
  authStore.clearAuth();
  router.replace('/login');
}
</script>

<template>
  <el-container style="min-height: 100vh" data-testid="layout-root">
    <el-aside width="220px" style="background: linear-gradient(180deg, #1f3e72 0%, #294f8d 60%, #2f5a9f 100%)">
      <div data-testid="layout-brand" style="height: 64px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600">
        雾屿潮间带研学营
      </div>
      <el-menu data-testid="layout-menu" :default-active="activeMenu" background-color="transparent" text-color="#d8e5ff" active-text-color="#ffffff" @select="navigate">
        <el-menu-item
          v-for="menu in menus"
          :key="menu.path"
          :index="menu.path"
          :data-testid="`nav-${menu.path.replace('/', '')}`"
        >
          {{ menu.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header data-testid="layout-header" style="display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06)">
        <div data-testid="layout-title" style="font-size: 16px; font-weight: 600; color: #1d3355">营务工作台</div>
        <div style="display: flex; gap: 12px; align-items: center">
          <el-tag data-testid="layout-username" type="info">{{ authStore.user?.realName || '-' }}</el-tag>
          <el-button data-testid="nav-logout" type="primary" plain size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main data-testid="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
