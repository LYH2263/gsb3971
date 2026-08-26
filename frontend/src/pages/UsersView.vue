<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUsers, updateUserStatus } from '@/api/users';
import { useAuthStore } from '@/stores/auth';
import type { UserInfo } from '@/types';

const authStore = useAuthStore();
const loading = ref(false);
const users = ref<UserInfo[]>([]);

const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  STAFF: '员工'
};

function roleLabel(role: string) {
  return roleLabelMap[role] || role;
}

async function fetchUsers() {
  if (!authStore.isAdmin) {
    return;
  }
  loading.value = true;
  try {
    users.value = await getUsers();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}

async function toggleStatus(user: UserInfo, status: number) {
  try {
    await updateUserStatus(user.id, status);
    ElMessage.success('用户状态更新成功');
    await fetchUsers();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

onMounted(fetchUsers);
</script>

<template>
  <div class="page-card" data-testid="page-users">
    <div class="page-header">
      <h2 class="page-title">营务账号</h2>
      <el-button data-testid="users-refresh" @click="fetchUsers">刷新</el-button>
    </div>

    <el-alert
      v-if="!authStore.isAdmin"
      data-testid="users-no-admin-alert"
      type="warning"
      show-icon
      title="仅管理员可查看并维护用户状态"
      :closable="false"
      style="margin-bottom: 12px"
    />

    <el-table v-else :data="users" v-loading="loading" data-testid="users-table" border>
      <el-table-column prop="realName" label="姓名" min-width="100" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          {{ roleLabel(row.role) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status"
            :data-testid="`users-status-${row.id}`"
            :active-value="1"
            :inactive-value="0"
            @change="(value: number) => toggleStatus(row, value)"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
