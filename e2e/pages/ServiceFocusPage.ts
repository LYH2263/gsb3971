import { expect, type Page } from '@playwright/test';
import { expectToastContains, fillDateInputByTestId, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface ServiceFocusPayload {
  customerName: string;
  serviceName: string;
  note?: string;
  statusLabel?: '生效中' | '暂停' | '结束';
}

export class ServiceFocusPage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/service-focus');
    await expect(this.page.getByTestId('page-service-focus')).toBeVisible();
  }

  async openServiceObjectDialog() {
    await this.page.getByTestId('service-object-open').click();
    await expect(this.page.getByTestId('service-object-dialog')).toBeVisible();
  }

  async setServiceObject(customerName: string, managerDisplay: string) {
    await this.openServiceObjectDialog();
    await selectOptionByLabel(this.page, 'service-object-customer', customerName);
    await selectOptionByLabel(this.page, 'service-object-manager', managerDisplay);
    await this.page.getByTestId('service-object-submit').click();
  }

  async fillServiceFocus(payload: ServiceFocusPayload) {
    await selectOptionByLabel(this.page, 'service-focus-customer', payload.customerName);
    await formControlByTestId(this.page, 'service-focus-name').fill(payload.serviceName);
    if (payload.statusLabel) {
      await selectOptionByLabel(this.page, 'service-focus-status', payload.statusLabel);
    }
    if (typeof payload.note === 'string') {
      await formControlByTestId(this.page, 'service-focus-note').fill(payload.note);
    }
  }

  async setPurchaseDate(date: string) {
    await fillDateInputByTestId(this.page, 'service-focus-purchase-date', date);
  }

  async setExpireDate(date: string) {
    await fillDateInputByTestId(this.page, 'service-focus-expire-date', date);
  }

  async submitServiceFocus() {
    await this.page.getByTestId('service-focus-submit').click();
  }

  async expectObjectTableContains(text: string) {
    await expect(this.page.getByTestId('service-object-table')).toContainText(text);
  }

  async expectFocusTableContains(text: string) {
    await expect(this.page.getByTestId('service-focus-table')).toContainText(text);
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
