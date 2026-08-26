<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { getRooms, saveBed, saveRoom } from '@/api/rooms';
import type { Room } from '@/types';

const loading = ref(false);
const rooms = ref<Room[]>([]);

const roomVisible = ref(false);
const roomRef = ref<FormInstance>();
const roomForm = reactive({
  floor: 1,
  roomNo: '',
  status: 1
});

const bedVisible = ref(false);
const bedForm = reactive({
  roomId: 0,
  bedNo: '',
  status: 'AVAILABLE'
});

const roomRules: FormRules = {
  floor: [{ required: true, message: '请输入楼层', trigger: 'change' }],
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }]
};

const bedStatusLabelMap: Record<string, string> = {
  AVAILABLE: '空闲',
  OCCUPIED: '占用',
  DISABLED: '停用'
};

function bedStatusLabel(status: string) {
  return bedStatusLabelMap[status] || status;
}

async function fetchRooms() {
  loading.value = true;
  try {
    rooms.value = await getRooms(true);
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}

function openRoomDialog() {
  roomVisible.value = true;
}

async function submitRoom() {
  if (!roomRef.value) {
    return;
  }
  await roomRef.value.validate();
  try {
    await saveRoom(roomForm);
    ElMessage.success('房间保存成功');
    roomVisible.value = false;
    Object.assign(roomForm, { floor: 1, roomNo: '', status: 1 });
    await fetchRooms();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

function openBedDialog(roomId: number) {
  bedVisible.value = true;
  bedForm.roomId = roomId;
  bedForm.bedNo = '';
  bedForm.status = 'AVAILABLE';
}

async function submitBed() {
  try {
    await saveBed(bedForm.roomId, {
      bedNo: bedForm.bedNo,
      status: bedForm.status
    });
    ElMessage.success('铺位保存成功');
    bedVisible.value = false;
    await fetchRooms();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function changeBedStatus(roomId: number, bedId: number, status: string) {
  try {
    await saveBed(roomId, { bedId, status });
    ElMessage.success('铺位状态更新成功');
    await fetchRooms();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

onMounted(fetchRooms);
</script>

<template>
  <div class="page-card" data-testid="page-rooms">
    <div class="page-header">
      <h2 class="page-title">营房铺位管理</h2>
      <div style="display: flex; gap: 8px">
        <el-button data-testid="rooms-refresh" @click="fetchRooms">刷新</el-button>
        <el-button data-testid="room-create-open" type="primary" @click="openRoomDialog">新增房间</el-button>
      </div>
    </div>

    <el-table :data="rooms" v-loading="loading" data-testid="rooms-table" row-key="id" border>
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column prop="roomNo" label="房间号" min-width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="铺位列表" min-width="380">
        <template #default="{ row }">
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <el-tag v-for="bed in row.beds" :key="bed.id" :type="bed.status === 'OCCUPIED' ? 'warning' : bed.status === 'DISABLED' ? 'info' : 'success'">
              {{ bed.bedNo }} - {{ bedStatusLabel(bed.status) }}
            </el-tag>
            <span v-if="!row.beds || row.beds.length === 0">暂无铺位</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
            <el-button :data-testid="`room-add-bed-${row.id}`" link type="primary" @click="openBedDialog(row.id)">新增铺位</el-button>
            <el-dropdown v-if="row.beds && row.beds.length > 0" trigger="click">
              <el-button :data-testid="`room-bed-status-trigger-${row.id}`" link type="primary">调整铺位状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="bed in row.beds"
                    :key="bed.id + '-available'"
                    :data-testid="`room-bed-status-available-${row.id}-${bed.id}`"
                    @click="changeBedStatus(row.id, bed.id, 'AVAILABLE')"
                  >
                    {{ bed.bedNo }} -> 设为空闲
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-for="bed in row.beds"
                    :key="bed.id + '-disabled'"
                    :data-testid="`room-bed-status-disabled-${row.id}-${bed.id}`"
                    @click="changeBedStatus(row.id, bed.id, 'DISABLED')"
                  >
                    {{ bed.bedNo }} -> 设为停用
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="roomVisible" data-testid="room-dialog" title="新增房间" width="460px">
    <el-form ref="roomRef" :model="roomForm" :rules="roomRules" data-testid="room-form" label-width="90px">
      <el-form-item label="楼层" prop="floor">
        <el-input-number v-model="roomForm.floor" data-testid="room-floor" :min="1" :max="100" style="width: 100%" />
      </el-form-item>
      <el-form-item label="房间号" prop="roomNo">
        <el-input v-model="roomForm.roomNo" data-testid="room-no" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="roomForm.status" data-testid="room-status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="room-cancel" @click="roomVisible = false">取消</el-button>
      <el-button data-testid="room-submit" type="primary" @click="submitRoom">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="bedVisible" data-testid="bed-dialog" title="新增铺位" width="460px">
    <el-form data-testid="bed-form" label-width="90px">
      <el-form-item label="铺位号">
        <el-input v-model="bedForm.bedNo" data-testid="bed-no" placeholder="例如 A铺" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="bedForm.status" data-testid="bed-status" style="width: 100%">
          <el-option value="AVAILABLE" label="空闲" />
          <el-option value="DISABLED" label="停用" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="bed-cancel" @click="bedVisible = false">取消</el-button>
      <el-button data-testid="bed-submit" type="primary" @click="submitBed">保存</el-button>
    </template>
  </el-dialog>
</template>
