# 雾屿潮间带研学营运营台（MVP） MVP 规划
*neusoft-elderly-care-mvp*

## 项目概述

**描述**: 面向潮间带生态研学营的基础运营台，提供用户注册登录、学员档案与入营/结营/离营外出登记、营房铺位设置与占用管理、营期配餐周菜单与学员定制、带教档位与辅导记录录入等核心能力，采用 Vue3+Element-Plus 前端与 Spring Boot+MyBatis-Plus 后端，MySQL 持久化，前后端分离通过 REST API 交互。

**目标用户**:
- 研学营管理员（维护用户、营房铺位、基础配置）
- 营务前台（学员建档、入营/结营/离营外出登记、铺位分配）
- 带队导师（辅导记录填写）
- 营养/后勤人员（周菜单维护、学员配餐定制）

## 原始需求

> 1）Java技术 

（2）Vue3、Element-Plus前端框架 

（3）Spring Boot、Spring、SpringMVC、MyBatis、MyBatis-Plus框架技术 

（4）前后端分离数据接口设计 

（5）MySQL数据库， 

注册 任何一个具有中国公民资格的人员都可以通过程序进行注册，获取身份。 

注册信息至少包括：手机号码（身份唯一识别）、登录密码、真实姓名（便于联系）、年龄、性别。 

登录 使用注册信息中的手机号码、登录密码进行登录。 

雾屿潮间带研学营 学员档案 铺位管理 设置营房和铺位 

营期配餐 对每个学员进行定制 

配餐日历 每周营期菜单 

入营登记 学员的入营登记 

结营登记 学员结营 

离营外出登记 学员离营外出 

设置带教配对 设置学员与带队导师关系 

研学增值包 学员购买的研学项目 

营务账号 人员 

带教模块 带教档位 定义档位 

辅导内容 设置辅导内容 

辅导记录 辅导项目进行记录 

请完成任务

## 评分信息

| 维度 | 分值 |
|------|------|
| 等级 | B |
| 总分 | 3.49 |
| 类型权重 | 3.25 |
| 加权总分 | 11.34 |
| 清晰度 | 4 |
| 复杂度 | 3.4 |
| 验证难度 | 1.8 |

## 技术栈

- **前端**: Vue 3 + Vite + Vue Router + Pinia + Element-Plus + Axios
- **后端**: Java 17 + Spring Boot 3 + Spring MVC + Spring Security(JWT) + MyBatis-Plus + Hibernate Validator
- **数据库**: MySQL 8.x
- **选型理由**: Vue3+Element-Plus 适合快速搭建管理后台；Spring Boot 提供成熟的分层与安全体系；MyBatis-Plus 提升 CRUD 开发效率；JWT 便于前后端分离；MySQL 满足结构化业务数据存储与查询。

## 核心功能

### 账号与营务账号 (P0)

- [ ] 公民用户注册：手机号唯一、密码加密存储、填写姓名/年龄/性别
- [ ] 手机号+密码登录获取 JWT，并在前端持久化会话
- [ ] 基础用户列表与启用/停用（管理员）

### 客户档案与出入管理 (P0)

- [ ] 客户建档：基本信息、紧急联系人（可选）、备注
- [ ] 入营登记：设置入营日期、分配铺位、状态变更为“在营”
- [ ] 结营/离营外出登记：记录日期与原因（简化），状态变更

### 营房与铺位管理 (P0)

- [ ] 房间维护：楼层/房间号/类型（可选）
- [ ] 铺位维护：铺位编号、所属营房、状态（空闲/占用/停用）
- [ ] 铺位分配校验：同一铺位同一时间仅允许一个在营学员占用

### 营期配餐（周菜单与客户定制） (P1)

- [ ] 配餐日历：维护每周菜单（按周起始日期+周一到周日）
- [ ] 学员配餐定制：为客户选择餐型/忌口备注/额外要求
- [ ] 按客户查看本周菜单与其定制信息汇总

### 带教辅导（级别与记录） (P1)

- [ ] 带教档位维护：级别名称与描述（如：一级/二级）
- [ ] 辅导内容（项目）简化为自由文本记录项
- [ ] 辅导记录：选择客户、记录日期、辅导内容、执行人

## 页面结构

| 路由 | 页面 | 描述 |
|------|------|------|
| `/login` | 登录/注册 | 用户使用手机号注册并登录，获取系统访问权限。 |
| `/customers` | 学员档案 | 学员建档、入营/结营/离营外出登记与铺位分配的核心操作页面。 |
| `/rooms` | 营房铺位管理 | 维护营房与铺位基础数据，并查看铺位占用情况。 |
| `/meals` | 营期配餐 | 维护每周菜单并为学员记录配餐定制信息。 |
| `/care` | 带教辅导 | 维护带教档位并记录学员辅导执行情况。 |

