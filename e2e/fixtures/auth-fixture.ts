import { test as base, expect, type Page } from '@playwright/test';
import { E2E_BACKEND_BASE_URL } from '../helpers/compose';
import { ApiClient } from './api-client';
import { adminAccount, staffAccount } from './accounts';
import { formControlByTestId } from '../pages/ui-helpers';

type UserRole = 'admin' | 'staff';

interface Fixtures {
  apiClient: ApiClient;
  loginAs: (page: Page, role: UserRole, options?: { redirectPath?: string }) => Promise<void>;
  clearSession: (page: Page) => Promise<void>;
}

export const test = base.extend<Fixtures>({
  apiClient: async ({ request }, use) => {
    await use(new ApiClient(request, E2E_BACKEND_BASE_URL));
  },

  loginAs: async ({}, use) => {
    await use(async (page, role, options) => {
      const account = role === 'admin' ? adminAccount : staffAccount;
      const target = options?.redirectPath ? `/login?redirect=${encodeURIComponent(options.redirectPath)}` : '/login';
      await page.goto(target);
      await formControlByTestId(page, 'login-phone').fill(account.phone);
      await formControlByTestId(page, 'login-password').fill(account.password);
      await page.getByTestId('login-submit').click();
      if (options?.redirectPath) {
        await expect(page).toHaveURL(new RegExp(options.redirectPath.replace('/', '\\/')));
      } else {
        await expect(page).toHaveURL(/\/customers$/);
      }
    });
  },

  clearSession: async ({}, use) => {
    await use(async (page) => {
      await page.goto('/login');
      await page.evaluate(() => {
        localStorage.clear();
        sessionStorage.clear();
      });
    });
  }
});

export { expect };
