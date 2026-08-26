import { http } from '@/api/http';
import type { CustomerMealPlan, WeeklyMenu } from '@/types';

export interface SaveWeeklyMenuPayload {
  mon: string;
  tue: string;
  wed: string;
  thu: string;
  fri: string;
  sat: string;
  sun: string;
}

export interface SaveCustomerMealPlanPayload {
  mealType: string;
  dietTaboo?: string;
  note?: string;
}

export function saveWeeklyMenu(weekStartDate: string, payload: SaveWeeklyMenuPayload) {
  return http.put<unknown, WeeklyMenu>(`/meals/weekly-menus/${weekStartDate}`, payload);
}

export function getWeeklyMenu(weekStartDate: string) {
  return http.get<unknown, WeeklyMenu>(`/meals/weekly-menus/${weekStartDate}`);
}

export function saveCustomerMealPlan(customerId: number, weekStartDate: string, payload: SaveCustomerMealPlanPayload) {
  return http.put<unknown, CustomerMealPlan>(`/meals/customer-plans/${customerId}/${weekStartDate}`, payload);
}

export function getCustomerMealPlan(customerId: number, weekStartDate: string) {
  return http.get<unknown, CustomerMealPlan>(`/meals/customer-plans/${customerId}/${weekStartDate}`);
}

export function listCustomerMealPlans(params?: { weekStartDate?: string; customerId?: number }) {
  return http.get<unknown, CustomerMealPlan[]>('/meals/customer-plans', { params });
}
