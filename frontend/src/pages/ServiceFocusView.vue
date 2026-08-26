<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { getCustomers } from '@/api/customers';
import { assignServiceObject, createServiceFocus, listServiceFocuses, listServiceObjects } from '@/api/services';
import { getUsers } from '@/api/users';
import type { Customer, ServiceFocusRecord, ServiceObjectRelation, UserInfo } from '@/types';

const customers = ref<Customer[]>([]);
const managers = ref<UserInfo[]>([]);
const serviceObjects = ref<ServiceObjectRelation[]>([]);
const serviceFocuses = ref<ServiceFocusRecord[]>([]);

const objectLoading = ref(false);
const focusLoading = ref(false);
const objectDialogVisible = ref(false);

const focusFilterCustomerId = ref<number | undefined>(undefined);

const objectForm = reactive({
  customerId: undefined as number | undefined,
  managerUserId: undefined as number | undefined
});

const focusForm = reactive({
  customerId: undefined as number | undefined,
  serviceName: '',
  purchaseDate: dayjs().format('YYYY-MM-DD'),
  expireDate: '',
  serviceStatus: 'ACTIVE' as 'ACTIVE' | 'PAUSED' | 'ENDED',
  note: ''
});

const serviceStatusOptions = [
  { label: '生效中', value: 'ACTIVE' },
  { label: '暂停', value: 'PAUSED' },
  { label: '结束', value: 'ENDED' }
];

const serviceStatusLabelMap: Record<string, string> = {
  ACTIVE: '生效中',
  PAUSED: '暂停',
  ENDED: '结束'
};

const customerNameMap = computed(() => {
  const entries = customers.value.map((customer) => [customer.id, customer.name] as const);
  return new Map<number, string>(entries);
});

function serviceStatusLabel(status: string) {
  return serviceStatusLabelMap[status] || status;
}

