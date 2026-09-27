# dog-config · 业务通用配置管理系统

> 面向业务系统的统一配置管理基础设施。用 **Page → Group → Item** 三级模型，把零散的业务配置（订单 / 商品 / 物流…）统一存储、统一管理、统一访问。配置 **Key** 是业务访问的稳定契约。

## 这是什么

业务系统里总有大量「可调的业务开关和参数」：订单超时时间、是否允许超卖、默认物流商、库存预警值……它们散落在各处的常量、数据库字段或硬编码里，改一次要发版，查一处要找半天。

dog-config 把它们收进来集中管理，并给业务方一个**类型化、带兜底**的读取接口。

**它不是什么：** 它不是 Nacos / Apollo 这类应用运行时配置中心。dog-config 管的是**业务通用配置**，形态是「管理人员在后台维护，业务代码按 Key 读取」，不负责应用自身的启动配置、也不做实时推送。

## 核心模型

```
Page（页面 / 业务域）        例：订单配置 ORDER
  └── Group（分组）          例：自动取消 AUTO_CANCEL
        └── Item（配置项）     key = order.auto.cancel.minutes
                              valueType = INTEGER
                              value = 30 / defaultValue = 30
                              componentType = NUMBER
```

- **Page** 用 `code` 标识一个业务域，全局唯一。
- **Group** 归属于某个 Page，`(page_id, code)` 唯一，用于给配置分组。
- **Item** 归属于某个 Group，`key` **全局唯一**——这是业务代码读取时唯一需要记住的东西。
- 每个 Item 声明 `valueType`（如何解析）、`componentType`（后台如何渲染）、`value` / `defaultValue`（原始字符串）与 `options`（下拉选项）。

## 功能特性

- **三级模型 CRUD**：Page / Group / Item 的增删改查、启停（ACTIVE / DISABLED）、排序。
- **6 种值类型**：`STRING` / `INTEGER` / `LONG` / `DECIMAL` / `BOOLEAN` / `JSON`。
- **6 种组件类型**：`INPUT` / `NUMBER` / `SWITCH` / `SELECT` / `RADIO` / `TEXTAREA`，驱动后台渲染。
- **默认值兜底**：`value` 为空时自动回落到 `defaultValue`。
- **读取 API**：单条 / 批量 keys / 前缀批量。
- **类型化 SDK**：业务系统引入 `dog-config-sdk`，用 `getInt` / `getBoolean` / `getJson` 等直接拿到强类型值。
- **管理后台**：原生 HTML/JS 静态页，无前端构建链。
- **软删除**：删除使用 `deleted` 列标记，保留审计痕迹。

## 快速开始

前置：**JDK 17**、**Maven 3.8+**、**本地 MySQL**（默认账号 `admin/123456`，可在 `dog-config-web/src/main/resources/application.yml` 修改）。

> 注意：主应用**不会**自动建表（`spring.sql.init.mode: never`），必须先手动初始化数据库。

```bash
# 1. 建库
mysql -uadmin -p123456 -e "CREATE DATABASE dog_config CHARACTER SET utf8mb4"

# 2. 建表 + 种子数据（订单 / 商品 / 物流示例配置）
mysql -uadmin -p123456 dog_config < dog-config-web/src/main/resources/db/schema.sql
mysql -uadmin -p123456 dog_config < dog-config-web/src/main/resources/db/data.sql

# 3. 编译并安装到本地仓库（core / sdk 需先安装）
mvn install

# 4. 启动（端口 8080）
mvn -pl dog-config-web spring-boot:run
```

启动后：

- 管理后台：http://localhost:8080/
- 读取接口：`GET http://localhost:8080/api/configs/order.auto.cancel.minutes`

## 使用方式

### 方式一：SDK（Java 业务系统推荐）

```xml
<dependency>
    <groupId>com.echozoo</groupId>
    <artifactId>dog-config-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

```java
@Autowired
ConfigService configService;

