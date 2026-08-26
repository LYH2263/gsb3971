<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { getCustomers } from '@/api/customers';
import {
  getCustomerMealPlan,
  getWeeklyMenu,
  listCustomerMealPlans,
  saveCustomerMealPlan,
  saveWeeklyMenu
} from '@/api/meals';
import type { Customer, CustomerMealPlan } from '@/types';

const loading = ref(false);
const customers = ref<Customer[]>([]);
const plans = ref<CustomerMealPlan[]>([]);

function getDefaultWeekStartDate() {
  const now = dayjs();
  const offset = (now.day() + 6) % 7;
  return now.subtract(offset, 'day').format('YYYY-MM-DD');
}

const weekStartDate = ref(getDefaultWeekStartDate());

const menuForm = reactive({
  mon: '',
  tue: '',
  wed: '',
  thu: '',
  fri: '',
  sat: '',
  sun: ''
});

const planForm = reactive({
  customerId: undefined as number | undefined,
  mealType: 'NORMAL',
  dietTaboo: '',
  note: ''
});

const mealTypeOptions = [
  { label: '普通餐', value: 'NORMAL' },
  { label: '糖尿病餐', value: 'DIABETIC' },
  { label: '低盐餐', value: 'LOW_SALT' },
  { label: '其他', value: 'OTHER' }
];

const mealTypeLabelMap: Record<string, string> = {
  NORMAL: '普通餐',
  DIABETIC: '糖尿病餐',
  LOW_SALT: '低盐餐',
  OTHER: '其他'
};

function mealTypeLabel(mealType: string) {
  return mealTypeLabelMap[mealType] || mealType;
}

async function fetchCustomers() {
  customers.value = await getCustomers();
}

async function fetchMenu() {
  loading.value = true;
  try {
    const menu = await getWeeklyMenu(weekStartDate.value);
    Object.assign(menuForm, {
      mon: menu.mon || '',
      tue: menu.tue || '',
      wed: menu.wed || '',
      thu: menu.thu || '',
      fri: menu.fri || '',
      sat: menu.sat || '',
      sun: menu.sun || ''
    });
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}

async function fetchPlans() {
  try {
    plans.value = await listCustomerMealPlans({ weekStartDate: weekStartDate.value });
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function handleWeekChange() {
  await Promise.all([fetchMenu(), fetchPlans()]);
  if (planForm.customerId) {
    await loadCustomerPlan();
  }
}

async function submitMenu() {
  try {
    await saveWeeklyMenu(weekStartDate.value, menuForm);
    ElMessage.success('周菜单保存成功');
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function loadCustomerPlan() {
  if (!planForm.customerId) {
    return;
  }
  try {
    const plan = await getCustomerMealPlan(planForm.customerId, weekStartDate.value);
    planForm.mealType = plan.mealType || 'NORMAL';
    planForm.dietTaboo = plan.dietTaboo || '';
    planForm.note = plan.note || '';
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function submitPlan() {
  if (!planForm.customerId) {
    ElMessage.warning('请先选择学员');
    return;
  }
  try {
    await saveCustomerMealPlan(planForm.customerId, weekStartDate.value, {
      mealType: planForm.mealType,
      dietTaboo: planForm.dietTaboo,
      note: planForm.note
    });
    ElMessage.success('学员配餐定制保存成功');
    await fetchPlans();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

onMounted(async () => {
  await fetchCustomers();
  await handleWeekChange();
});
</script>

<template>
  <div class="grid-two" data-testid="page-meals">
    <div class="page-card" data-testid="weekly-menu-card">
      <div class="page-header">
        <h2 class="page-title">配餐日历</h2>
        <div data-testid="menu-week-start-date">
          <el-date-picker
            v-model="weekStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择周起始日期"
            @change="handleWeekChange"
          />
        </div>
      </div>

      <el-form label-width="90px">
        <el-form-item label="周一"><el-input v-model="menuForm.mon" data-testid="menu-mon" /></el-form-item>
        <el-form-item label="周二"><el-input v-model="menuForm.tue" data-testid="menu-tue" /></el-form-item>
        <el-form-item label="周三"><el-input v-model="menuForm.wed" data-testid="menu-wed" /></el-form-item>
        <el-form-item label="周四"><el-input v-model="menuForm.thu" data-testid="menu-thu" /></el-form-item>
        <el-form-item label="周五"><el-input v-model="menuForm.fri" data-testid="menu-fri" /></el-form-item>
        <el-form-item label="周六"><el-input v-model="menuForm.sat" data-testid="menu-sat" /></el-form-item>
        <el-form-item label="周日"><el-input v-model="menuForm.sun" data-testid="menu-sun" /></el-form-item>
      </el-form>

      <el-button data-testid="menu-save-btn" type="primary" :loading="loading" @click="submitMenu">保存周菜单</el-button>
    </div>

    <div class="page-card" data-testid="customer-meal-plan-card">
      <div class="page-header">
        <h2 class="page-title">学员配餐定制</h2>
      </div>

      <el-form label-width="90px">
        <el-form-item label="学员">
          <el-select
            v-model="planForm.customerId"
            data-testid="meal-plan-customer-id"
            filterable
            placeholder="请选择学员"
            style="width: 100%"
            @change="loadCustomerPlan"
          >
            <el-option v-for="customer in customers" :key="customer.id" :value="customer.id" :label="customer.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="餐型">
          <el-select v-model="planForm.mealType" data-testid="meal-plan-meal-type" style="width: 100%">
            <el-option v-for="item in mealTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="忌口信息">
          <el-input v-model="planForm.dietTaboo" data-testid="meal-plan-diet-taboo" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.note" data-testid="meal-plan-note" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <el-button data-testid="meal-plan-save-btn" type="primary" plain @click="submitPlan">保存学员定制</el-button>

      <el-divider />

      <el-table :data="plans" data-testid="meal-plan-table" border>
        <el-table-column prop="customerName" label="学员" min-width="100" />
        <el-table-column label="餐型" min-width="100">
          <template #default="{ row }">
            {{ mealTypeLabel(row.mealType) }}
          </template>
        </el-table-column>
        <el-table-column prop="dietTaboo" label="忌口" min-width="140" />
        <el-table-column prop="note" label="备注" min-width="140" />
      </el-table>
    </div>
  </div>
</template>
