# MVP 验收对照

> 说明：本文件按 `mvp.md` 验收标准逐条对照，标注结果与证据。

## AC-CORE-001（注册+登录获取 JWT）

- 结果：通过
- 证据：
  - 后端接口：`/api/auth/register`、`/api/auth/login`
  - 自动化测试：`backend/src/test/java/com/hanyu/learning/ApiIntegrationTests.java` 中 `shouldRegisterAndLogin`
  - E2E：`e2e/specs/01-auth-and-routing.spec.ts`（注册成功、登录成功、JWT 写入会话）
  - 前端页面：`frontend/src/pages/LoginView.vue`

## AC-CORE-002（学员入营后状态与铺位占用变更）

- 结果：通过
- 证据：
  - 后端事务逻辑：`backend/src/main/java/com/hanyu/learning/service/CustomerService.java`
  - 自动化测试：`shouldCheckinAndRejectOccupiedBed` 的第一段校验（入营成功）
  - E2E：`e2e/specs/02-customers-lifecycle.spec.ts`（入营后学员状态与铺位占用变化）
  - 前端页面：`frontend/src/pages/CustomersView.vue`

## AC-CORE-003（周菜单保存并可回显）

- 结果：通过
- 证据：
  - 后端接口：`PUT/GET /api/meals/weekly-menus/{weekStartDate}`
  - 自动化测试：`shouldSaveAndLoadWeeklyMenu`
  - E2E：`e2e/specs/04-meals.spec.ts`（保存周菜单并跨路由回显）
  - 前端页面：`frontend/src/pages/MealsView.vue`

## AC-EDGE-001（铺位占用冲突）

- 结果：通过
- 证据：
  - 后端冲突校验：`CustomerService#checkin`
  - 自动化测试：`shouldCheckinAndRejectOccupiedBed` 断言 `40902` + “铺位已占用”
  - E2E：`e2e/specs/02-customers-lifecycle.spec.ts`（同铺位二次入营冲突）+ `e2e/specs/03-rooms-beds.spec.ts`（占用铺位禁停用）

## AC-ERROR-001（错误密码登录失败）

- 结果：通过
- 证据：
  - 后端登录鉴权：`AuthService#login`
  - 自动化测试：`shouldRejectWrongPassword`（HTTP 401，业务码 40101）
  - E2E：`e2e/specs/01-auth-and-routing.spec.ts`（错误密码登录不下发 token）

## AC-USABILITY-001（字段级校验与可读错误）

- 结果：通过
- 证据：
  - 前端校验：`LoginView.vue`、`CustomersView.vue` 中表单 rules（手机号/必填）
  - 后端校验：`dto/request/*.java` + `GlobalExceptionHandler` 输出字段级信息
  - E2E：`e2e/specs/01-auth-and-routing.spec.ts`、`e2e/specs/02-customers-lifecycle.spec.ts`、`e2e/specs/07-api-edge-supplement.spec.ts`

## AC-CORE-004（辅导记录新增并倒序展示）

- 结果：通过
- 证据：
  - 后端接口与排序：`CareService#listCareRecords`
  - 自动化测试：`shouldCreateCareRecordAndSortByDateDesc`
  - E2E：`e2e/specs/05-care.spec.ts`（新增辅导记录并验证倒序）
  - 前端页面：`frontend/src/pages/CareView.vue`

## AC-EDGE-002（DISCHARGED 禁止外出）

- 结果：通过
- 证据：
  - 后端状态机约束：`CustomerService#outing`
  - 自动化测试：`shouldRejectOutingForDischargedCustomer`（业务码 40903）
  - E2E：`e2e/specs/02-customers-lifecycle.spec.ts`（结营后离营外出按钮禁用）+ `e2e/specs/07-api-edge-supplement.spec.ts`（非法生命周期动作拒绝）

## E2E 全链路覆盖映射（页面 + 分支/边界）

- 页面覆盖：
  - `/login`：`e2e/specs/01-auth-and-routing.spec.ts`
  - `/customers`：`e2e/specs/02-customers-lifecycle.spec.ts`
  - `/rooms`：`e2e/specs/03-rooms-beds.spec.ts`
  - `/meals`：`e2e/specs/04-meals.spec.ts`
  - `/care`：`e2e/specs/05-care.spec.ts`
  - `/service-focus`：`e2e/specs/08-service-focus.spec.ts`
  - `/users`：`e2e/specs/06-users-admin.spec.ts`
- API 边界补齐：`e2e/specs/07-api-edge-supplement.spec.ts`（含带教配对/研学增值包非法请求）
- 鉴权与角色分支：
  - 未登录拦截（401 重定向）：`01-auth-and-routing`
  - 非管理员拦截（403）：`01-auth-and-routing` + `07-api-edge-supplement`
  - 被停用用户登录失败：`06-users-admin`

## 质量门禁结果

已执行并通过：

- `pnpm lint`
- `pnpm test`
- `pnpm e2e:smoke`（17 passed）
- `pnpm e2e:full`（22 passed）
- `pnpm build`

## 新增需求实现（带教配对 / 研学增值包）

- 设置带教配对（客户与带队导师关系）：已实现
  - 后端接口：`GET /api/services/objects`、`PUT /api/services/objects/{customerId}`
  - 前端页面：`/service-focus` 中“设置带教配对”卡片与弹窗
- 研学增值包（学员购买的研学项目）：已实现
  - 后端接口：`GET /api/services/focuses`、`POST /api/services/focuses`
  - 前端页面：`/service-focus` 中“研学增值包”录入表单与列表
- 自动化证据：
  - 后端集成测试：`backend/src/test/java/com/hanyu/learning/ApiIntegrationTests.java` 中 `shouldAssignServiceObjectAndCreateServiceFocus`
  - E2E：`e2e/specs/08-service-focus.spec.ts`（带教配对设置成功、研学增值包保存成功、页面校验分支）
