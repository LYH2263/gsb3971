<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import { login, register } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const activeTab = ref<'login' | 'register'>('login');
const loading = ref(false);
const loginRef = ref<FormInstance>();
const registerRef = ref<FormInstance>();

const loginForm = reactive({
  phone: '',
  password: ''
});

const registerForm = reactive({
  phone: '',
  password: '',
  realName: '',
  age: 60,
  gender: 1
});

const phoneRule = [
  { required: true, message: '请输入手机号', trigger: 'blur' },
  { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
];

const loginRules: FormRules = {
  phone: phoneRule,
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const registerRules: FormRules = {
  phone: phoneRule,
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在6到32位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
};

async function submitLogin() {
  if (!loginRef.value) {
    return;
  }
  await loginRef.value.validate();
  loading.value = true;
  try {
    const result = await login(loginForm);
    authStore.setAuth(result.token, result.user);
    ElMessage.success('登录成功');
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/customers';
    await router.replace(redirect);
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}

async function submitRegister() {
  if (!registerRef.value) {
    return;
  }
  await registerRef.value.validate();
  loading.value = true;
  try {
    await register(registerForm);
    ElMessage.success('注册成功，请登录');
    activeTab.value = 'login';
    loginForm.phone = registerForm.phone;
    loginForm.password = registerForm.password;
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div data-testid="page-login" style="min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px">
    <div data-testid="auth-card" class="page-card" style="width: min(100%, 460px)">
      <h2 data-testid="auth-title" style="margin-top: 0; margin-bottom: 16px; color: #1d3355">雾屿潮间带研学营运营台</h2>
      <el-tabs v-model="activeTab" data-testid="auth-tabs">
        <el-tab-pane label="登录" name="login" data-testid="tab-login">
          <el-form ref="loginRef" :model="loginForm" :rules="loginRules" label-position="top" data-testid="form-login">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="loginForm.phone" data-testid="login-phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" data-testid="login-password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button data-testid="login-submit" type="primary" :loading="loading" style="width: 100%" @click="submitLogin">登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register" data-testid="tab-register">
          <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-position="top" data-testid="form-register">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" data-testid="register-phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="registerForm.password" data-testid="register-password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="registerForm.realName" data-testid="register-real-name" placeholder="请输入真实姓名" />
            </el-form-item>
            <div class="grid-two">
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="registerForm.age" data-testid="register-age" :min="1" :max="120" style="width: 100%" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-select v-model="registerForm.gender" data-testid="register-gender" style="width: 100%">
                  <el-option :value="1" label="男" />
                  <el-option :value="2" label="女" />
                  <el-option :value="0" label="未知" />
                </el-select>
              </el-form-item>
            </div>
            <el-button data-testid="register-submit" type="primary" plain :loading="loading" style="width: 100%" @click="submitRegister">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
