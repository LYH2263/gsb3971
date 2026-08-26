import axios from 'axios';
import type { ApiResponse } from '@/types';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

export const http = axios.create({
  baseURL,
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('elderly_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  ((response: any) => {
    const payload = response.data as ApiResponse<unknown>;
    if (typeof payload?.code === 'number' && payload.code !== 0) {
      return Promise.reject(new Error(payload.message || '请求失败'));
    }
    return payload.data;
  }) as any,
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      localStorage.removeItem('elderly_token');
      localStorage.removeItem('elderly_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    const message = error?.response?.data?.message || error?.message || '请求失败';
    return Promise.reject(new Error(message));
  }
);
