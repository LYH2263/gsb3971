import { adminAccount, staffAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { ServiceFocusPage } from '../pages/ServiceFocusPage';

test.describe('带教配对与研学增值包', () => {
  test('@smoke @full 管理员可设置带教配对并保存研学增值包', async ({ page, loginAs, apiClient }) => {
    const serviceFocusPage = new ServiceFocusPage(page);
    await loginAs(page, 'admin');
    await serviceFocusPage.goto();

    await serviceFocusPage.setServiceObject('张三', `导师甲（${staffAccount.phone}）`);
    await serviceFocusPage.expectToastContains('带教配对设置成功');
    await serviceFocusPage.expectObjectTableContains('张三');
    await serviceFocusPage.expectObjectTableContains('导师甲');

    const serviceName = `观测计划-${Date.now()}`;
    await serviceFocusPage.fillServiceFocus({
      customerName: '张三',
      serviceName,
      statusLabel: '生效中',
      note: 'E2E 自动化研学增值包'
    });
    await serviceFocusPage.submitServiceFocus();
    await serviceFocusPage.expectToastContains('研学增值包保存成功');
    await serviceFocusPage.expectFocusTableContains(serviceName);
    await serviceFocusPage.expectFocusTableContains('生效中');

    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const apiResult = await apiClient.rawGet('/api/services/focuses', token, { customerId: 1 });
    expect(apiResult.status).toBe(200);
    expect(apiResult.body.code).toBe(0);

    const records = apiResult.body.data as Array<{ serviceName: string; serviceStatus: string }>;
    const created = records.find((record) => record.serviceName === serviceName);
    expect(created).toBeTruthy();
    expect(created?.serviceStatus).toBe('ACTIVE');
  });

  test('@smoke @full 研学增值包页面校验分支可阻断非法提交', async ({ page, loginAs }) => {
    const serviceFocusPage = new ServiceFocusPage(page);
    await loginAs(page, 'admin');
    await serviceFocusPage.goto();

    await serviceFocusPage.submitServiceFocus();
    await serviceFocusPage.expectToastContains('请选择学员');

    await serviceFocusPage.fillServiceFocus({
      customerName: '张三',
      serviceName: ''
    });
    await serviceFocusPage.submitServiceFocus();
    await serviceFocusPage.expectToastContains('请输入项目名称');

    await serviceFocusPage.fillServiceFocus({
      customerName: '张三',
      serviceName: `服务分支-${Date.now()}`,
      statusLabel: '暂停'
    });
    await serviceFocusPage.submitServiceFocus();
    await serviceFocusPage.expectToastContains('研学增值包保存成功');
    await serviceFocusPage.expectFocusTableContains('暂停');

    await serviceFocusPage.openServiceObjectDialog();
    await page.getByTestId('service-object-submit').click();
    await serviceFocusPage.expectToastContains('请选择学员');
    await page.getByTestId('service-object-cancel').click();
  });
});
