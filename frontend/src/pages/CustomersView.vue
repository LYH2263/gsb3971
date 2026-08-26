<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { createCustomer, getCustomers, updateLifecycle } from '@/api/customers';
import { getRooms } from '@/api/rooms';
import type { Bed, Customer } from '@/types';
import dayjs from 'dayjs';

const loading = ref(false);
const customers = ref<Customer[]>([]);
const beds = ref<Bed[]>([]);
const keyword = ref('');
const filterStatus = ref('');

const createVisible = ref(false);
const createRef = ref<FormInstance>();
const createForm = reactive({
  name: '',
  phone: '',
  age: 70,
  gender: 1,
  note: ''
});

const lifecycleVisible = ref(false);
const lifecycleMode = ref<'checkin' | 'discharge' | 'outing'>('checkin');
const currentCustomer = ref<Customer | null>(null);
const lifecycleForm = reactive({
  actionDate: dayjs().format('YYYY-MM-DD'),
  bedId: undefined as number | undefined,
  reason: ''
});

const createRules: FormRules = {
  name: [{ required: true, message: '请输入学员姓名', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
};

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'DRAFT', label: '待入营' },
  { value: 'RESIDENT', label: '在营' },
  { value: 'OUTING', label: '离营外出' },
  { value: 'DISCHARGED', label: '已结营' }
];

const availableBeds = computed(() => beds.value.filter((item) => item.status === 'AVAILABLE'));

function statusType(status: Customer['status']) {
  if (status === 'RESIDENT') {
    return 'success';
  }
  if (status === 'OUTING') {
    return 'warning';
  }
  if (status === 'DISCHARGED') {
    return 'info';
  }
  return '';
}

function statusText(status: Customer['status']) {
  if (status === 'RESIDENT') {
    return '在营';
  }
  if (status === 'OUTING') {
    return '离营外出';
  }
  if (status === 'DISCHARGED') {
    return '已结营';
  }
  return '待入营';
}

