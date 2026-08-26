import { adminAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { uniquePhone } from '../fixtures/factories';
import { CarePage } from '../pages/CarePage';

function formatDateTime(offsetMinutes = 0): string {
  const date = new Date(Date.now() + offsetMinutes * 60 * 1000);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

test.describe('带教辅导', () => {
  test('@smoke @full 带教档位新增与启停切换', async ({ page, loginAs, apiClient }) => {
    const carePage = new CarePage(page);
    await loginAs(page, 'admin');
    await carePage.goto();

    const levelName = `E2E带教档位-${Date.now()}`;
    await carePage.createCareLevel({
      name: levelName,
      description: '用于 E2E 自动化验证'
    });
    await carePage.expectToastContains('带教档位新增成功');
    await carePage.expectLevelTableContains(levelName);

    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const levels = await apiClient.getCareLevels(token);
    const createdLevel = levels.find((item) => item.name === levelName);
    expect(createdLevel).toBeTruthy();

    await carePage.toggleCareLevel(createdLevel!.id);
    await carePage.expectToastContains('带教档位状态更新成功');

    const refreshedLevels = await apiClient.getCareLevels(token);
    const toggled = refreshedLevels.find((item) => item.id === createdLevel!.id);
    expect(toggled?.status).toBe(0);
  });

  test('@smoke @full 辅导记录未选客户告警、新增成功、客户过滤与倒序展示', async ({ page, loginAs, apiClient }) => {
    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const customerAName = `辅导学员A-${Date.now()}`;
    const customerBName = `辅导学员B-${Date.now()}`;

    const customerA = await apiClient.createCustomer(token, {
      name: customerAName,
      phone: uniquePhone(),
      age: 72,
      gender: 1,
      note: '辅导记录过滤测试A'
    });
    const customerB = await apiClient.createCustomer(token, {
      name: customerBName,
      phone: uniquePhone('137'),
      age: 75,
      gender: 2,
      note: '辅导记录过滤测试B'
    });

    await apiClient.createCareRecord(token, {
      customerId: customerB.id,
      careDate: formatDateTime(-3),
      content: '其他客户记录'
    });

    const carePage = new CarePage(page);
    await loginAs(page, 'admin');
    await carePage.goto();

    await carePage.submitCareRecord();
    await carePage.expectToastContains('请选择学员');

    await carePage.selectCustomer(customerAName);
    await carePage.fillCareRecord({
      careDate: formatDateTime(-2),
      content: '辅导记录-较早'
    });
    await carePage.submitCareRecord();
    await carePage.expectToastContains('辅导记录保存成功');

    await carePage.fillCareRecord({
      careDate: formatDateTime(-1),
      content: '辅导记录-较新'
    });
    await carePage.submitCareRecord();
    await carePage.expectToastContains('辅导记录保存成功');

    await carePage.expectFirstRecordContains('辅导记录-较新');
    await carePage.expectRecordTableContains(customerAName);
    await carePage.expectRecordTableContains('系统管理员');
    await expect(page.getByTestId('care-record-table')).not.toContainText('其他客户记录');

    const records = await apiClient.getCareRecords(token, { customerId: customerA.id });
    expect(records.length).toBeGreaterThanOrEqual(2);
    expect(records[0].content).toContain('较新');
    expect(records[1].content).toContain('较早');
  });
});
