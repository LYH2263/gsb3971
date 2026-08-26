import { expect, type Page } from '@playwright/test';
import { expectToastContains } from './ui-helpers';

export class LayoutPage {
  constructor(private readonly page: Page) {
  }

  async expectReady() {
    await expect(this.page.getByTestId('layout-root')).toBeVisible();
    await expect(this.page.getByTestId('layout-header')).toBeVisible();
  }

  async gotoCustomers() {
    await this.page.getByTestId('nav-customers').click();
    await expect(this.page).toHaveURL(/\/customers$/);
  }

  async gotoRooms() {
    await this.page.getByTestId('nav-rooms').click();
    await expect(this.page).toHaveURL(/\/rooms$/);
  }

  async gotoMeals() {
    await this.page.getByTestId('nav-meals').click();
    await expect(this.page).toHaveURL(/\/meals$/);
  }

  async gotoCare() {
    await this.page.getByTestId('nav-care').click();
    await expect(this.page).toHaveURL(/\/care$/);
  }

  async gotoServiceFocus() {
    await this.page.getByTestId('nav-service-focus').click();
    await expect(this.page).toHaveURL(/\/service-focus$/);
  }

  async gotoUsers() {
    await this.page.getByTestId('nav-users').click();
    await expect(this.page).toHaveURL(/\/users$/);
  }

  async expectUsersMenuVisible(visible: boolean) {
    if (visible) {
      await expect(this.page.getByTestId('nav-users')).toBeVisible();
      return;
    }
    await expect(this.page.getByTestId('nav-users')).toHaveCount(0);
  }

  async expectServiceFocusMenuVisible(visible: boolean) {
    if (visible) {
      await expect(this.page.getByTestId('nav-service-focus')).toBeVisible();
      return;
    }
    await expect(this.page.getByTestId('nav-service-focus')).toHaveCount(0);
  }

  async logout() {
    await this.page.getByTestId('nav-logout').click();
    await expect(this.page).toHaveURL(/\/login/);
  }

  async expectRoleBlockedToast() {
    await expectToastContains(this.page, '仅管理员可访问该页面');
  }
}
