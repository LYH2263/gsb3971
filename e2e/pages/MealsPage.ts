import { expect, type Page } from '@playwright/test';
import { expectToastContains, fillDateInputByTestId, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface WeeklyMenuPayload {
  mon: string;
  tue: string;
  wed: string;
  thu: string;
  fri: string;
  sat: string;
  sun: string;
}

interface CustomerMealPlanPayload {
  mealType: 'NORMAL' | 'DIABETIC' | 'LOW_SALT' | 'OTHER';
  dietTaboo?: string;
  note?: string;
}

export class MealsPage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/meals');
    await expect(this.page.getByTestId('page-meals')).toBeVisible();
  }

  async setWeekStartDate(date: string) {
    await fillDateInputByTestId(this.page, 'menu-week-start-date', date);
  }

  async fillWeeklyMenu(payload: WeeklyMenuPayload) {
    await formControlByTestId(this.page, 'menu-mon').fill(payload.mon);
    await formControlByTestId(this.page, 'menu-tue').fill(payload.tue);
    await formControlByTestId(this.page, 'menu-wed').fill(payload.wed);
    await formControlByTestId(this.page, 'menu-thu').fill(payload.thu);
    await formControlByTestId(this.page, 'menu-fri').fill(payload.fri);
    await formControlByTestId(this.page, 'menu-sat').fill(payload.sat);
    await formControlByTestId(this.page, 'menu-sun').fill(payload.sun);
  }

  async saveWeeklyMenu() {
    await this.page.getByTestId('menu-save-btn').click();
  }

  async expectWeeklyMenuValue(day: keyof WeeklyMenuPayload, value: string) {
    await expect(formControlByTestId(this.page, `menu-${day}`)).toHaveValue(value);
  }

  async expectWeeklyMenuAllEmpty() {
    for (const day of ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun']) {
      await expect(formControlByTestId(this.page, `menu-${day}`)).toHaveValue('');
    }
  }

  async selectCustomer(name: string) {
    await selectOptionByLabel(this.page, 'meal-plan-customer-id', name);
  }

  async setMealType(label: '普通餐' | '糖尿病餐' | '低盐餐' | '其他') {
    await selectOptionByLabel(this.page, 'meal-plan-meal-type', label);
  }

  async fillMealPlan(payload: CustomerMealPlanPayload) {
    const labelMap = {
      NORMAL: '普通餐',
      DIABETIC: '糖尿病餐',
      LOW_SALT: '低盐餐',
      OTHER: '其他'
    };
    await this.setMealType(labelMap[payload.mealType]);
    if (typeof payload.dietTaboo === 'string') {
      await formControlByTestId(this.page, 'meal-plan-diet-taboo').fill(payload.dietTaboo);
    }
    if (typeof payload.note === 'string') {
      await formControlByTestId(this.page, 'meal-plan-note').fill(payload.note);
    }
  }

  async saveCustomerMealPlan() {
    await this.page.getByTestId('meal-plan-save-btn').click();
  }

  async expectMealPlanValues(payload: { mealTypeLabel: '普通餐' | '糖尿病餐' | '低盐餐' | '其他'; dietTaboo: string; note: string }) {
    await expect(this.page.getByTestId('meal-plan-meal-type')).toContainText(payload.mealTypeLabel);
    await expect(formControlByTestId(this.page, 'meal-plan-diet-taboo')).toHaveValue(payload.dietTaboo);
    await expect(formControlByTestId(this.page, 'meal-plan-note')).toHaveValue(payload.note);
  }

  async expectPlanTableContains(text: string) {
    await expect(this.page.getByTestId('meal-plan-table')).toContainText(text);
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
