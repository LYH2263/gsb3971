import { expect, type Locator, type Page } from '@playwright/test';
import { expectToastContains, formControlByTestId, selectOptionByLabel } from './ui-helpers';

interface SaveRoomPayload {
  floor: number;
  roomNo: string;
  enabled?: boolean;
}

interface SaveBedPayload {
  bedNo: string;
  status?: 'AVAILABLE' | 'DISABLED';
}

export class RoomsPage {
  constructor(private readonly page: Page) {
  }

  async goto() {
    await this.page.goto('/rooms');
    await expect(this.page.getByTestId('page-rooms')).toBeVisible();
  }

  async refresh() {
    await this.page.getByTestId('rooms-refresh').click();
  }

  async openCreateRoomDialog() {
    await this.page.getByTestId('room-create-open').click();
    await expect(this.page.getByTestId('room-dialog')).toBeVisible();
  }

  async createRoom(payload: SaveRoomPayload) {
    await this.openCreateRoomDialog();
    await formControlByTestId(this.page, 'room-floor').fill(String(payload.floor));
    await formControlByTestId(this.page, 'room-no').fill(payload.roomNo);
    if (typeof payload.enabled === 'boolean') {
      const switchControl = this.page.getByTestId('room-status');
      const checked = await switchControl.locator('input[type="checkbox"]').first().isChecked();
      if (checked !== payload.enabled) {
        await switchControl.click();
      }
    }
    await this.page.getByTestId('room-submit').click();
  }

  async openAddBedDialog(roomId: number) {
    await this.page.getByTestId(`room-add-bed-${roomId}`).click();
    await expect(this.page.getByTestId('bed-dialog')).toBeVisible();
  }

  async createBed(roomId: number, payload: SaveBedPayload) {
    await this.openAddBedDialog(roomId);
    await formControlByTestId(this.page, 'bed-no').fill(payload.bedNo);
    if (payload.status) {
      const statusLabel = payload.status === 'DISABLED' ? '停用' : '空闲';
      await selectOptionByLabel(this.page, 'bed-status', statusLabel);
    }
    await this.page.getByTestId('bed-submit').click();
  }

  async closeBedDialogIfVisible() {
    const dialog = this.page.getByTestId('bed-dialog');
    if (await dialog.isVisible()) {
      await this.page.getByTestId('bed-cancel').click();
      await expect(dialog).toBeHidden();
    }
  }

  rowByRoomNo(roomNo: string): Locator {
    return this.page.locator('.el-table__body-wrapper tbody tr').filter({ hasText: roomNo }).first();
  }

  async expectRoomRowVisible(roomNo: string) {
    await expect(this.rowByRoomNo(roomNo)).toBeVisible();
  }

  async expectRoomRowContains(roomNo: string, text: string) {
    await expect(this.rowByRoomNo(roomNo)).toContainText(text);
  }

  async changeBedStatus(roomId: number, bedId: number, status: 'available' | 'disabled') {
    await this.page.getByTestId(`room-bed-status-trigger-${roomId}`).click();
    await this.page.getByTestId(`room-bed-status-${status}-${roomId}-${bedId}`).click();
  }

  async expectToastContains(text: string) {
    await expectToastContains(this.page, text);
  }
}
