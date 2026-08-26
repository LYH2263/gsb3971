import { adminAccount, staffAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { AuthPage } from '../pages/AuthPage';
import { UsersPage } from '../pages/UsersPage';

test.describe('营务账号与角色分支', () => {
  test('@smoke @full 管理员可维护用户状态且停用后登录受限', async ({ page, loginAs, clearSession, apiClient }) => {
    const usersPage = new UsersPage(page);
    const authPage = new AuthPage(page);

    const adminLogin = await apiClient.login(adminAccount.phone, adminAccount.password);
    const adminToken = adminLogin.token;

    await apiClient.updateUserStatus(adminToken, 2, 1);

    try {
      await loginAs(page, 'admin');
      await usersPage.goto();
      await usersPage.expectAdminTableVisible();
      await usersPage.expectTableContains(staffAccount.phone);

      await usersPage.toggleUserStatus(2);
      await usersPage.expectToastContains('用户状态更新成功');

      const usersAfterDisable = await apiClient.getUsers(adminToken);
      const staffAfterDisable = usersAfterDisable.find((item) => item.phone === staffAccount.phone);
      expect(staffAfterDisable?.status).toBe(0);

      await clearSession(page);
      await authPage.goto();
      await authPage.login(staffAccount.phone, staffAccount.password);
      await authPage.expectToastContains('账号已停用');
      await expect(page).toHaveURL(/\/login$/);
    } finally {
      await apiClient.updateUserStatus(adminToken, 2, 1);
    }

    await authPage.goto();
    await authPage.login(staffAccount.phone, staffAccount.password);
    await expect(page).toHaveURL(/\/customers$/);
  });
});
