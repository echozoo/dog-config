# dog-config / Business Config 业务通用配置管理系统

> 面向业务系统的统一配置管理基础设施，通过 **Page → Group → Item** 三级模型，对零散的业务配置和基础设置进行统一存储、统一管理、统一访问。

## 定位

本系统**不是** Nacos / Apollo 这类应用运行时配置中心，核心解决的是**业务通用配置管理**问题（需求书 §3）。

V1 能力范围（需求书 §25）：

- Page / Group / Item 三级配置模型与 CRUD
- 配置类型（STRING / INTEGER / LONG / DECIMAL / BOOLEAN / JSON）
- 默认值、组件类型（INPUT / NUMBER / SWITCH / SELECT / RADIO / TEXTAREA）、Options
- 配置查询 API（单条 / 批量 keys / 前缀批量）
- 类型化 SDK（Java Bean）读取接口
- 基础管理后台（静态页面）

V1 不做：实时推送、Long Polling、MQ、Raft、多租户、复杂权限、配置版本、审批、灰度、缓存。

## 文档

| 位置 | 内容 |
| --- | --- |
| `doc/product/v0.1/` | 需求规格说明书 v1.0 |
| `specs/v0.1/README.md` | Spec 流程索引（Requirement → Spec → Design → Tasks → Review） |
| `specs/v0.1/requirements.md` | 需求确认记录与边界 |
| `specs/v0.1/domain-model.md` | 领域模型规格（Spec / What） |
| `specs/v0.1/database-design.md` | 数据库设计（Design / How） |
| `specs/v0.1/api-design.md` | API 设计（Design / How） |
| `specs/v0.1/project-structure.md` | 模块结构设计（Design / How） |
| `specs/v0.1/design-decisions.md` | 设计决策记录（Why / 取舍） |
| `specs/v0.1/tasks.md` | 任务分解表（可追溯） |
| `specs/v0.1/review.md` | 验收与评审记录 |

## 模块结构

```text
dog-config/
├── dog-config-sdk/   # 独立 SDK：业务系统可引入的类型化读取接口契约
├── dog-config-core/  # 领域模型 + MyBatis-Plus Mapper + ConfigService 实现
└── dog-config-web/   # Spring Boot 启动器 + 管理端 CRUD + HTTP 读取接口 + 静态后台
```

依赖方向：`core → sdk（接口）`，`web → core + sdk`。sdk 独立、不含实现。

## 快速开始

前置：本地 MySQL（默认 `admin/123456`，可改 `dog-config-web/src/main/resources/application.yml`）。

```bash
# 1. 建库（默认 dog_config）
mysql -uadmin -p123456 -e "CREATE DATABASE dog_config CHARACTER SET utf8mb4"

# 2. 建表 + 种子数据（也可启动后由应用执行 db/schema.sql、db/data.sql）
mysql -uadmin -p123456 dog_config < dog-config-web/src/main/resources/db/schema.sql
mysql -uadmin -p123456 dog_config < dog-config-web/src/main/resources/db/data.sql

# 3. 编译 + 测试（集成测试用独立测试库 dog_config_test，自动建库）
mvn install

# 4. 启动
mvn -pl dog-config-web spring-boot:run
```

访问：

- 管理后台：http://localhost:8080/
- 业务读取：`GET /api/configs/{key}`、`GET /api/configs?keys=a,b`、`GET /api/configs?prefix=order.`
- 管理 API：`/api/pages`、`/api/groups`、`/api/items`（详见 `specs/v0.1/api-design.md`）

## 配置读取规则

```text
value 非空            → 使用 value
value 空，defaultValue 非空 → 使用 defaultValue（DB 集中兜底）
value 空，defaultValue 空  → 带参重载返回调用方传入参数；无参重载返回 null
```

## SDK 使用示例

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

Integer minutes = configService.getInt("order.auto.cancel.minutes"); // 30
Boolean overSell = configService.getBoolean("order.allow.over.sell"); // false
String channel = configService.getString("logistics.default.channel", "SF");
```
