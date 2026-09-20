# 设计决策记录 — Business Config v0.1

> Design 层（Why / 关键取舍）。
> 对应 Spec：`specs/v0.1/domain-model.md`、`specs/v0.1/api-design.md`、`specs/v0.1/project-structure.md`

## 决策汇总

| # | 决策点 | 选择 | 备选 | 理由 |
| --- | --- | --- | --- | --- |
| D1 | 数据访问层 | MyBatis-Plus | JPA | 用户环境有 go-admin 等 MyBatis 先例；手写 SQL 可控性强 |
| D2 | 数据库 | 本地 MySQL | H2 / Redis | V1 直接读库（需求书 §23.3 允许）；Redis 缓存留后续 |
| D3 | 管理后台 | 静态页面（原生 JS） | Thymeleaf / Vue | 无前端构建链，V1 保持简单（需求书 §27 原则四） |
| D4 | 读取通道 | 服务内 Bean + HTTP 双通道 | 仅 HTTP / 仅 Bean | 需求书 §16 为 Java 方法、§17 为 HTTP，双通道覆盖两种使用方 |
| D5 | 项目结构 | 多模块 sdk/core/web | 单模块 | 业务系统仅需依赖 sdk 即可获得类型化读取契约 |
| D6 | sdk 独立性 | sdk 只定义接口，不依赖 core | sdk 依赖 core | 保持契约纯净，业务系统不引入实现细节 |
| D7 | Options 存储 | JSON 字符串列 | 独立 options 表 | V1 配置表量小，JSON 列直观（需求书 §13 示例即 JSON） |
| D8 | 软删除 | @TableLogic（deleted 列） | 手写 status 过滤 | MyBatis-Plus 原生支持，自动过滤，status 保留 ACTIVE/DISABLED 业务态 |
| D9 | 包名 | com.echozoo.config | com.echozoo.dogconfig | 用户选择 |
| D10 | 配置值存储 | value/defaultValue 均字符串原始存储 | 类型化列 | 一个列适应 6 种类型，读取时按 valueType 解析（需求书 §10） |

## 关键决策详述

### D4：双通道读取

- **HTTP 通道**（`/api/configs`）：供非 JVM 业务系统 / 运维调试
- **Bean 通道**（sdk `ConfigService`）：供同进程 JVM 业务系统直接注入
- 两者共用同一份读取语义（value → defaultValue → 参数 / null），实现集中在 core

### D5 / D6：多模块与 sdk 独立

```text
dog-config-sdk  ──▶ （无依赖，纯接口契约）
dog-config-core ──▶ 实现 sdk 接口（不依赖 sdk 之外的项目模块）
dog-config-web  ──▶ 依赖 core（装配） + sdk（契约）
```

业务系统接入 = 引入 `dog-config-sdk` 一个依赖即可，无需感知存储与实现。

### D8：软删除与状态分离

- `deleted` 列由 `@TableLogic` 维护（0/1），删除操作自动置 1，所有查询自动过滤
- `status` 列是业务态（ACTIVE/DISABLED），由管理端显式控制
- 语义上 deleted=1 与 status=DELETED 重复，但 deleted 由框架维护、status 由业务维护，二者职责分离（对齐需求书 §18 状态模型）

### D10：字符串原始存储

- value / defaultValue 存字符串，不按类型建列
- 好处：统一处理、DB 结构稳定；代价：读取时需类型转换（core 中集中处理）
- 类型转换失败抛 `IllegalArgumentException`（Bad Request）

## 未决 / 后续可优化

- 读取性能：V1 直连 DB，未来可加 Local Cache（需求书 §23.3 的架构图）
- 前缀查询 `LIKE 'prefix.%'`：表量大时需前缀索引，V1 表小暂不建
- 权限：V1 仅基础管理权限，Page/Group/Item 级权限留后续