Integer minutes     = configService.getInt("order.auto.cancel.minutes");        // 30
Boolean overSell    = configService.getBoolean("order.allow.over.sell");        // false
String  channel     = configService.getString("logistics.default.channel", "SF"); // 带兜底参数
BigDecimal rate     = configService.getDecimal("order.fee.rate");
OrderRule rule      = configService.getJson("order.rule", OrderRule.class);      // JSON 反序列化
Map<String, Object> batch  = configService.getBatch(List.of("order.timeout", "product.stock.warning"));
Map<String, Object> prefix = configService.getByPrefix("order.");
```

### 方式二：HTTP 读取接口

```bash
GET /api/configs/{key}          # 单条
GET /api/configs?keys=a,b,c     # 批量 keys
GET /api/configs?prefix=order.  # 前缀批量
```

统一响应体（业务错误也返回 HTTP 200，用 `code` 表达）：

```json
{ "code": 0, "message": "ok", "data": 30 }
```

### 管理 API

| 资源 | 端点 |
| --- | --- |
| Page | `GET/POST /api/pages`，`GET/PUT/DELETE /api/pages/{id}`，`PATCH /api/pages/{id}/status` |
| Group | `POST /api/pages/{pageId}/groups`，`GET /api/pages/{pageId}/groups`，`GET/PUT/DELETE /api/groups/{id}`，`PATCH /api/groups/{id}/status` |
| Item | `POST /api/groups/{groupId}/items`，`GET /api/groups/{groupId}/items`，`GET/PUT/DELETE /api/items/{id}`，`PATCH /api/items/{id}/status`，`PATCH /api/items/{id}/value` |

错误码：`40000` 参数校验失败 / `40400` 资源不存在 / `40900` 唯一性冲突 / `50000` 服务器内部错误。

## 读取语义（稳定契约）

```
value 非空                      → 使用 value
value 空，defaultValue 非空     → 使用 defaultValue（DB 集中兜底）
value 空，defaultValue 空       → 带参重载返回调用方传入参数；无参重载返回 null
```

这条语义是业务依赖的稳定契约，不会随功能迭代改变。

## 模块结构

```text
dog-config/
├── dog-config-sdk/   # 独立 SDK：只定义 ConfigService 读取契约，不含实现
├── dog-config-core/  # 领域模型 + MyBatis-Plus Mapper + ConfigService 实现
└── dog-config-web/   # Spring Boot 启动器 + 管理 API + HTTP 读取接口 + 静态后台
```

依赖方向：`core → sdk`，`web → core + sdk`。接口契约只定义在 `sdk`，实现放 `core`，`web` 只做 HTTP 暴露。

技术栈：Java 17 · Spring Boot 3.3.5 · MyBatis-Plus 3.5.7 · MySQL · 原生 HTML/JS。

## 当前状态

| 状态 | 内容 |
| --- | --- |
| ✅ 已完成 | v0.1 基础：Page/Group/Item CRUD 与启停、6 种值类型、6 种组件类型、options、读取 API（单条/批量/前缀）、类型化 SDK、管理后台、种子数据 |
| 🚧 进行中 | `harden-config-writes`：写入值/选项/组件校验、软删感知的唯一性 409、父级删除保护 |
| 🚧 进行中 | `config-item-versioning`：配置值版本记录、版本查询、回滚、软删恢复 |

进度以 `openspec/changes/` 为准（`openspec list` 可查看）。

## 路线图

以下是 V1 暂未纳入、后续按需推进的能力：

- 缓存（减少配置读取对 DB 的压力）
- 多租户与权限控制
- 配置变更的实时推送 / Long Polling / MQ
- 审批、灰度发布
- 更多配置类型与校验规则

## 文档与规格

项目采用 **Spec-Driven Development**，规格与变更记录统一在 `openspec/`：

| 位置 | 内容 |
| --- | --- |
| `openspec/specs/` | 能力规格（canonical，随实现沉淀） |
| `openspec/changes/` | 进行中的变更（proposal / design / specs / tasks） |
| `openspec/changes/archive/` | 已归档变更 |
| `doc/product/v0.1/` | 业务通用配置管理系统需求规格说明书 v1.0 |
| `specs/v0.1/` | 早期设计文档（迁移进 `openspec/` 后删除） |

## 开发与贡献

- **流程**：先确认 `openspec/` 中的规格再实现，范围以 change 为界。新功能用 `openspec new change <name>` 起一个变更，按 proposal → design → specs → tasks 推进，完成后归档。
- **分层**：接口契约只改 `dog-config-sdk`，实现放 `dog-config-core`，`web` 只做 HTTP 暴露。
- **持久化**：数据库结构变更需同步 `db/schema.sql`；软删除统一用 `@TableLogic`（`deleted` 列）。
- **测试**：`core` 用 Mockito 单元测试；`web` 用 `@SpringBootTest + MockMvc` 集成测试，连本地 MySQL `dog_config_test`（自动建库、事务回滚）。
- **收尾**：提交前运行 `mvn install`，确保全模块与测试通过。

## License

本项目采用 [MIT License](LICENSE)。
