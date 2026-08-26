import { adminAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { todayDate, uniquePhone, uniqueRoomNo } from '../fixtures/factories';
import { RoomsPage } from '../pages/RoomsPage';

test.describe('营房铺位管理', () => {
  test('@smoke @full 新增房间成功与重复房间冲突', async ({ page, loginAs }) => {
    const roomsPage = new RoomsPage(page);
    await loginAs(page, 'admin');
    await roomsPage.goto();

    const roomNo = uniqueRoomNo('E2E-R');
    await roomsPage.createRoom({ floor: 5, roomNo, enabled: true });
    await roomsPage.expectToastContains('房间保存成功');
    await roomsPage.expectRoomRowVisible(roomNo);
    await roomsPage.expectRoomRowContains(roomNo, '暂无铺位');

    await roomsPage.createRoom({ floor: 5, roomNo, enabled: true });
    await roomsPage.expectToastContains('房间号已存在');
  });

  test('@smoke @full 新增铺位成功/重复冲突/占用不可停用/空闲可停用', async ({ page, loginAs, apiClient }) => {
    const roomsPage = new RoomsPage(page);
    await loginAs(page, 'admin');
    await roomsPage.goto();

    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const roomNo = uniqueRoomNo('E2E-B');
    await roomsPage.createRoom({ floor: 6, roomNo, enabled: true });
    await roomsPage.expectToastContains('房间保存成功');

    const rooms = await apiClient.getRooms(token, true);
    const room = rooms.find((item) => item.roomNo === roomNo);
    expect(room).toBeTruthy();

    const occupiedBedNo = `A${Date.now().toString().slice(-4)}`;
    await roomsPage.createBed(room!.id, { bedNo: occupiedBedNo, status: 'AVAILABLE' });
    await roomsPage.expectToastContains('铺位保存成功');
    await roomsPage.expectRoomRowContains(roomNo, occupiedBedNo);

    await roomsPage.createBed(room!.id, { bedNo: occupiedBedNo, status: 'AVAILABLE' });
    await roomsPage.expectToastContains('营房内铺位号已存在');
    await roomsPage.closeBedDialogIfVisible();

    const refreshedRooms = await apiClient.getRooms(token, true);
    const refreshedRoom = refreshedRooms.find((item) => item.id === room!.id);
    expect(refreshedRoom).toBeTruthy();
    const occupiedBed = refreshedRoom?.beds.find((bed) => bed.bedNo === occupiedBedNo);
    expect(occupiedBed).toBeTruthy();

    const customer = await apiClient.createCustomer(token, {
      name: `占床客户-${Date.now()}`,
      phone: uniquePhone(),
      age: 76,
      gender: 1,
      note: '占用铺位禁停用'
    });
    await apiClient.updateCustomerLifecycle(token, customer.id, {
      action: 'checkin',
      actionDate: todayDate(),
      bedId: occupiedBed?.id
    });

    await roomsPage.refresh();
    await roomsPage.changeBedStatus(room!.id, occupiedBed!.id, 'disabled');
    await roomsPage.expectToastContains('占用中的铺位不可停用');

    const availableBedNo = `B${Date.now().toString().slice(-4)}`;
    await roomsPage.createBed(room!.id, { bedNo: availableBedNo, status: 'AVAILABLE' });
    await roomsPage.expectToastContains('铺位保存成功');

    const latestRooms = await apiClient.getRooms(token, true);
    const latestRoom = latestRooms.find((item) => item.id === room!.id);
    const availableBed = latestRoom?.beds.find((bed) => bed.bedNo === availableBedNo);
    expect(availableBed).toBeTruthy();

    await roomsPage.refresh();
    await roomsPage.changeBedStatus(room!.id, availableBed!.id, 'disabled');
    await roomsPage.expectToastContains('铺位状态更新成功');
    await roomsPage.expectRoomRowContains(roomNo, '停用');
  });
});
