import { expect, type Page } from '@playwright/test';
import { expectToastContains, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface RegisterPayload {
  phone: string;
  password: string;
  realName: string;
  age: number;
  gender: '男' | '女' | '未知';
}

export class AuthPage {
  constructor(private readonly page: Page) {
  }

  async goto(path = '/login') {
    await this.page.goto(path);
    await expect(this.page.getByTestId('page-login')).toBeVisible();
  }

  async switchToRegister() {
    await this.page.getByRole('tab', { name: '注册' }).click();
    await expect(this.page.getByTestId('form-register')).toBeVisible();
  }

  async switchToLogin() {
    await this.page.getByRole('tab', { name: '登录' }).click();
    await expect(this.page.getByTestId('form-login')).toBeVisible();
  }

  async fillLogin(phone: string, password: string) {
    await formControlByTestId(this.page, 'login-phone').fill(phone);
    await formControlByTestId(this.page, 'login-password').fill(password);
  }

  async submitLogin() {
    await this.page.getByTestId('login-submit').click();
  }

  async login(phone: string, password: string) {
    await this.fillLogin(phone, password);
    await this.submitLogin();
  }

  async fillRegister(payload: RegisterPayload) {
    await formControlByTestId(this.page, 'register-phone').fill(payload.phone);
    await formControlByTestId(this.page, 'register-password').fill(payload.password);
    await formControlByTestId(this.page, 'register-real-name').fill(payload.realName);
    await formControlByTestId(this.page, 'register-age').fill(String(payload.age));
    await selectOptionByLabel(this.page, 'register-gender', payload.gender);
  }

  async submitRegister() {
    await this.page.getByTestId('register-submit').click();
  }

  async register(payload: RegisterPayload) {
    await this.switchToRegister();
    await this.fillRegister(payload);
    await this.submitRegister();
  }

  async expectValidationText(text: string) {
    await expect(this.page.locator('.el-form-item__error').filter({ hasText: text }).first()).toBeVisible();
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
