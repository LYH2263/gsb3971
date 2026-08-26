import { http } from '@/api/http';
import type { UserInfo } from '@/types';

export function getUsers() {
  return http.get<unknown, UserInfo[]>('/users');
}

export function updateUserStatus(id: number, status: number) {
  return http.patch<unknown, UserInfo>(`/users/${id}/status`, { status });
}