## 数据模型

### User

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| phone | VARCHAR(20) | 手机号，唯一 |
| password_hash | VARCHAR(255) | 加密后的登录密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| age | INT | 年龄 |
| gender | TINYINT | 性别：0未知/1男/2女 |
| role | VARCHAR(20) | 角色：ADMIN/STAFF |
| status | TINYINT | 状态：1启用/0停用 |
| created_at | DATETIME | 创建时间 |

### Customer

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 学员姓名 |
| phone | VARCHAR(20) | 联系电话（可与注册手机号不同），可选 |
| age | INT | 年龄 |
| gender | TINYINT | 性别：0未知/1男/2女 |
| status | VARCHAR(20) | 状态：DRAFT/RESIDENT/OUTING/DISCHARGED |
| bed_id | BIGINT | 当前铺位ID（在营时必填） |
| checkin_date | DATE | 入营日期 |
| note | VARCHAR(255) | 备注 |
| created_at | DATETIME | 创建时间 |

### Room

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| floor | INT | 楼层 |
| room_no | VARCHAR(20) | 房间号 |
| status | TINYINT | 状态：1启用/0停用 |

### Bed

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| room_id | BIGINT | 所属房间ID |
| bed_no | VARCHAR(20) | 铺位编号（营房内唯一） |
| status | VARCHAR(20) | 状态：AVAILABLE/OCCUPIED/DISABLED |

### MealWeeklyMenu

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| week_start_date | DATE | 周起始日期（建议周一），唯一 |
| mon | VARCHAR(255) | 周一菜单 |
| tue | VARCHAR(255) | 周二菜单 |
| wed | VARCHAR(255) | 周三菜单 |
| thu | VARCHAR(255) | 周四菜单 |
| fri | VARCHAR(255) | 周五菜单 |
| sat | VARCHAR(255) | 周六菜单 |
| sun | VARCHAR(255) | 周日菜单 |
| updated_at | DATETIME | 更新时间 |

### CustomerMealPlan

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| customer_id | BIGINT | 客户ID |
| week_start_date | DATE | 对应周起始日期 |
| meal_type | VARCHAR(20) | 餐型：NORMAL/DIABETIC/LOW_SALT/OTHER |
| diet_taboo | VARCHAR(255) | 忌口信息 |
| note | VARCHAR(255) | 额外备注 |
| created_by | BIGINT | 创建人用户ID |

### CareLevel

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 级别名称 |
| description | VARCHAR(255) | 级别描述 |
| status | TINYINT | 状态：1启用/0停用 |

### CareRecord

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| customer_id | BIGINT | 客户ID |
| care_date | DATETIME | 辅导发生时间 |
| content | VARCHAR(500) | 辅导内容/项目记录（自由文本） |
| performed_by | BIGINT | 执行人用户ID |

## API 端点

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | `none` | 用户注册（手机号唯一），返回用户基础信息 |
| `POST` | `/api/auth/login` | `none` | 用户登录，成功返回 JWT 与用户信息 |
| `GET` | `/api/users` | `required` | 用户列表（管理员） |
| `PATCH` | `/api/users/{id}/status` | `required` | 启用/停用用户（管理员） |
| `GET` | `/api/customers` | `required` | 客户列表查询（可按状态/关键词过滤） |
| `POST` | `/api/customers` | `required` | 新建客户档案 |
| `PATCH` | `/api/customers/{id}/lifecycle` | `required` | 学员入营/结营/离营外出登记（传入动作checkin/discharge/outing与日期、原因、bedId等） |
| `GET` | `/api/rooms` | `required` | 营房列表（包含铺位摘要信息或可按参数includeBeds=true返回铺位列表） |
| `POST` | `/api/rooms` | `required` | 新增/编辑房间（MVP 可用同接口通过是否带id区分，或仅新增） |
| `POST` | `/api/rooms/{roomId}/beds` | `required` | 在指定房间下新增铺位/更新铺位状态（MVP：新增为主，更新可通过payload带bedId与status实现） |
| `PUT` | `/api/meals/weekly-menus/{weekStartDate}` | `required` | 保存指定周起始日期的周菜单（覆盖式更新） |
| `PUT` | `/api/meals/customer-plans/{customerId}/{weekStartDate}` | `required` | 保存学员某周的配餐定制信息（覆盖式更新） |

