import { http } from '@/api/http';
import type { LoginResponse, UserInfo } from '@/types';

export interface RegisterPayload {
  phone: string;
  password: string;
  realName: string;
  age: number;
  gender: number;
}

export interface LoginPayload {
  phone: string;
  password: string;
}

export function register(payload: RegisterPayload) {
  return http.post<unknown, UserInfo>('/auth/register', payload);
}

export function login(payload: LoginPayload) {
  return http.post<unknown, LoginResponse>('/auth/login', payload);
}
