# 农机共享平台

服务合作社、农机手、种植大户和乡镇农服中心的农机共享管理平台。

## 项目简介

本平台实现了农机共享的全流程管理，包括：
- 种植大户提交地块信息、预约作业
- 合作社维护机具信息、安排调度
- 农机手上报到田时间、作业轨迹、油耗票据和完工照片
- 农服中心核对补贴政策、处理跨村作业和面积争议
- 完整的作业流程：预约排程 → 雨后改期 → 进田作业 → 面积确认 → 油耗补贴 → 费用结算

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL 8.0
- Lombok

### 前端
- Vue 3
- Vite 5
- Element Plus
- Vue Router 4
- Pinia
- Axios
- ECharts

## 原始需求

> 搭建一个服务合作社、农机手、种植大户和乡镇农服中心的农机共享平台，Vue3 页面展示地块、机具、预约和补贴核算，Spring Boot 保存作业单、轨迹、油票和结算流水。种植大户提交地块面积、作物类型、作业窗口和联系人；合作社维护机具型号、小时表、驾驶员、可作业区域和服务价格；农机手上报到田时间、作业轨迹、油耗票据和完工照片；农服中心核对补贴政策、跨村作业和面积争议。系统要把预约排程、雨后改期、进田作业、面积确认、油耗补贴和费用结算串起来。雨后无法进田、面积不一致、跨村调度和油票异常要产生不同处理路径。

## 启动方式

### 前置要求

- Docker & Docker Compose
- 或 JDK 17 + Maven 3.8+ + Node.js 18+ + MySQL 8.0

### Docker 一键启动（推荐）

#### 1. 启动项目

```bash
docker compose up --build
```

后台运行：

```bash
docker compose up --build -d
```

#### 2. 访问地址

- 前端页面：http://localhost
- 后端API：http://localhost:8080/api
- MySQL数据库：localhost:3306（账号：root / root123456）

#### 3. 停止服务

```bash
docker compose down
```

保留数据卷：

```bash
docker compose down -v
```

### 本地开发启动

#### 1. 启动数据库

安装并启动 MySQL 8.0，创建数据库：

