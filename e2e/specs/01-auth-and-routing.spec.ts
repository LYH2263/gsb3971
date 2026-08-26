import { adminAccount, staffAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { uniquePhone } from '../fixtures/factories';
import { AuthPage } from '../pages/AuthPage';
import { LayoutPage } from '../pages/LayoutPage';

test.describe('认证与路由守卫', () => {
  test('@smoke @full 未登录访问受保护路由会重定向到登录页', async ({ page, clearSession }) => {
    await clearSession(page);
    const protectedRoutes = ['/customers', '/rooms', '/meals', '/care', '/service-focus', '/users'];

    for (const route of protectedRoutes) {
      await page.goto(route);
      await expect(page.getByTestId('page-login')).toBeVisible();
      const current = new URL(page.url());
      expect(current.pathname).toBe('/login');
      expect(current.searchParams.get('redirect')).toBe(route);
    }
  });

  test('@smoke @full 错误密码登录失败且不会写入 token', async ({ page }) => {
    const authPage = new AuthPage(page);
    await authPage.goto();

    await authPage.login(adminAccount.phone, 'WrongPassword123');
    await authPage.expectToastContains('账号或密码错误');
    await expect(page).toHaveURL(/\/login$/);

    const token = await page.evaluate(() => localStorage.getItem('elderly_token'));
    expect(token).toBeNull();
  });

  test('@smoke @full 注册校验与注册成功后登录回跳', async ({ page }) => {
    const authPage = new AuthPage(page);
    await authPage.goto('/login?redirect=%2Frooms');
    await authPage.switchToRegister();

    await authPage.submitRegister();
    await authPage.expectValidationText('请输入手机号');

    const phone = uniquePhone();
    await authPage.fillRegister({
      phone: '123',
      password: 'Pass@123',
      realName: 'E2E注册用户',
      age: 66,
      gender: '男'
    });
    await authPage.submitRegister();
    await authPage.expectValidationText('手机号格式不正确');

    await authPage.fillRegister({
      phone,
      password: 'Pass@123',
      realName: 'E2E注册用户',
      age: 66,
      gender: '男'
    });
    await authPage.submitRegister();
    await authPage.expectToastContains('注册成功，请登录');

    await authPage.submitLogin();
    await expect(page).toHaveURL(/\/rooms$/);

    const token = await page.evaluate(() => localStorage.getItem('elderly_token'));
    expect(token).toBeTruthy();
  });

  test('@smoke @full 退出登录会清理会话', async ({ page, loginAs }) => {
    const layoutPage = new LayoutPage(page);
    await loginAs(page, 'admin');
    await layoutPage.expectReady();

    await layoutPage.logout();
    const [token, user] = await page.evaluate(() => [
      localStorage.getItem('elderly_token'),
      localStorage.getItem('elderly_user')
    ]);

    expect(token).toBeNull();
    expect(user).toBeNull();
  });

  test('@smoke @full 非管理员访问营务账号页会被拦截', async ({ page, loginAs }) => {
    const layoutPage = new LayoutPage(page);
    await loginAs(page, 'staff');
    await layoutPage.expectReady();
    await layoutPage.expectServiceFocusMenuVisible(false);
    await layoutPage.expectUsersMenuVisible(false);

    for (const adminRoute of ['/users', '/service-focus']) {
      await page.goto(adminRoute);
      await expect(page).toHaveURL(/\/customers$/);
      await layoutPage.expectRoleBlockedToast();
    }

    const currentPhone = await page.evaluate(() => {
      const raw = localStorage.getItem('elderly_user');
      if (!raw) {
        return null;
      }
      return JSON.parse(raw).phone;
    });
    expect(currentPhone).toBe(staffAccount.phone);
  });
});
