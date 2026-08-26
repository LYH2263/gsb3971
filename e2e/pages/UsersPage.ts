import { expect, type Page } from '@playwright/test';
import { expectToastContains } from './ui-helpers';

export class UsersPage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/users');
  }

  async expectAdminTableVisible() {
    await expect(this.page.getByTestId('users-table')).toBeVisible();
  }

  async expectNoAdminAlertVisible() {
    await expect(this.page.getByTestId('users-no-admin-alert')).toBeVisible();
  }

  async expectTableContains(text: string) {
    await expect(this.page.getByTestId('users-table')).toContainText(text);
  }

  async toggleUserStatus(userId: number) {
    await this.page.getByTestId(`users-status-${userId}`).click();
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