## 验收标准

### AC-CORE-001 (core)

- **Given**: 用户在注册页填写合法手机号、密码、姓名、年龄、性别且手机号未被注册
- **When**: 点击注册提交
- **Then**: 系统创建用户记录并提示注册成功，随后可使用该手机号密码登录获取 JWT

### AC-CORE-002 (core)

- **Given**: 已登录的工作人员进入学员档案页
- **When**: 新建学员并执行入营登记选择一个空闲铺位
- **Then**: 学员状态变更为 RESIDENT，铺位状态变更为 OCCUPIED，列表中显示学员与铺位绑定关系

### AC-CORE-003 (core)

- **Given**: 已登录用户在营期配餐页选择某个周起始日期
- **When**: 保存该周周一到周日菜单内容
- **Then**: 系统可在同一周选择器下正确加载并展示已保存的周菜单

### AC-EDGE-001 (edge)

- **Given**: 某铺位已被在营学员占用（OCCUPIED）
- **When**: 另一学员尝试入营登记选择该铺位
- **Then**: 接口返回冲突错误并提示“铺位已占用”，不产生重复占用数据

### AC-ERROR-001 (error)

- **Given**: 用户在登录页输入已注册手机号但密码错误
- **When**: 点击登录
- **Then**: 系统返回认证失败并提示账号或密码错误，不下发 JWT

### AC-USABILITY-001 (usability)

- **Given**: 用户在注册或新增学员时未填写必填项或手机号格式不正确
- **When**: 点击提交保存
- **Then**: 前端展示明确的字段级校验提示，且不发起提交或后端返回 400 时可展示可读错误信息

### AC-CORE-004 (core)

- **Given**: 已登录带队导师在带教辅导页选择一个学员并填写辅导内容与时间
- **When**: 提交新增辅导记录
- **Then**: 辅导记录保存成功并在列表中按时间倒序可见，显示执行人为当前登录用户

### AC-EDGE-002 (edge)

- **Given**: 客户当前状态为 DISCHARGED
- **When**: 用户尝试对该客户发起离营外出登记
- **Then**: 系统拒绝该操作并提示状态不允许（仅 RESIDENT 可外出）

## 不在 MVP 范围内

- 费用结算/缴费/发票
- 家属端小程序/短信通知/电话外呼
- 复杂排班、辅导工单流转与审批
- 带队导师关系与研学增值包的完整商品化/订单化管理（仅保留未来扩展空间）
- 数据分析报表与大屏
- 多院区、多租户与细粒度权限体系（仅提供ADMIN/STAFF基础角色）
- 文件上传（证件、合同、体检报告等）

## 实现里程碑

### Phase 1 - 项目骨架与认证

- [ ] 前端：Vue3+Element-Plus 工程初始化、路由与登录态（Pinia + localStorage）
- [ ] 后端：Spring Boot 工程初始化、统一返回体与全局异常处理
- [ ] 数据库：MySQL 初始化表结构（User 等最小集合）
- [ ] 实现注册/登录接口：密码BCrypt加密、JWT签发与鉴权拦截
- [ ] 实现登录/注册页面与表单校验

**验收**: ['可完成注册与登录并获取 JWT', '未登录访问受保护接口返回 401', '前端可保持会话并在刷新后仍可请求受保护接口']

### Phase 2 - 学员与铺位核心流程

- [ ] 实现营房与铺位数据模型、CRUD（以房间页管理为主）
- [ ] 实现客户档案创建与列表查询
- [ ] 实现入营/结营/离营外出生命周期接口与状态机校验
- [ ] 实现铺位占用冲突校验与事务处理（入营成功同时更新学员与铺位）
- [ ] 前端：学员档案页与营房铺位管理页联动选择铺位

**验收**: ['可新增营房与铺位并在页面展示', '可新增学员并完成入营登记，铺位状态正确变更', '占用铺位不可再次分配，返回可读错误提示']

### Phase 3 - 配餐与带教（运营必需）

- [ ] 实现周菜单保存/读取（按weekStartDate唯一）
- [ ] 实现学员配餐定制保存/读取（按customerId+weekStartDate唯一）
- [ ] 实现带教档位（可选）与辅导记录新增/列表
- [ ] 前端：营期配餐页（周菜单+客户定制）与带教辅导页（记录列表与新增）

**验收**: ['可维护任意周的周菜单并可再次加载显示', '可为学员保存某周配餐定制信息并在页面查询到', '可新增辅导记录并按时间倒序展示']
