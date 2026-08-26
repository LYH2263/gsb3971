<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { createCareLevel, createCareRecord, getCareLevels, getCareRecords, updateCareLevelStatus } from '@/api/care';
import { getCustomers } from '@/api/customers';
import type { CareLevel, CareRecord, Customer } from '@/types';

const careLevels = ref<CareLevel[]>([]);
const careRecords = ref<CareRecord[]>([]);
const customers = ref<Customer[]>([]);

const careLevelForm = reactive({
  name: '',
  description: '',
  status: 1
});

const careRecordForm = reactive({
  customerId: undefined as number | undefined,
  careDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  content: ''
});

async function fetchBaseData() {
  try {
    [customers.value, careLevels.value] = await Promise.all([getCustomers(), getCareLevels()]);
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function fetchCareRecords() {
  try {
    careRecords.value = await getCareRecords({ customerId: careRecordForm.customerId });
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function submitCareLevel() {
  try {
    await createCareLevel(careLevelForm);
    ElMessage.success('带教档位新增成功');
    Object.assign(careLevelForm, { name: '', description: '', status: 1 });
    careLevels.value = await getCareLevels();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function toggleCareLevel(level: CareLevel, value: number) {
  try {
    await updateCareLevelStatus(level.id, value);
    ElMessage.success('带教档位状态更新成功');
    careLevels.value = await getCareLevels();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function submitCareRecord() {
  if (!careRecordForm.customerId) {
    ElMessage.warning('请选择学员');
    return;
  }
  try {
    await createCareRecord({
      customerId: careRecordForm.customerId,
      careDate: careRecordForm.careDate,
      content: careRecordForm.content
    });
    ElMessage.success('辅导记录保存成功');
    careRecordForm.content = '';
    careRecordForm.careDate = dayjs().format('YYYY-MM-DD HH:mm:ss');
    await fetchCareRecords();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

onMounted(async () => {
  await fetchBaseData();
  await fetchCareRecords();
});
</script>

<template>
  <div class="grid-two" data-testid="page-care">
    <div class="page-card" data-testid="care-level-card">
      <div class="page-header">
        <h2 class="page-title">带教档位</h2>
      </div>

      <el-form label-width="90px">
        <el-form-item label="级别名称">
          <el-input v-model="careLevelForm.name" data-testid="care-level-name" placeholder="例如 一级带教" />
        </el-form-item>
        <el-form-item label="级别描述">
          <el-input v-model="careLevelForm.description" data-testid="care-level-description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <el-button data-testid="care-level-create-btn" type="primary" @click="submitCareLevel">新增级别</el-button>

      <el-divider />

      <el-table :data="careLevels" data-testid="care-level-table" border>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :data-testid="`care-level-status-${row.id}`"
              :active-value="1"
              :inactive-value="0"
              @change="(value: number) => toggleCareLevel(row, value)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card" data-testid="care-record-card">
      <div class="page-header">
        <h2 class="page-title">辅导记录</h2>
      </div>

      <el-form label-width="90px">
        <el-form-item label="学员">
          <el-select v-model="careRecordForm.customerId" data-testid="care-record-customer-id" filterable style="width: 100%" @change="fetchCareRecords">
            <el-option v-for="item in customers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="辅导时间">
          <div data-testid="care-record-date" style="width: 100%">
            <el-date-picker
              v-model="careRecordForm.careDate"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </div>
        </el-form-item>
        <el-form-item label="辅导内容">
          <el-input
            v-model="careRecordForm.content"
            data-testid="care-record-content"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <el-button data-testid="care-record-create-btn" type="primary" plain @click="submitCareRecord">保存辅导记录</el-button>

      <el-divider />

      <el-table :data="careRecords" data-testid="care-record-table" border>
        <el-table-column prop="careDate" label="辅导时间" min-width="150" />
        <el-table-column prop="customerName" label="学员" min-width="100" />
        <el-table-column prop="content" label="辅导内容" min-width="200" />
        <el-table-column prop="performerName" label="执行人" min-width="100" />
      </el-table>
    </div>
  </div>
</template>
