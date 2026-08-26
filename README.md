# 雾屿潮间带研学营运营台（MVP）

面向潮间带生态研学营的前后端分离运营台，覆盖注册登录、学员档案与入营/结营/离营外出、营房铺位管理、营期配餐周菜单与学员定制、带教档位与辅导记录，并支持“设置带教配对（学员-带队导师关系）”与“研学增值包（学员购买的研学项目）”。

## 原始需求

> 1）Java技术

> 2）Vue3、Element-Plus前端框架

> 3）Spring Boot、Spring、SpringMVC、MyBatis、MyBatis-Plus框架技术

> 4）前后端分离数据接口设计

> 5）MySQL数据库，

> 注册 任何一个具有中国公民资格的人员都可以通过程序进行注册，获取身份。

> 注册信息至少包括：手机号码（身份唯一识别）、登录密码、真实姓名（便于联系）、年龄、性别。

> 登录 使用注册信息中的手机号码、登录密码进行登录。

> 雾屿潮间带研学营 学员档案 铺位管理 设置营房和铺位

> 营期配餐 对每个学员进行定制

> 配餐日历 每周营期菜单

> 入营登记 学员的入营登记

> 结营登记 学员结营

> 离营外出登记 学员离营外出

> 设置带教配对 设置学员与带队导师关系

> 研学增值包 学员购买的研学项目

> 营务账号 人员

> 带教模块 带教档位 定义档位

> 辅导内容 设置辅导内容

> 辅导记录 辅导项目进行记录

> 请完成任务

## 技术栈

- 前端：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Axios
- 后端：Java 17 + Spring Boot 3 + Spring Security (JWT) + MyBatis-Plus + Hibernate Validator
- 数据库：MySQL 8.4
- 容器：Docker Compose（全部非 Alpine 镜像）
  - `mysql:8.4`
  - `maven:3.9-eclipse-temurin-17` / `eclipse-temurin:17-jre-jammy`
  - `node:20-bookworm-slim` / `nginx:1.27-bookworm`

## 目录结构

- `frontend`：管理后台前端
- `backend`：Spring Boot API 服务
- `ops/mysql/init`：MySQL 初始化 SQL（建表 + 种子数据）
- `docs/acceptance.md`：验收标准对照说明

## 代码架构

### 前端架构（Vue 3）

- 分层结构：
  - `src/pages`：页面级容器（`/login`、`/customers`、`/rooms`、`/meals`、`/care`、`/service-focus`、`/users`）
  - `src/api`：按业务域拆分 API 客户端（auth/customers/rooms/meals/care/services/users）
  - `src/stores`：Pinia 会话状态（JWT、当前用户、管理员判断）
  - `src/router`：路由守卫（未登录拦截、管理员权限拦截）
  - `src/types`：前后端 DTO 对齐类型定义
- 权限与导航：
  - `AppLayout.vue` 根据 `authStore.isAdmin` 动态渲染管理员菜单（研学增值包、营务账号）。
  - 路由 `meta.requiresAuth / meta.requiresAdmin` 与本地会话联动实现访问控制。
- 交互契约：
  - 核心页面补充 `data-testid`，作为 Playwright E2E 稳定选择器契约。

### 后端架构（Spring Boot 3）

- 分层结构：
  - `controller`：REST 接口定义与参数入口
  - `service`：业务规则、状态机、事务边界
  - `mapper`：MyBatis-Plus 数据访问
  - `domain/entity`：数据库实体映射
  - `dto/request`、`dto/view`：入参与出参模型
  - `security`：JWT 认证过滤、用户上下文、权限校验
  - `common`：统一响应体、错误码、全局异常处理
- 关键业务设计：
  - 客户生命周期状态机：`DRAFT -> RESIDENT -> OUTING -> RESIDENT -> DISCHARGED`
  - 入营/结营操作事务化，保证学员状态与铺位状态一致性。
  - 带教配对与研学增值包模块独立建模，支持管理员分配带队导师与服务购买记录维护。

### 数据与部署架构

- 数据初始化：
  - `ops/mysql/init/001_schema.sql`：建表与约束
  - `ops/mysql/init/002_seed.sql`：管理员/导师及基础业务种子
- 容器编排：
  - `docker-compose.yml` 编排 `mysql + backend + frontend`
  - 镜像均为非 Alpine 体系（bookworm/jammy/temurin）
- 反向代理：
  - 前端 Nginx 统一入口并转发 `/api` 到后端服务。

## 技术细节

### 鉴权与权限控制

- 登录成功签发 JWT，前端存储于 `localStorage`（`elderly_token`、`elderly_user`）。
- Axios 请求拦截器自动注入 `Authorization: Bearer <token>`。
- 后端通过 JWT 过滤器解析用户身份；权限校验由 `PermissionService` 执行：
  - `requireLogin`：登录即可访问
  - `requireAdmin`：仅管理员访问

