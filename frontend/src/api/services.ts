import { http } from '@/api/http';
import type { ServiceFocusRecord, ServiceObjectRelation } from '@/types';

export interface AssignServiceObjectPayload {
  managerUserId: number;
}

export interface CreateServiceFocusPayload {
  customerId: number;
  serviceName: string;
  purchaseDate: string;
  expireDate?: string;
  serviceStatus: 'ACTIVE' | 'PAUSED' | 'ENDED';
  note?: string;
}

export function listServiceObjects(params?: { customerId?: number }) {
  return http.get<unknown, ServiceObjectRelation[]>('/services/objects', { params });
}

export function assignServiceObject(customerId: number, payload: AssignServiceObjectPayload) {
  return http.put<unknown, ServiceObjectRelation>(`/services/objects/${customerId}`, payload);
}

export function listServiceFocuses(params?: { customerId?: number }) {
  return http.get<unknown, ServiceFocusRecord[]>('/services/focuses', { params });
}

export function createServiceFocus(payload: CreateServiceFocusPayload) {
  return http.post<unknown, ServiceFocusRecord>('/services/focuses', payload);
}