async function fetchBaseData() {
  try {
    const [customerList, userList] = await Promise.all([getCustomers(), getUsers()]);
    customers.value = customerList;
    managers.value = userList.filter((user) => user.role === 'STAFF' && user.status === 1);
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function fetchServiceObjects() {
  objectLoading.value = true;
  try {
    serviceObjects.value = await listServiceObjects();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    objectLoading.value = false;
  }
}

async function fetchServiceFocuses() {
  focusLoading.value = true;
  try {
    serviceFocuses.value = await listServiceFocuses({ customerId: focusFilterCustomerId.value });
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    focusLoading.value = false;
  }
}

function openObjectDialog(row?: ServiceObjectRelation) {
  objectForm.customerId = row?.customerId;
  objectForm.managerUserId = row?.managerUserId;
  objectDialogVisible.value = true;
}

async function submitObjectSetting() {
  if (!objectForm.customerId) {
    ElMessage.warning('请选择学员');
    return;
  }
  if (!objectForm.managerUserId) {
    ElMessage.warning('请选择带队导师');
    return;
  }

  try {
    await assignServiceObject(objectForm.customerId, { managerUserId: objectForm.managerUserId });
    ElMessage.success('带教配对设置成功');
    objectDialogVisible.value = false;
    await fetchServiceObjects();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function submitServiceFocus() {
  if (!focusForm.customerId) {
    ElMessage.warning('请选择学员');
    return;
  }
  if (!focusForm.serviceName.trim()) {
    ElMessage.warning('请输入项目名称');
    return;
  }
  if (!focusForm.purchaseDate) {
    ElMessage.warning('请选择报名日期');
    return;
  }
  if (focusForm.expireDate && focusForm.expireDate < focusForm.purchaseDate) {
    ElMessage.warning('到期日期不能早于报名日期');
    return;
  }

  try {
    await createServiceFocus({
      customerId: focusForm.customerId,
      serviceName: focusForm.serviceName.trim(),
      purchaseDate: focusForm.purchaseDate,
      expireDate: focusForm.expireDate || undefined,
      serviceStatus: focusForm.serviceStatus,
      note: focusForm.note || undefined
    });
    ElMessage.success('研学增值包保存成功');
    focusForm.serviceName = '';
    focusForm.expireDate = '';
    focusForm.note = '';
    await fetchServiceFocuses();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

onMounted(async () => {
  await fetchBaseData();
  await Promise.all([fetchServiceObjects(), fetchServiceFocuses()]);
});
</script>

<template>
  <div class="grid-two" data-testid="page-service-focus">
    <div class="page-card" data-testid="service-object-card">
      <div class="page-header">
        <h2 class="page-title">设置带教配对</h2>
        <el-button data-testid="service-object-open" type="primary" plain @click="openObjectDialog()">设置带教配对</el-button>
      </div>
      <el-table :data="serviceObjects" v-loading="objectLoading" data-testid="service-object-table" border>
        <el-table-column prop="customerName" label="学员" min-width="120" />
        <el-table-column label="带队导师" min-width="120">
          <template #default="{ row }">
            {{ row.managerName || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column label="导师手机号" min-width="130">
          <template #default="{ row }">
            {{ row.managerPhone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assignedAt" label="设置时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button :data-testid="`service-object-edit-${row.customerId}`" link type="primary" @click="openObjectDialog(row)">设置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card" data-testid="service-focus-card">
      <div class="page-header">
        <h2 class="page-title">研学增值包</h2>
        <div style="display: flex; gap: 8px">
          <el-select
            v-model="focusFilterCustomerId"
            data-testid="service-focus-filter-customer"
            clearable
            placeholder="按学员筛选"
            style="width: 180px"
            @change="fetchServiceFocuses"
          >
            <el-option v-for="customer in customers" :key="customer.id" :label="customer.name" :value="customer.id" />
          </el-select>
          <el-button data-testid="service-focus-refresh" @click="fetchServiceFocuses">刷新</el-button>
        </div>
      </div>

      <el-form label-width="90px" style="margin-bottom: 12px">
        <el-form-item label="学员">
          <el-select v-model="focusForm.customerId" data-testid="service-focus-customer" placeholder="请选择学员" style="width: 100%">
            <el-option v-for="customer in customers" :key="customer.id" :label="customer.name" :value="customer.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="focusForm.serviceName" data-testid="service-focus-name" placeholder="例如 潮汐观测套餐" />
        </el-form-item>
        <el-form-item label="报名日期">
          <el-date-picker
            v-model="focusForm.purchaseDate"
            data-testid="service-focus-purchase-date"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker
            v-model="focusForm.expireDate"
            data-testid="service-focus-expire-date"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="focusForm.serviceStatus" data-testid="service-focus-status" style="width: 100%">
            <el-option v-for="item in serviceStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="focusForm.note" data-testid="service-focus-note" type="textarea" :rows="2" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>

      <el-button data-testid="service-focus-submit" type="primary" @click="submitServiceFocus">保存研学增值包</el-button>

      <el-divider />

      <el-table :data="serviceFocuses" v-loading="focusLoading" data-testid="service-focus-table" border>
        <el-table-column label="学员" min-width="100">
          <template #default="{ row }">
            {{ row.customerName || customerNameMap.get(row.customerId) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="serviceName" label="项目名称" min-width="140" />
        <el-table-column prop="purchaseDate" label="报名日期" min-width="110" />
        <el-table-column prop="expireDate" label="到期日期" min-width="110" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            {{ serviceStatusLabel(row.serviceStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="140" />
        <el-table-column prop="createdByName" label="录入人" min-width="90" />
        <el-table-column prop="createdAt" label="录入时间" min-width="160" />
      </el-table>
    </div>
  </div>

  <el-dialog v-model="objectDialogVisible" data-testid="service-object-dialog" title="设置带教配对" width="500px">
    <el-form label-width="90px">
      <el-form-item label="学员">
        <el-select v-model="objectForm.customerId" data-testid="service-object-customer" placeholder="请选择学员" style="width: 100%">
          <el-option v-for="customer in customers" :key="customer.id" :label="customer.name" :value="customer.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="带队导师">
        <el-select v-model="objectForm.managerUserId" data-testid="service-object-manager" placeholder="请选择带队导师" style="width: 100%">
          <el-option v-for="user in managers" :key="user.id" :label="`${user.realName}（${user.phone}）`" :value="user.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="service-object-cancel" @click="objectDialogVisible = false">取消</el-button>
      <el-button data-testid="service-object-submit" type="primary" @click="submitObjectSetting">保存</el-button>
    </template>
  </el-dialog>
</template>

