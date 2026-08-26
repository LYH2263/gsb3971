import { expect, type Locator, type Page } from '@playwright/test';
import { expectToastContains, fillDateInputByTestId, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface CreateCareLevelPayload {
  name: string;
  description?: string;
}

interface CreateCareRecordPayload {
  careDate: string;
  content: string;
}

export class CarePage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/care');
    await expect(this.page.getByTestId('page-care')).toBeVisible();
  }

  async createCareLevel(payload: CreateCareLevelPayload) {
    await formControlByTestId(this.page, 'care-level-name').fill(payload.name);
    if (typeof payload.description === 'string') {
      await formControlByTestId(this.page, 'care-level-description').fill(payload.description);
    }
    await this.page.getByTestId('care-level-create-btn').click();
  }

  async toggleCareLevel(levelId: number) {
    await this.page.getByTestId(`care-level-status-${levelId}`).click();
  }

  async selectCustomer(name: string) {
    await selectOptionByLabel(this.page, 'care-record-customer-id', name);
  }

  async fillCareRecord(payload: CreateCareRecordPayload) {
    await fillDateInputByTestId(this.page, 'care-record-date', payload.careDate);
    await formControlByTestId(this.page, 'care-record-content').fill(payload.content);
  }

  async submitCareRecord() {
    await this.page.getByTestId('care-record-create-btn').click();
  }

  recordRows(): Locator {
    return this.page.locator('[data-testid="care-record-table"] .el-table__body-wrapper tbody tr');
  }

  async expectFirstRecordContains(text: string) {
    await expect(this.recordRows().first()).toContainText(text);
  }

  async expectRecordTableContains(text: string) {
    await expect(this.page.getByTestId('care-record-table')).toContainText(text);
  }

  async expectLevelTableContains(text: string) {
    await expect(this.page.getByTestId('care-level-table')).toContainText(text);
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
