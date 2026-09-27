# 设计决策记录 — Business Config v0.1

> Design 层（Why / 关键取舍）。
> 对应 Spec：`openspec/specs/config-model/spec.md`、`openspec/specs/config-admin-api/spec.md`、`openspec/specs/config-read-api/spec.md`
> 对应 Design：`doc/design/database-design.md`、`doc/design/api-design.md`、`doc/design/project-structure.md`

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
| D11 | 值校验与解析同源 | core 共享值编解码器 | 写路径各自校验 / DTO 注解 | 读写规则漂移会导致「写通过、读失败」；注解无法表达由请求决定的动态 valueType |
| D12 | 唯一性与删除保护 | 查重覆盖已软删除记录 + 父级删除保护 | 唯一索引含 deleted / 级联删除 | 软删行仍占用唯一约束，需显式返回 409；删除仍有子级的 Page/Group 需先清空 |
| D13 | 配置值版本 | append-only `config_item_version`，只记值变更 | 整行快照 / 发布-草稿双区 | 只记 value/defaultValue/valueType，回滚即写回旧值再追加 ROLLBACK；读路径仍只读当前值 |

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

### D11：值校验与解析同源

- core 提供共享值编解码器，读路径解析与写路径校验使用同一份类型规则
- 写入时校验 value/defaultValue 能否按 valueType 解析、componentType 与 valueType 兼容、SELECT/RADIO 提供合法 JSON 数组 options、required 至少一个取值非空；不满足返回 `BAD_REQUEST`

### D12：软删感知的唯一性与父级删除保护

- 唯一性查重覆盖已软删除记录：命中已删除记录时返回 `CONFLICT`（提示「已被已删除记录占用」），避免触碰数据库唯一索引异常
- 删除 Page / Group 时若存在未删除子级，返回 `CONFLICT`，要求先清空子级

### D13：配置值版本（append-only）

- `config_item_version` 只在 `value` / `defaultValue` 变化时追加，记录 value/defaultValue/valueType 与 change_type（CREATE/UPDATE/ROLLBACK/RESTORE），无 deleted、无 operator
- 回滚 = 写回旧值 + 追加 ROLLBACK 版本；恢复软删项 = 解除软删 + 追加 RESTORE 版本
- 读路径与 SDK 契约零改动：读取始终返回当前值

## 未决 / 后续可优化

- 读取性能：V1 直连 DB，未来可加 Local Cache（需求书 §23.3 的架构图）
- 前缀查询 `LIKE 'prefix.%'`：表量大时需前缀索引，V1 表小暂不建
- 权限：V1 仅基础管理权限，Page/Group/Item 级权限留后续
