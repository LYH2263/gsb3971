import { adminAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { todayDate, uniquePhone } from '../fixtures/factories';
import { CustomersPage } from '../pages/CustomersPage';

test.describe('客户生命周期链路', () => {
  test('@smoke @full 新增学员表单校验失败分支', async ({ page, loginAs }) => {
    const customersPage = new CustomersPage(page);
    await loginAs(page, 'admin');
    await customersPage.goto();

    await customersPage.openCreateDialog();
    await customersPage.submitCreate();
    await expect(page.locator('.el-form-item__error').filter({ hasText: '请输入学员姓名' }).first()).toBeVisible();

    await customersPage.fillCreateForm({
      name: '校验用户',
      phone: '123',
      age: 80,
      gender: '男'
    });
    await customersPage.submitCreate();
    await expect(page.locator('.el-form-item__error').filter({ hasText: '手机号格式不正确' }).first()).toBeVisible();

    await customersPage.cancelCreate();
  });

  test('@smoke @full 学员入营/离营外出/结营全流程与边界', async ({ page, loginAs, apiClient }) => {
    const customersPage = new CustomersPage(page);
    await loginAs(page, 'admin');
    await customersPage.goto();

    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const rooms = await apiClient.getRooms(token, true);
    const targetRoom = rooms.find((room) => room.beds.some((bed) => bed.status === 'AVAILABLE'));
    expect(targetRoom).toBeTruthy();
    const targetBed = targetRoom?.beds.find((bed) => bed.status === 'AVAILABLE');
    expect(targetBed).toBeTruthy();

    const customerName = `E2E客户-${Date.now()}`;
    const customerPhone = uniquePhone();
    await customersPage.createCustomer({
      name: customerName,
      phone: customerPhone,
      age: 73,
      gender: '男',
      note: '生命周期链路测试'
    });
    await customersPage.expectToastContains('学员创建成功');
    await customersPage.expectRowVisible(customerName);
    await customersPage.expectRowContains(customerName, '待入营');

    const listAfterCreate = await apiClient.getCustomers(token, { keyword: customerName });
    expect(listAfterCreate.length).toBeGreaterThan(0);
    const customer = listAfterCreate[0];

    await customersPage.openCheckin(customer.id);
    await customersPage.submitLifecycle();
    await customersPage.expectToastContains('入营需选择铺位');

    const bedLabel = `${targetRoom?.roomNo}/${targetBed?.bedNo}`;
    await customersPage.chooseLifecycleBed(bedLabel);
    await customersPage.fillLifecycleReason('首次入营');
    await customersPage.submitLifecycle();
    await customersPage.expectToastContains('状态更新成功');
    await customersPage.expectRowContains(customerName, '在营');
    await customersPage.expectRowContains(customerName, targetBed?.bedNo || '');

    const conflictCustomer = await apiClient.createCustomer(token, {
      name: `冲突客户-${Date.now()}`,
      phone: uniquePhone(),
      age: 69,
      gender: 2,
      note: '铺位冲突校验'
    });
    const conflictResult = await apiClient.rawPatch(`/api/customers/${conflictCustomer.id}/lifecycle`, token, {
      action: 'checkin',
      actionDate: todayDate(),
      bedId: targetBed?.id
    });
    expect(conflictResult.status).toBe(409);
    expect(conflictResult.body.code).toBe(40902);
    expect(conflictResult.body.message).toContain('铺位已占用');

    await customersPage.clickOuting(customer.id);
    await customersPage.cancelMessageBox();
    await customersPage.expectRowContains(customerName, '在营');

    await customersPage.clickOuting(customer.id);
    await customersPage.confirmMessageBox();
    await customersPage.expectToastContains('离营外出登记成功');
    await customersPage.expectRowContains(customerName, '外出');

    await customersPage.openDischarge(customer.id);
    await customersPage.fillLifecycleReason('办理结营');
    await customersPage.submitLifecycle();
    await customersPage.expectToastContains('状态更新成功');
    await customersPage.expectRowContains(customerName, '已结营');

    await customersPage.expectOutingDisabled(customer.id);
    const dischargedOuting = await apiClient.rawPatch(`/api/customers/${customer.id}/lifecycle`, token, {
      action: 'outing',
      actionDate: todayDate()
    });
    expect(dischargedOuting.status).toBe(409);
    expect(dischargedOuting.body.code).toBe(40903);
  });
});
