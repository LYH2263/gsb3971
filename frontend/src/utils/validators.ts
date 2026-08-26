import type { Customer } from '@/types';

export function isValidPhone(phone: string): boolean {
  return /^1\d{10}$/.test(phone);
}

export function canOuting(customer: Customer): boolean {
  return customer.status === 'RESIDENT';
}
