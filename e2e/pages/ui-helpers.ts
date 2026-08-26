import { expect, type Locator, type Page } from '@playwright/test';

export function latestToast(page: Page): Locator {
  return page.locator('.el-message').last();
}

export async function expectToastContains(page: Page, text: string) {
  await expect(page.locator('.el-message__content').filter({ hasText: text }).last()).toBeVisible();
}

export function formControlByTestId(page: Page, testId: string): Locator {
  return page
    .locator(`:is(input,textarea)[data-testid="${testId}"], [data-testid="${testId}"] :is(input,textarea)`)
    .first();
}

function fieldByTestId(page: Page, testId: string): Locator {
  return page
    .locator(
      [
        `:is(input,textarea,[role="combobox"])[data-testid="${testId}"]`,
        `[data-testid="${testId}"] :is(input,textarea,[role="combobox"])`,
        `[data-testid="${testId}"]`
      ].join(', ')
    )
    .first();
}

async function writeValue(locator: Locator, value: string) {
  await locator.click();
  await locator.press('Control+A').catch(() => undefined);
  await locator.press('Meta+A').catch(() => undefined);
  const tagName = (await locator.evaluate((node) => node.tagName.toLowerCase()).catch(() => '')) as string;
  if (tagName === 'input' || tagName === 'textarea') {
    await locator.fill(value);
  } else {
    await locator.type(value);
  }
  await locator.press('Enter').catch(() => undefined);
}

export async function selectOptionByLabel(page: Page, testId: string, optionLabel: string) {
  await page.getByTestId(testId).click();
  const option = page.getByRole('option', { name: optionLabel }).first();
  await expect(option).toBeVisible();
  await option.click();
}

export async function fillDateInputByTestId(page: Page, testId: string, value: string) {
  const directField = fieldByTestId(page, testId);
  if (await directField.count()) {
    await writeValue(directField, value);
    return;
  }

  const fallbackLabels: Record<string, string> = {
    'menu-week-start-date': '选择周起始日期',
    'care-record-date': '辅导时间',
    'customer-lifecycle-date': '办理日期'
  };
  const label = fallbackLabels[testId];
  if (label) {
    const combobox = page.getByRole('combobox', { name: label }).first();
    if (await combobox.count()) {
      await writeValue(combobox, value);
      return;
    }
  }

  throw new Error(`无法定位日期输入控件: ${testId}`);
}