```sql
CREATE DATABASE agricultural_machinery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `backend/src/main/resources/application.yml` 中的数据库连接信息。

#### 2. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080/api

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端页面访问：http://localhost:3000

## 功能模块

### 1. 数据概览 (Dashboard)
- 地块、机具、预约、补贴统计
- 作业状态分布图表
- 月度作业量趋势
- 最近作业动态时间线

### 2. 地块管理
- 种植大户维护地块信息（面积、位置、作物类型、土壤、联系人等）
- 支持从地块直接发起预约

### 3. 机具管理
- 合作社维护机具信息（型号、类型、车牌号、小时表、驾驶员、服务区域、价格等）
- 机具可用状态管理

### 4. 预约管理
- 种植大户提交作业预约
- 合作社安排机具和农机手排程
- 支持雨后改期和重新排程
- 跨村作业标识
- 预约状态流转：待排程 → 已排程 → 雨后延期 → 作业中 → 完成/取消/面积争议

### 5. 作业单管理
- 从预约生成作业单
- 农机手上报到田、开始作业、完工上报
- 种植大户面积确认（一致/不一致争议）
- 农服中心处理面积争议

### 6. 油票管理
- 农机手上传加油票据
- 系统自动检测异常（加油量异常、油价异常）
- 农服中心审核油票

### 7. 补贴核算
- 根据作业面积、油耗、政策自动计算补贴
- 包含作业补贴、燃油补贴、跨村补贴
- 补贴审核和支付流程

### 8. 补贴政策
- 农服中心维护补贴政策
- 按作业类型、区域设置不同补贴标准
- 跨村作业额外补贴配置

## 业务流程

### 正常作业流程
1. 种植大户录入地块信息
2. 种植大户提交作业预约（选择地块、作业类型、日期时段）
3. 合作社查看预约，安排机具和农机手排程
4. 生成作业单，派发给农机手
5. 农机手到达田间，上报到田时间
6. 农机手开始作业，记录作业轨迹
7. 农机手完工，上报实测面积和完工照片，上传油票
8. 种植大户确认面积（如一致则进入结算）
9. 系统自动核算补贴和费用
10. 农服中心审核结算单
11. 费用支付，流程结束

### 异常处理路径
- **雨后无法进田**：预约状态改为"雨后延期"，支持重新选择作业日期排程
- **面积不一致**：种植大户确认面积时选择"不一致"并说明原因，状态进入"面积争议"，由农服中心调解后确定最终面积
- **跨村调度**：预约时标记跨村作业，核算时自动计算跨村额外补贴
- **油票异常**：系统自动检测加油量或单价异常，标记为异常油票，需农服中心人工审核确认

## 目录结构

```
wl-265/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/.../
│   │   ├── entity/            # 数据实体
│   │   ├── enums/             # 枚举类
│   │   ├── repository/        # 数据访问层
│   │   ├── service/           # 业务逻辑层
│   │   ├── controller/        # API接口层
│   │   └── MachinerySharingPlatformApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   ├── .dockerignore
│   └── pom.xml
├── frontend/                   # Vue3 前端
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 组件
│   │   ├── router/            # 路由配置
│   │   ├── store/             # 状态管理
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── Dockerfile                  # 根目录统一构建
├── docker-compose.yml          # Docker编排
├── .dockerignore
└── README.md
```

## API接口清单

### 地块管理
- `GET /api/fields` - 获取所有地块
- `GET /api/fields/{id}` - 获取地块详情
- `GET /api/fields/grower/{growerId}` - 获取种植大户的地块
- `POST /api/fields` - 新增地块
- `PUT /api/fields/{id}` - 修改地块
- `DELETE /api/fields/{id}` - 删除地块

### 机具管理
- `GET /api/machines` - 获取所有机具
- `GET /api/machines/{id}` - 获取机具详情
- `GET /api/machines/available` - 获取可用机具
- `POST /api/machines` - 新增机具
- `PUT /api/machines/{id}` - 修改机具
- `DELETE /api/machines/{id}` - 删除机具

### 预约管理
- `GET /api/appointments` - 获取所有预约
- `GET /api/appointments/{id}` - 获取预约详情
- `POST /api/appointments` - 新增预约
- `POST /api/appointments/{id}/schedule` - 预约排程
- `POST /api/appointments/{id}/rain-delay` - 雨后改期
- `POST /api/appointments/{id}/reschedule` - 重新排程
- `POST /api/appointments/{id}/cancel` - 取消预约

### 作业单管理
- `GET /api/work-orders` - 获取所有作业单
- `GET /api/work-orders/{id}` - 获取作业单详情
- `POST /api/work-orders/from-appointment/{appointmentId}` - 从预约生成作业单
- `POST /api/work-orders/{id}/arrive` - 报到田
- `POST /api/work-orders/{id}/start` - 开始作业
- `POST /api/work-orders/{id}/complete` - 完工上报
- `POST /api/work-orders/{id}/confirm-area` - 面积确认
- `POST /api/work-orders/{id}/resolve-dispute` - 处理面积争议

### 油票管理
- `GET /api/fuel-tickets` - 获取所有油票
- `GET /api/fuel-tickets/abnormal` - 获取异常油票
- `POST /api/fuel-tickets` - 上传油票
- `POST /api/fuel-tickets/{id}/verify` - 审核油票

### 补贴核算
- `GET /api/settlements` - 获取所有结算单
- `POST /api/settlements/calculate/{workOrderId}` - 核算补贴
- `POST /api/settlements/{id}/approve` - 审核通过
- `POST /api/settlements/{id}/reject` - 驳回
- `POST /api/settlements/{id}/paid` - 标记已支付

### 补贴政策
- `GET /api/subsidy-policies` - 获取所有政策
- `GET /api/subsidy-policies/active` - 获取生效中政策
- `POST /api/subsidy-policies` - 新增政策
- `PUT /api/subsidy-policies/{id}` - 修改政策
- `DELETE /api/subsidy-policies/{id}` - 删除政策

### 轨迹管理
- `GET /api/trajectory/work-order/{workOrderId}` - 获取作业轨迹
- `POST /api/trajectory` - 上报轨迹点