### 数据一致性与约束

- 核心唯一约束：
  - `users.phone`
  - `rooms(floor, room_no)`
  - `beds(room_id, bed_no)`
  - `meal_weekly_menu.week_start_date`
  - `customer_meal_plan(customer_id, week_start_date)`
- 关键业务约束：
  - 已占用铺位不可再次入营（返回 409）
  - `DISCHARGED` 客户禁止离营外出登记（返回 409）
  - 研学增值包 `expireDate` 不可早于 `purchaseDate`（返回 400）
  - 服务状态仅允许 `ACTIVE/PAUSED/ENDED`

### 错误处理与接口规范

- 统一响应结构：
  - 成功：`{ code: 0, message: "OK", data: ... }`
  - 失败：`{ code: 非0, message: 可读错误, data: null }`
- 关键错误码：
  - `40001` 参数校验失败
  - `40101` 凭证错误
  - `40102` 未登录
  - `40301` 权限不足
  - `40902` 铺位占用冲突
  - `40903` 客户状态冲突

### 测试与质量门禁

- 前端：Vitest（表单校验、auth store）
- 后端：SpringBootTest + MockMvc 集成测试（认证、铺位冲突、状态机、配餐、带教、带教配对/研学增值包）
- E2E：Playwright 分层执行
  - `@smoke`：关键主链路与失败链路
  - `@full`：全页面分支 + API 边界补齐
- 报告产物：
  - `playwright-report/`（HTML）
  - `test-results/`（trace/screenshot/video）

## 本地开发

### 1. 安装依赖

```bash
pnpm install
```

### 2. 启动后端（本地 MySQL）

```bash
pnpm backend:dev
```

### 3. 启动前端

```bash
pnpm frontend:dev
```

默认地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 一键容器启动

```bash
docker compose up --build -d
```

服务端口：

- Frontend：`http://localhost:5174`（来自当前 `.env` 的 `FRONTEND_PORT`）
- Backend：`http://localhost:8082`（来自当前 `.env` 的 `BACKEND_PORT`）
- MySQL：`localhost:3307`（来自当前 `.env` 的 `MYSQL_PORT`）

说明：端口最终以 `.env` 为准；`.env.example` 里提供的是初始示例值（`5173/8080/3306`）。

停止并清理：

```bash
docker compose down -v
```

## 默认账号（种子数据）

- 管理员：`13800000001 / Admin@123`
- 导师：`13800000002 / Staff@123`

## 质量验证命令

```bash
pnpm lint
pnpm test
pnpm e2e:smoke
pnpm e2e:full
pnpm build
```

## E2E 分层执行

- `pnpm e2e:smoke`：CI 快速门禁（全页面主链路 + 关键失败链路）
- `pnpm e2e:full`：全量回归（页面分支 + API 边界补齐）
- `pnpm e2e:all`：串行执行 smoke/full
- `pnpm e2e:headed`：本地有头模式调试

测试标签约定：

- `@smoke`：快速门禁用例
- `@full`：全量回归用例

失败留痕产物：

- `playwright-report/`：HTML 报告
- `test-results/`：失败截图、视频、trace

## 核心接口（节选）

- 认证：`POST /api/auth/register`、`POST /api/auth/login`
- 用户：`GET /api/users`、`PATCH /api/users/{id}/status`
- 客户：`GET /api/customers`、`POST /api/customers`、`PATCH /api/customers/{id}/lifecycle`
- 营房铺位：`GET /api/rooms`、`POST /api/rooms`、`POST /api/rooms/{roomId}/beds`
- 配餐：
  - `PUT /api/meals/weekly-menus/{weekStartDate}`
  - `GET /api/meals/weekly-menus/{weekStartDate}`
  - `PUT /api/meals/customer-plans/{customerId}/{weekStartDate}`
  - `GET /api/meals/customer-plans/{customerId}/{weekStartDate}`
  - `GET /api/meals/customer-plans`
- 带教：
  - `GET /api/care-levels`、`POST /api/care-levels`、`PATCH /api/care-levels/{id}/status`
  - `GET /api/care-records`、`POST /api/care-records`
- 带教配对与研学增值包：
  - `GET /api/services/objects`、`PUT /api/services/objects/{customerId}`
  - `GET /api/services/focuses`、`POST /api/services/focuses`

## 环境变量

参考 `.env.example`：

- 后端：`DB_HOST` `DB_PORT` `DB_NAME` `DB_USER` `DB_PASSWORD` `JWT_SECRET` `JWT_EXPIRE_SECONDS`
- 前端：`VITE_API_BASE_URL`
- 容器端口：`MYSQL_PORT` `BACKEND_PORT` `FRONTEND_PORT`
