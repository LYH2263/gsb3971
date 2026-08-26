import { adminAccount, staffAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { todayDate, uniquePhone } from '../fixtures/factories';

test.describe('API 边界补齐断言', () => {
  test('@full 未登录请求受保护接口返回 401', async ({ apiClient }) => {
    const result = await apiClient.rawGet('/api/customers', null);
    expect(result.status).toBe(401);
    expect(result.body.code).toBe(40102);
    expect(result.body.message).toContain('未登录');
  });

  test('@full 生命周期接口非法动作与缺失 bedId 均被拒绝', async ({ apiClient }) => {
    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const customer = await apiClient.createCustomer(token, {
      name: `API边界客户-${Date.now()}`,
      phone: uniquePhone(),
      age: 70,
      gender: 1,
      note: 'API 边界测试'
    });

    const invalidAction = await apiClient.rawPatch(`/api/customers/${customer.id}/lifecycle`, token, {
      action: 'invalid_action',
      actionDate: todayDate()
    });
    expect(invalidAction.status).toBe(400);
    expect(invalidAction.body.code).toBe(40001);
    expect(invalidAction.body.message).toContain('无效的生命周期动作');

    const missingBed = await apiClient.rawPatch(`/api/customers/${customer.id}/lifecycle`, token, {
      action: 'checkin',
      actionDate: todayDate()
    });
    expect(missingBed.status).toBe(400);
    expect(missingBed.body.code).toBe(40001);
    expect(missingBed.body.message).toContain('入营必须选择铺位');
  });

  test('@full 不存在资源与参数校验错误返回正确状态码', async ({ apiClient }) => {
    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);

    const notFoundCustomer = await apiClient.rawPatch('/api/customers/999999/lifecycle', token, {
      action: 'discharge',
      actionDate: todayDate()
    });
    expect(notFoundCustomer.status).toBe(404);
    expect(notFoundCustomer.body.code).toBe(40401);
    expect(notFoundCustomer.body.message).toContain('学员不存在');

    const invalidRoomStatus = await apiClient.rawPost('/api/rooms', token, {
      floor: 1,
      roomNo: `BAD-${Date.now().toString().slice(-4)}`,
      status: 2
    });
    expect(invalidRoomStatus.status).toBe(400);
    expect(invalidRoomStatus.body.code).toBe(40001);
    expect(invalidRoomStatus.body.message).toContain('状态取值非法');

    const notFoundMealPlanCustomer = await apiClient.rawPut('/api/meals/customer-plans/999999/2026-01-05', token, {
      mealType: 'NORMAL',
      dietTaboo: '',
      note: ''
    });
    expect(notFoundMealPlanCustomer.status).toBe(404);
    expect(notFoundMealPlanCustomer.body.code).toBe(40401);
    expect(notFoundMealPlanCustomer.body.message).toContain('学员不存在');
  });

  test('@full 非管理员访问管理员接口返回 403', async ({ apiClient }) => {
    const staffLogin = await apiClient.login(staffAccount.phone, staffAccount.password);
    const result = await apiClient.rawGet('/api/users', staffLogin.token);

    expect(result.status).toBe(403);
    expect(result.body.code).toBe(40301);
    expect(result.body.message).toContain('仅管理员可执行该操作');
  });

  test('@full 带教配对与研学增值包边界拒绝非法请求', async ({ apiClient }) => {
    const adminLogin = await apiClient.login(adminAccount.phone, adminAccount.password);
    const staffLogin = await apiClient.login(staffAccount.phone, staffAccount.password);

    const noPermission = await apiClient.rawPut('/api/services/objects/1', staffLogin.token, {
      managerUserId: 2
    });
    expect(noPermission.status).toBe(403);
    expect(noPermission.body.code).toBe(40301);
    expect(noPermission.body.message).toContain('仅管理员可执行该操作');

    const invalidExpireDate = await apiClient.rawPost('/api/services/focuses', adminLogin.token, {
      customerId: 1,
      serviceName: `API服务边界-${Date.now()}`,
      purchaseDate: '2026-03-10',
      expireDate: '2026-03-01',
      serviceStatus: 'ACTIVE'
    });
    expect(invalidExpireDate.status).toBe(400);
    expect(invalidExpireDate.body.code).toBe(40001);
    expect(invalidExpireDate.body.message).toContain('到期日期不能早于报名日期');

    const invalidStatus = await apiClient.rawPost('/api/services/focuses', adminLogin.token, {
      customerId: 1,
      serviceName: `API状态边界-${Date.now()}`,
      purchaseDate: '2026-03-10',
      serviceStatus: 'UNKNOWN'
    });
    expect(invalidStatus.status).toBe(400);
    expect(invalidStatus.body.code).toBe(40001);
    expect(invalidStatus.body.message).toContain('服务状态取值非法');
  });
});
