import { http } from '@/api/http';
import type { Customer } from '@/types';

export interface CreateCustomerPayload {
  name: string;
  phone?: string;
  age: number;
  gender: number;
  note?: string;
}

export interface LifecyclePayload {
  action: 'checkin' | 'discharge' | 'outing';
  actionDate: string;
  bedId?: number;
  reason?: string;
}

export function getCustomers(params?: { status?: string; keyword?: string }) {
  return http.get<unknown, Customer[]>('/customers', { params });
}

export function createCustomer(payload: CreateCustomerPayload) {
  return http.post<unknown, Customer>('/customers', payload);
}

export function updateLifecycle(customerId: number, payload: LifecyclePayload) {
  return http.patch<unknown, Customer>(`/customers/${customerId}/lifecycle`, payload);
}
