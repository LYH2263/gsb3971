import { adminAccount } from '../fixtures/accounts';
import { test, expect } from '../fixtures/auth-fixture';
import { uniquePhone } from '../fixtures/factories';
import { LayoutPage } from '../pages/LayoutPage';
import { MealsPage } from '../pages/MealsPage';

function weekStart(offsetWeeks = 0): string {
  const date = new Date();
  const offsetDays = (date.getDay() + 6) % 7;
  date.setDate(date.getDate() - offsetDays + offsetWeeks * 7);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

test.describe('营期配餐', () => {
  test('@smoke @full 周菜单保存并可回显', async ({ page, loginAs, apiClient }) => {
    const mealsPage = new MealsPage(page);
    const layoutPage = new LayoutPage(page);
    await loginAs(page, 'admin');
    await mealsPage.goto();

    const currentWeek = weekStart();
    const menuPayload = {
      mon: `周一餐-${Date.now()}`,
      tue: '周二餐',
      wed: '周三餐',
      thu: '周四餐',
      fri: '周五餐',
      sat: '周六餐',
      sun: '周日餐'
    };

    await mealsPage.setWeekStartDate(currentWeek);
    await mealsPage.fillWeeklyMenu(menuPayload);
    await mealsPage.saveWeeklyMenu();
    await mealsPage.expectToastContains('周菜单保存成功');

    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const menuFromApi = await apiClient.getWeeklyMenu(token, currentWeek);
    expect(menuFromApi.mon).toBe(menuPayload.mon);

    await layoutPage.gotoCustomers();
    await layoutPage.gotoMeals();
    await mealsPage.setWeekStartDate(currentWeek);
    await mealsPage.expectWeeklyMenuValue('mon', menuPayload.mon);
    await mealsPage.expectWeeklyMenuValue('sun', menuPayload.sun);
  });

  test('@smoke @full 切换空周时菜单为空态', async ({ page, loginAs }) => {
    const mealsPage = new MealsPage(page);
    await loginAs(page, 'admin');
    await mealsPage.goto();

    const nextQuarterWeek = weekStart(12);
    await mealsPage.setWeekStartDate(nextQuarterWeek);
    await mealsPage.expectWeeklyMenuAllEmpty();

    await expect(page.getByTestId('meal-plan-table').locator('.el-table__body-wrapper tbody tr')).toHaveCount(0);
  });

  test('@smoke @full 学员配餐定制未选客户告警与保存回显', async ({ page, loginAs, apiClient }) => {
    const { token } = await apiClient.login(adminAccount.phone, adminAccount.password);
    const customerName = `配餐学员-${Date.now()}`;
    const customer = await apiClient.createCustomer(token, {
      name: customerName,
      phone: uniquePhone(),
      age: 71,
      gender: 1,
      note: '配餐定制测试'
    });

    const mealsPage = new MealsPage(page);
    const layoutPage = new LayoutPage(page);
    const currentWeek = weekStart();

    await loginAs(page, 'admin');
    await mealsPage.goto();
    await mealsPage.setWeekStartDate(currentWeek);

    await mealsPage.saveCustomerMealPlan();
    await mealsPage.expectToastContains('请先选择客户');

    await mealsPage.selectCustomer(customerName);
    await mealsPage.fillMealPlan({
      mealType: 'DIABETIC',
      dietTaboo: '避免高糖',
      note: '早餐控糖'
    });
    await mealsPage.saveCustomerMealPlan();
    await mealsPage.expectToastContains('学员配餐定制保存成功');
    await mealsPage.expectPlanTableContains(customerName);
    await mealsPage.expectPlanTableContains('糖尿病餐');

    const planFromApi = await apiClient.getCustomerMealPlan(token, customer.id, currentWeek);
    expect(planFromApi.mealType).toBe('DIABETIC');
    expect(planFromApi.dietTaboo).toBe('避免高糖');

    await layoutPage.gotoCustomers();
    await layoutPage.gotoMeals();
    await mealsPage.setWeekStartDate(currentWeek);
    await mealsPage.selectCustomer(customerName);
    await mealsPage.expectMealPlanValues({
      mealTypeLabel: '糖尿病餐',
      dietTaboo: '避免高糖',
      note: '早餐控糖'
    });
  });
});