async function fetchCustomers() {
  loading.value = true;
  try {
    customers.value = await getCustomers({
      status: filterStatus.value || undefined,
      keyword: keyword.value || undefined
    });
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}

async function fetchBeds() {
  try {
    const rooms = await getRooms(true);
    beds.value = rooms.flatMap((room) => room.beds || []);
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function openCreateDialog() {
  createVisible.value = true;
}

async function submitCreate() {
  if (!createRef.value) {
    return;
  }
  await createRef.value.validate();
  try {
    await createCustomer(createForm);
    ElMessage.success('学员创建成功');
    createVisible.value = false;
    Object.assign(createForm, { name: '', phone: '', age: 70, gender: 1, note: '' });
    await fetchCustomers();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

function openLifecycle(customer: Customer, mode: 'checkin' | 'discharge' | 'outing') {
  currentCustomer.value = customer;
  lifecycleMode.value = mode;
  lifecycleForm.actionDate = dayjs().format('YYYY-MM-DD');
  lifecycleForm.bedId = undefined;
  lifecycleForm.reason = '';
  lifecycleVisible.value = true;
}

async function submitLifecycle() {
  if (!currentCustomer.value) {
    return;
  }
  if (lifecycleMode.value === 'checkin' && !lifecycleForm.bedId) {
    ElMessage.warning('入营需选择铺位');
    return;
  }

  try {
    await updateLifecycle(currentCustomer.value.id, {
      action: lifecycleMode.value,
      actionDate: lifecycleForm.actionDate,
      bedId: lifecycleForm.bedId,
      reason: lifecycleForm.reason || undefined
    });
    ElMessage.success('状态更新成功');
    lifecycleVisible.value = false;
    await Promise.all([fetchCustomers(), fetchBeds()]);
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function quickOuting(customer: Customer) {
  try {
    await ElMessageBox.confirm('确认登记该学员离营外出？', '提示', { type: 'warning' });
    await updateLifecycle(customer.id, {
      action: 'outing',
      actionDate: dayjs().format('YYYY-MM-DD')
    });
    ElMessage.success('离营离营外出登记成功');
    await fetchCustomers();
  } catch {
    // ignore cancel
  }
}

onMounted(async () => {
  await Promise.all([fetchCustomers(), fetchBeds()]);
});
</script>

<template>
  <div class="page-card" data-testid="page-customers">
    <div class="page-header">
      <h2 class="page-title">学员档案</h2>
      <div style="display: flex; gap: 8px">
        <el-button data-testid="customers-refresh" @click="fetchCustomers">刷新</el-button>
        <el-button data-testid="customer-create-open" type="primary" @click="openCreateDialog">新增学员</el-button>
      </div>
    </div>

    <div style="display: flex; gap: 8px; margin-bottom: 12px">
      <el-select v-model="filterStatus" data-testid="customer-filter-status" style="width: 180px" @change="fetchCustomers">
        <el-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
      </el-select>
      <el-input
        v-model="keyword"
        data-testid="customer-filter-keyword"
        placeholder="按姓名或手机号搜索"
        clearable
        @keyup.enter="fetchCustomers"
        @clear="fetchCustomers"
      />
      <el-button data-testid="customer-filter-search" type="primary" plain @click="fetchCustomers">查询</el-button>
    </div>

    <el-table :data="customers" v-loading="loading" data-testid="customers-table" border>
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="铺位" min-width="140">
        <template #default="{ row }">
          <span v-if="row.bedNo">{{ row.roomNo }} / {{ row.bedNo }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="note" label="备注" min-width="180" />
      <el-table-column label="操作" min-width="260" fixed="right">
        <template #default="{ row }">
          <el-button :data-testid="`customer-checkin-${row.id}`" link type="primary" @click="openLifecycle(row, 'checkin')">入营</el-button>
          <el-button
            :data-testid="`customer-outing-${row.id}`"
            link
            type="warning"
            :disabled="row.status !== 'RESIDENT'"
            @click="quickOuting(row)"
          >
            离营外出
          </el-button>
          <el-button
            :data-testid="`customer-discharge-${row.id}`"
            link
            type="danger"
            :disabled="!['RESIDENT', 'OUTING'].includes(row.status)"
            @click="openLifecycle(row, 'discharge')"
          >
            结营
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="createVisible" data-testid="customer-create-dialog" title="新增学员" width="520px">
    <el-form ref="createRef" :model="createForm" :rules="createRules" data-testid="customer-create-form" label-width="90px">
      <el-form-item label="学员姓名" prop="name">
        <el-input v-model="createForm.name" data-testid="customer-create-name" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="createForm.phone" data-testid="customer-create-phone" />
      </el-form-item>
      <el-form-item label="年龄" prop="age">
        <el-input-number v-model="createForm.age" data-testid="customer-create-age" :min="1" :max="120" style="width: 100%" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="createForm.gender" data-testid="customer-create-gender" style="width: 100%">
          <el-option :value="1" label="男" />
          <el-option :value="2" label="女" />
          <el-option :value="0" label="未知" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="note">
        <el-input v-model="createForm.note" data-testid="customer-create-note" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="customer-create-cancel" @click="createVisible = false">取消</el-button>
      <el-button data-testid="customer-create-submit" type="primary" @click="submitCreate">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="lifecycleVisible"
    data-testid="customer-lifecycle-dialog"
    :title="lifecycleMode === 'checkin' ? '办理入营' : '办理结营'"
    width="520px"
  >
    <el-form data-testid="customer-lifecycle-form" label-width="90px">
      <el-form-item label="办理日期">
        <div data-testid="customer-lifecycle-date" style="width: 100%">
          <el-date-picker
            v-model="lifecycleForm.actionDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </div>
      </el-form-item>
      <el-form-item v-if="lifecycleMode === 'checkin'" label="选择铺位">
        <el-select v-model="lifecycleForm.bedId" data-testid="customer-lifecycle-bed" style="width: 100%" placeholder="请选择空闲铺位">
          <el-option
            v-for="item in availableBeds"
            :key="item.id"
            :value="item.id"
            :label="`${item.roomNo}/${item.bedNo}`"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="lifecycleForm.reason"
          data-testid="customer-lifecycle-reason"
          type="textarea"
          :rows="3"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button data-testid="customer-lifecycle-cancel" @click="lifecycleVisible = false">取消</el-button>
      <el-button data-testid="customer-lifecycle-submit" type="primary" @click="submitLifecycle">提交</el-button>
    </template>
  </el-dialog>
</template>
