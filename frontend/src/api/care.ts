import { http } from '@/api/http';
import type { CareLevel, CareRecord } from '@/types';

export interface CreateCareLevelPayload {
  name: string;
  description?: string;
  status?: number;
}

export interface CreateCareRecordPayload {
  customerId: number;
  careDate: string;
  content: string;
}

export function getCareLevels() {
  return http.get<unknown, CareLevel[]>('/care-levels');
}

export function createCareLevel(payload: CreateCareLevelPayload) {
  return http.post<unknown, CareLevel>('/care-levels', payload);
}

export function updateCareLevelStatus(id: number, status: number) {
  return http.patch<unknown, CareLevel>(`/care-levels/${id}/status`, { status });
}

export function getCareRecords(params?: { customerId?: number; from?: string; to?: string }) {
  return http.get<unknown, CareRecord[]>('/care-records', { params });
}

export function createCareRecord(payload: CreateCareRecordPayload) {
  return http.post<unknown, CareRecord>('/care-records', payload);
}
