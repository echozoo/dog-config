## Context

见 `proposal.md — Why`。相关现状：

- 写入编排集中在 `ConfigAdminService`（web）：`createItem` / `updateItem` / `updateItemValue` / `updateItemStatus` / `deleteItem`。
- 读取集中在 `ConfigServiceImpl`（core），只读 `config_item` 当前值，且被 SDK 与 HTTP 两条通道共享——因此版本能力必须绕开读路径。
- `config_item` 使用 MyBatis-Plus `@TableLogic`（`deleted` 列），普通查询自动过滤软删行。
- 前置 change `harden-config-writes` 已提供「包含已删记录」的查询支撑与「删后同名 CONFLICT」语义。

## Goals / Non-Goals

**Goals:**

- 值变更有完整、不可篡改的历史，可查询、可回滚。
- 软删配置项可通过值级恢复重新生效，且恢复动作本身留痕。
- 读路径、SDK 契约、性能零影响。

**Non-Goals:**

- 不做草稿/发布审批、不做环境/作用域版本（Shape 2/3）。
- 不记录 operator（无鉴权）。
- 不记录非值字段（name/description/status/sort）的历史。
- 不物理删除、不修改既有版本。
- 不做跨版本 diff UI。

## Decisions

### D1：版本表结构与快照范围

新增 `config_item_version`（只存值相关字段）：

```
id            BIGINT PK AUTO_INCREMENT
item_id       BIGINT       -- → config_item.id
version_no    INT          -- item 内自增
value         TEXT
default_value TEXT
value_type    VARCHAR(32)  -- 记录该版本值的类型（信息性）
change_type   VARCHAR(32)  -- CREATE | UPDATE | ROLLBACK | RESTORE
created_at    DATETIME
UNIQUE (item_id, version_no)
```

无 `deleted` 列（历史不可删）、无 `operator` 列。**备选**：整行快照——被否，与「只记值变更」的触发范围不一致，且回滚可能误覆盖非值字段。

### D2：版本触发集中在写入编排

在 `ConfigAdminService` 写入路径统一调用一个「追加版本」方法，触发规则：

| 操作 | 是否产生版本 | change_type |
|------|------|------|
| createItem | 是（初始值，可为空） | CREATE |
| updateItem，`value`/`defaultValue` 有变化 | 是 | UPDATE |
| updateItem，仅非值字段变化 | 否 | — |
| updateItemValue | 是 | UPDATE |
| updateItemStatus / deleteItem | 否 | — |
| rollback | 是 | ROLLBACK |
| restore | 是 | RESTORE |

判断「值是否变化」用原始字符串比较（`value`、`defaultValue`）。

### D3：version_no 生成与并发

同一事务内 `SELECT MAX(version_no) WHERE item_id = ?` 后插入，`UNIQUE(item_id, version_no)` 作为并发安全网。配置写为低频写，够用。

### D4：恢复需要绕过逻辑删除

版本表自身无逻辑删除，查询直接走 Mapper。恢复已删 `config_item` 需要显式 SQL（`UPDATE ... SET deleted = 0 WHERE id = ?`），并在恢复前用「包含已删」查询确认该行存在且已删除，否则 `NOT_FOUND`。

### D5：回滚实现

`POST /api/items/{id}/versions/{versionNo}/rollback`：读版本行（不存在 → `NOT_FOUND`）→ 将该版本的 `value` / `defaultValue` 写回当前项 → 追加一条 ROLLBACK 版本。既有多条 ROLLBACK 允许累积（每次回滚都是一次真实操作）。`value_type` 不随回滚恢复，仅作信息记录——与 spec「回滚写回 value/defaultValue」一致。

### D6：恢复实现

`POST /api/items/{id}/restore`：确认存在且已软删，否则 `NOT_FOUND` → 解除软删 → 以恢复后的当前值追加一条 RESTORE 版本。

### D7：端点与错误码

```
GET  /api/items/{id}/versions                → 版本列表（按 version_no）
GET  /api/items/{id}/versions/{versionNo}    → 版本详情
POST /api/items/{id}/versions/{versionNo}/rollback
POST /api/items/{id}/restore
```

沿用 `ApiResponse` 包装；不存在资源返回 `NOT_FOUND`。

### D8：分层

版本实体 / Mapper / 版本表在 core；追加版本、回滚、恢复的编排放在 web `ConfigAdminService`（与现有写入逻辑同处）。

## Risks / Trade-offs

- [版本表无限增长] 配置变更低频，可接受 → 预留后续按数量/时间保留策略，本 change 不做。
- [回滚不恢复 valueType] 若历史版本与当前 valueType 不同，回滚只写值 → 记录在案；如需改类型应显式更新配置项。
- [恢复语义跨 change] 恢复依赖 `harden-config-writes` 的 409 提示引导；两个 change 建议按顺序应用。
- [存量数据无历史] 已有配置项在首次值变更前版本列表为空 → 迁移脚本可选地为其回填一条 CREATE 版本（见 Migration）。

## Migration Plan

- 新增 `config_item_version` 表，使用 `CREATE TABLE IF NOT EXISTS`，与 `db/schema.sql` 同步；另同步 `specs/v0.1/database-design.md`。
- 回滚策略：删表即可回退 schema；应用回退到旧包后该表不被引用。
- 存量数据：提供可选回填脚本，为现有 `config_item` 各插入一条基于当前值的 CREATE 版本；不阻塞发布。

## Open Questions

- 版本保留策略（数量上限 / 时间窗口）是否纳入下一轮——不影响本 change 的规格与任务拆分。
