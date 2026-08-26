import { expect, type Locator, type Page } from '@playwright/test';
import { expectToastContains, fillDateInputByTestId, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface CreateCustomerPayload {
  name: string;
  phone?: string;
  age?: number;
  gender?: '男' | '女' | '未知';
  note?: string;
}

export class CustomersPage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/customers');
    await expect(this.page.getByTestId('page-customers')).toBeVisible();
  }

  async refresh() {
    await this.page.getByTestId('customers-refresh').click();
  }

  async openCreateDialog() {
    await this.page.getByTestId('customer-create-open').click();
    await expect(this.page.getByTestId('customer-create-dialog')).toBeVisible();
  }

  async fillCreateForm(payload: CreateCustomerPayload) {
    await formControlByTestId(this.page, 'customer-create-name').fill(payload.name);
    if (typeof payload.phone === 'string') {
      await formControlByTestId(this.page, 'customer-create-phone').fill(payload.phone);
    }
    if (typeof payload.age === 'number') {
      await formControlByTestId(this.page, 'customer-create-age').fill(String(payload.age));
    }
    if (payload.gender) {
      await selectOptionByLabel(this.page, 'customer-create-gender', payload.gender);
    }
    if (typeof payload.note === 'string') {
      await formControlByTestId(this.page, 'customer-create-note').fill(payload.note);
    }
  }

  async submitCreate() {
    await this.page.getByTestId('customer-create-submit').click();
  }

  async createCustomer(payload: CreateCustomerPayload) {
    await this.openCreateDialog();
    await this.fillCreateForm(payload);
    await this.submitCreate();
  }

  async cancelCreate() {
    await this.page.getByTestId('customer-create-cancel').click();
    await expect(this.page.getByTestId('customer-create-dialog')).toBeHidden();
  }

  rowByName(name: string): Locator {
    return this.page.locator('.el-table__body-wrapper tbody tr').filter({ hasText: name }).first();
  }

  async expectRowVisible(name: string) {
    await expect(this.rowByName(name)).toBeVisible();
  }

  async expectRowContains(name: string, text: string) {
    await expect(this.rowByName(name)).toContainText(text);
  }

  async openCheckin(customerId: number) {
    await this.page.getByTestId(`customer-checkin-${customerId}`).click();
    await expect(this.page.getByTestId('customer-lifecycle-dialog')).toBeVisible();
  }

  async openDischarge(customerId: number) {
    await this.page.getByTestId(`customer-discharge-${customerId}`).click();
    await expect(this.page.getByTestId('customer-lifecycle-dialog')).toBeVisible();
  }

  async clickOuting(customerId: number) {
    await this.page.getByTestId(`customer-outing-${customerId}`).click();
  }

  async expectOutingDisabled(customerId: number) {
    await expect(this.page.getByTestId(`customer-outing-${customerId}`)).toBeDisabled();
  }

  async expectDischargeDisabled(customerId: number) {
    await expect(this.page.getByTestId(`customer-discharge-${customerId}`)).toBeDisabled();
  }

  async setLifecycleDate(date: string) {
    await fillDateInputByTestId(this.page, 'customer-lifecycle-date', date);
  }

  async chooseLifecycleBed(label: string) {
    await selectOptionByLabel(this.page, 'customer-lifecycle-bed', label);
  }

  async fillLifecycleReason(reason: string) {
    await formControlByTestId(this.page, 'customer-lifecycle-reason').fill(reason);
  }

  async submitLifecycle() {
    await this.page.getByTestId('customer-lifecycle-submit').click();
  }

  async cancelLifecycle() {
    await this.page.getByTestId('customer-lifecycle-cancel').click();
  }

  async confirmMessageBox() {
    const box = this.page.locator('.el-message-box').last();
    await expect(box).toBeVisible();
    await box.getByRole('button', { name: /^(确定|OK)$/ }).click();
  }

  async cancelMessageBox() {
    const box = this.page.locator('.el-message-box').last();
    await expect(box).toBeVisible();
    await box.getByRole('button', { name: /^(取消|Cancel)$/ }).click();
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
